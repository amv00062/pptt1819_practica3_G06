package practica3;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;


public class HttpConnection implements Runnable {

    private final static String CRLF = "\r\n";

    Socket socket = null;
    final String ruta = "archivos";

    public HttpConnection(Socket s) {
        socket = s;
    }

    @Override
    public void run() {

        DataOutputStream dos = null;
        String envio = ""; //
        byte[] pagina = null; 
        String line = "";
        String cabeceras = "";
       
        
        
        try {
            BufferedReader bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            line = bis.readLine();
            System.out.println("HTTP-HEADER: " + line);
            String readline = "";
            while ((readline = bis.readLine()) != null && readline.compareTo("") != 0) { //Recogemos las cabeceras 
                cabeceras += readline + CRLF;
            
                System.out.println("HTTP-HEADER: " + readline);
            }
            
           

        } catch (IOException ex) {
            System.err.println("Error con la conexión [" + socket.getInetAddress().getHostAddress() + "]");
            Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        

    

        try {

            String part = analiza_cabeceras(line); 
            pagina = searchFile(part);
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 ok");
            sb.append(CRLF);
            sb.append(buildHeader(part, pagina.length));
            envio = sb.toString();

        } catch (HTTPException505 e) { 
            pagina = "<H1>version no soprtada</H1>".getBytes();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 505 HTTP Version Not Supported");
            sb.append(CRLF);
            sb.append(buildHeader(".html", pagina.length));
            envio = sb.toString();
            
            
        }catch (HTTPException400 e) { 
            pagina = "<H1>peticion erronea</H1>".getBytes();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 400 Bad Request");
            sb.append(CRLF);
            sb.append(buildHeader(".html", pagina.length));
            envio = sb.toString();

            
        } catch (HTTPException405 e) { 
            pagina = "<H1>Metodo distinto de get</H1>".getBytes();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 405 Method Not Allowed");
            sb.append(CRLF);
            sb.append(buildHeader(".html", pagina.length));
            envio = sb.toString();

            
             
        }catch (HTTPException404 e) { 
            pagina = "<H1>Pagina no encontrada</H1>".getBytes();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 404 Not Found");
            sb.append(CRLF);
            sb.append(buildHeader(".html", pagina.length));
            envio = sb.toString();

        }  
         
            try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.write(envio.getBytes());
            dos.write(pagina);
            dos.flush();

            dos.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Error con la conexión... " + socket.getInetAddress().getHostAddress());
            Logger.getLogger(HttpConnection.class.getName()).log(Level.SEVERE, null, ex);
        } 
        }
    

    
    protected String analiza_cabeceras(String line) throws HTTPException400, HTTPException505, HTTPException405 {
        if (line != null) { //error de peticon
            String[] division = line.split(" ");
            if (division.length == 3) { // error de peticion

                if (division[2].equals("HTTP/1.0") || division[2].equals("HTTP/1.1") ) {//error version
                                // para la version no soprtada cambiar el 1.1 por cualquier otro
                        if (division[0].equals("GET")) {//error metodo
                          
                            String path = division[1];
                                if (path.compareTo("/") == 0) {//error archivo
                                path = "/index.html";
                                }
                            return path;
                        } else                          
                            throw new HTTPException405(); 
                } else                    
                    throw new HTTPException505();
            } else 
                throw new HTTPException400();
        } else 
            throw new HTTPException400();
    }

    protected byte[] searchFile(String path) throws HTTPException404 {
        String file = ruta + path;
        byte[] pagina = null;

        try {
           

            File archivo = new File(file);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            long length = archivo.length();
            int int_length = (int) (length % 100000);
            byte[] array = new byte[int_length];
            bis.read(array, 0, int_length);
            pagina = array;

        } catch (FileNotFoundException ex) {
            throw new HTTPException404();
        } catch (IOException ex) {
            throw new HTTPException404();
        }
        return pagina;
    }

   
    
    protected String buildHeader(String path, int length) {
        StringBuilder cabeceras = new StringBuilder();
        cabeceras.append("Connection: close"+CRLF);
        String[] subs = path.split("\\.");
        switch (subs[1]) {
            case "jpg":
                cabeceras.append("Content-Type: image/jpeg");
                break;
            case "html":
                cabeceras.append("Content-Type: text/html");
                break;
        }
        cabeceras.append(CRLF);
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("E, d MMM y hh:mm:ss z");
        String date = ft.format(dNow);
        cabeceras.append("Date: " + date + CRLF);
        cabeceras.append("Server: sergio cabellero y angel moreno"+CRLF);
        cabeceras.append("Allow: GET" + CRLF);
        cabeceras.append("Content-Length: " + length + CRLF);
        // fin de las cabeceras
        cabeceras.append(CRLF);

        return cabeceras.toString();
    }

    public class HTTPException400 extends IOException {
    }

    public class HTTPException404 extends IOException {
    }

    public class HTTPException505 extends IOException {
    }

    public class HTTPException405 extends IOException {
    }

}
