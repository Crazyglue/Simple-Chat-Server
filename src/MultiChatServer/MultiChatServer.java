package MultiChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Eric
 */
public class MultiChatServer extends Application {
    int port = 1337;
    int numClients = 0;
    ObjectInputStream in;
    ObjectOutputStream out;
    TextArea log = new TextArea();
    ArrayList<Socket> clients = new ArrayList<>();
    ArrayList<ObjectOutputStream> clientOutputs = new ArrayList<>();
    Message outToClients = null;
    File logFile;
    
    
    @Override
    public void start(Stage primaryStage) {
        log.setEditable(false);
        logFile = new File("log.dat");
        
        
        Scene scene = new Scene(log, 500, 300);
        primaryStage.setTitle("MultiChat - Server");
        primaryStage.setScene(scene);
        primaryStage.show();
        //Thread created for server
        log.appendText("MultiChatServer started: " + new Date() + "\n");
        
        ExecutorService mainExecutor = Executors.newCachedThreadPool();
        //ExecutorService handleExecutor = Executors.newCachedThreadPool();
        //input thread
        mainExecutor.execute(new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(port);
                
                while (true) {
                    
                    //ServerSocket server = new ServerSocket(port);
                    System.out.println("Input thread created");
                    
                    //Accept connection and add to the list of clients
                    Socket socket = server.accept();
                    clients.add(socket);
                    clientOutputs.add(new ObjectOutputStream(socket.getOutputStream()));
                    numClients++;
                    Platform.runLater( () -> {
                        log.appendText("Starting thread for client " + numClients + " at " + new Date() + "\n");
                        InetAddress inetAddress = socket.getInetAddress();
                        log.appendText("Client " + numClients + " hostname: \t\t" + inetAddress.getHostName() + "\n");
                        log.appendText("Client " + numClients + " IP Address: \t\t" + inetAddress.getHostAddress() + "\n");
                    });
                    
                    new Thread(new HandleClient(socket)).start();
                }
            } catch(IOException io) {
                io.printStackTrace();
            }
        }));
        mainExecutor.shutdown();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }


    class HandleClient implements Runnable {
        private Socket socket;

        public HandleClient(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Message message;

                while (true) {
                    //Grabs
                    System.out.println("(Server) Listening...");
                    message = (Message) input.readObject();
                    
                    //send the message to all clients
                    for (ObjectOutputStream m : clientOutputs)
                        m.writeObject(message);                    

                    //prints a message in the system tray
                    System.out.println(message.getDate() + " :: " + message.getName() + " : " + message.getMessage());
                }

            } catch(IOException io) {
                io.printStackTrace();
                clients.remove(socket);
                try {
                    clientOutputs.remove((ObjectOutputStream) socket.getOutputStream());
                } catch (IOException ioe) {
                    System.out.println("Error removing from output stream");
                }
            } catch(ClassNotFoundException cnf) {
                cnf.printStackTrace();
            }
        }
    
    }
}