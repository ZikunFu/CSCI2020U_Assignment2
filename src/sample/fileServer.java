package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * this class is used to initialize multi-threads server
 */
public class fileServer {
    //server config
    protected String SERVER_PATH = "server_shared";
    protected int SERVER_PORT = 16789;
    protected int MAX_THREAD = 20;

    /**
     * this method is used to establish multiple threads
     * @throws IOException
     */
    public fileServer() throws IOException {
        Socket clientSocket ;
        File dir = new File(SERVER_PATH);
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        ClientConnectionHandler[] threads =
                new ClientConnectionHandler[MAX_THREAD];
        int count = 1;
        // to make sure it is multi-thread
        while (true){
            clientSocket = serverSocket.accept();
            System.out.println("Connection "+count+" established");
            threads[count]=new ClientConnectionHandler(clientSocket,dir);
            threads[count].start();
            count++;
        }
    }
    public static void main(String[] args) throws IOException { fileServer server = new fileServer(); }
}
