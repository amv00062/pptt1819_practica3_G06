package practica3;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class MainServer {
static ServerSocket server= null;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        try {
            InetAddress serveraddr=InetAddress.getLocalHost(); 
            server= new ServerSocket (80,5,serveraddr); 
            System.out.println("Server waiting for HTTP connections...");
            
            while(true){
            Socket s=server.accept();
            HttpConnection conn = new HttpConnection(s);
            new Thread(conn).start();
            
            }
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
    }
        
       
        
        
        
    }
    
}
