package MultiChatClient;

import MultiChatServer.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import MultiChatServer.Message;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

/**
 *
 * @author Eric
 */
public class MultiChatClient extends Application {
    String defaultName = "Jean Luc";
    final String SERVERHOSTNAME = "localhost";
    final int SERVERPORT = 1337;
    ObjectInputStream fromServer;
    ObjectOutputStream toServer;
    Socket socket;
    TextArea log;
    TextArea logName;
    Message dataMessage = null;
    TextField message;
    //TextField name;
    Label lblName;
    VBox vbMain = new VBox();
    HBox hbMenu = new HBox();
    HBox hbInfo = new HBox();
    HBox hbLog = new HBox();
    HBox hbMessage = new HBox();
    Button sendMessage;
    MenuBar mbMain = new MenuBar();
    Menu menuFile = new Menu("File");
    Menu menuSettings = new Menu("Settings");
    MenuItem itemExit = new MenuItem("Exit");
    MenuItem itemProfile = new MenuItem("Edit Profile");
    BorderPane bpMain = new BorderPane();
    File defaultPic = new File("default.png");
    File theme = new File("theme.css");
    File userPic = defaultPic;
    String userName = defaultName;
    Image profilePic = new Image(defaultPic.toURI().toString());
    ImageView ivProfile = new ImageView(profilePic);
    Font logFont = new Font("Tahoma", 16);
    Font infoFont = new Font("Tahoma", 28);
    Font messageFont = new Font("Tahoma", 14);
    Insets infoInsets = new Insets(25, 50, 10, 50);
    Insets logInsets = new Insets(10, 50, 10, 50);
    Insets messageInsets = new Insets(10, 50, 25, 50);
    Insets infoLabelInsets = new Insets(0, 0, 0, 30);
    TextField textField;
    FileChooser fileChooser = new FileChooser();
    
    @Override
    public void start(Stage primaryStage) {
        initializeMenu();
        initializeInfoArea();
        initializeLog();
        initilizeMessage();
        itemExit.setOnAction(e -> {
            primaryStage.close();
        });
        
        ivProfile.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) 
                if(e.getClickCount() == 2) {
                    userPic = fileChooser.showOpenDialog(primaryStage);
                    ivProfile.setImage(new Image(userPic.toURI().toString()));
                }
        });
        message.setEditable(true);

        vbMain.setId("vb_main");
        vbMain.getChildren().addAll(hbMenu, hbInfo, hbLog, hbMessage);
        vbMain.setVgrow(hbLog, Priority.ALWAYS);
        vbMain.setStyle("-fx-background-color: gray");
        Scene scene = new Scene(vbMain, 800, 600);
        //set the scene's theme
        //scene.getStylesheets().add("theme.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("MultiChat - Client");
        primaryStage.show();
        
        
        
        try {
            System.out.println("Connecting to server...");
            socket = new Socket(SERVERHOSTNAME, SERVERPORT); 
            toServer = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Connections made");
            
        } catch(IOException io) {
            io.printStackTrace();
        }
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // receive input thread
        executor.execute(new Thread(() -> {
            try{
                System.out.println("Setting fromServer stream");
                fromServer = new ObjectInputStream(socket.getInputStream());
                System.out.println("fromServer stream set");
                while (true) {
                    System.out.println("(Client) Listening...");
                    Message receivedMessage = (Message) fromServer.readObject();
                    System.out.println(receivedMessage);
                    Platform.runLater(() -> {
                        log.appendText(receivedMessage.getNameDate() + ": " + receivedMessage.getMessage() + "\n");
                        //logName.appendText(receivedMessage.getNameDate() + "\n");
                    });
                    
                }
            } catch(IOException io) {
                io.printStackTrace();
            } catch(ClassNotFoundException cnf) {
                cnf.printStackTrace();
            }
        }));
        
        
        
        executor.shutdown();
        
    }  
    private void createTextField() {
        hbInfo.getChildren().remove(lblName);
        textField = new TextField(userName);
        textField.setPrefHeight(60);
        textField.setFont(infoFont);
        hbInfo.getChildren().add(textField);
        textField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                userName = textField.getText();
                hbInfo.getChildren().remove(textField);
                hbInfo.getChildren().add(lblName);
                lblName.setText(userName);
            } else if (key.getCode() == KeyCode.ESCAPE) {
                hbInfo.getChildren().remove(textField);
                hbInfo.getChildren().add(lblName);
            }
        });
    }
    
    
    private void initializeMenu() {
        mbMain.getMenus().addAll(menuFile);
        mbMain.setId("menu_main");
        menuFile.getItems().add(itemExit);
        hbMenu.getChildren().add(mbMain);
        hbMenu.setId("hb_menu");
        hbMenu.setHgrow(mbMain, Priority.ALWAYS);
    }
    
    private void initializeInfoArea() {
        ivProfile.setId("info_pic");
        ivProfile.setFitWidth(100);
        ivProfile.setFitHeight(100);
        
        
        lblName = new Label(userName);
        lblName.setId("info_name");
        lblName.setFont(infoFont);
        lblName.setPadding(infoLabelInsets);
        lblName.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) 
                if(e.getClickCount() == 2)
                    createTextField();
            
        });
        
        hbInfo.getChildren().addAll(ivProfile, lblName);
        hbInfo.setId("hb_info");
        hbInfo.setPadding(infoInsets);
        hbInfo.setAlignment(Pos.CENTER_LEFT);
    }
    private void initializeLog() {
        // Message area of the text log
        log = new TextArea();
        log.setId("log_messages");
        log.setEditable(false);
        log.setFont(logFont);
        log.setMinHeight(275);
        
        // Name area of the text log
        logName = new TextArea();
        logName.setId("log_name");
        logName.setEditable(false);
        logName.setFont(logFont);
        logName.setMaxWidth(300);
        logName.setMinWidth(200);
        logName.setMinHeight(275);
        
        
        // Log wrapper
        hbLog.getChildren().add(log);
        hbLog.setHgrow(log, Priority.ALWAYS);
        hbLog.setMinHeight(300);
        hbLog.setPadding(logInsets);
        hbLog.setId("hb_log");
    }
    private void initilizeMessage() {
        message = new TextField();
        message.setId("message_message");
        message.setMinWidth(200);
        message.setMinHeight(60);
        message.setEditable(true);
        message.setFont(messageFont);
        sendMessage = new Button("Send");
        sendMessage.setId("message_button");
        hbMessage.getChildren().addAll(message, sendMessage);
        hbMessage.setId("hb_message");
        hbMessage.setPadding(messageInsets);
        message.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                sendData();
        });
        
        sendMessage.setOnAction(e -> {
            sendData();
            
        });
    }
    
    private void sendData() {
        try {
                toServer.writeObject(new Message(userName, message.getText()));
                System.out.println("Message Sent");
            } catch (IOException io) {
                io.printStackTrace();
            }
        
    }
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
