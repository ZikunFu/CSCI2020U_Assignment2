package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class fileServer {
    //server config
    String SERVER_PATH = "server_shared";
    int SERVER_PORT = 16789;
    int MAX_THREAD = 20;
    public fileServer() throws IOException {
        Socket clientSocket ;
        File dir = new File(SERVER_PATH);
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        ClientConnectionHandler[] threads =
                new ClientConnectionHandler[MAX_THREAD];
        int count = 1;
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
