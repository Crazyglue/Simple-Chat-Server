/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiChatServer;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Eric
 */
public class Message implements Serializable {
    String message;
    String name;
    Date date;
    
    public Message(String name, String message) {
        this.name = name;
        this.message = message;
        date = new Date();
    }
    
    public String getMessage() { return message; }
    public String getName() { return name; }
    public String getDate() { return date.toString(); }
    public String getNameDate() { 
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String strDate = "(" + sdf.format(date) + ")";
        return strDate + this.name; 
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String strDate = "(" + sdf.format(date) + ")";        
        String message = strDate + this.name + ": \t" + this.message + "\n";
        return message;
    }
}
