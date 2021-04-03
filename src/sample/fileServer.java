package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class fileServer {
    public fileServer() throws IOException {
        File dir = new File("server_shared");
        ServerSocket serverSocket = new ServerSocket(16789);
        ClientConnectionHandler[] threads = new ClientConnectionHandler[20];
        Socket clientSocket ;
        int count = 1;

        while (true){
            clientSocket = serverSocket.accept();
            System.out.println("Client "+count+" connected");
            threads[count]=new ClientConnectionHandler(clientSocket,dir);
            threads[count].start();
            count++;
        }
    }
    public static void main(String[] args) throws IOException { fileServer server = new fileServer(); }
}
