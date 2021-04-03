package sample;

import java.io.*;
import java.net.Socket;

class ClientConnectionHandler extends Thread {
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;
    File directory;

    public ClientConnectionHandler(Socket socket, File dir) throws IOException {
        super();
        clientSocket = socket;
        directory = dir;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {
        String input = null, command, argument;
        try {
            System.out.println("listen input");
            input = in.readLine();
            System.out.println("received input: "+input);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: "+e);
        }

        if(input.split(" ").length!=1){
            command = input.split(" ", 2)[0];
            argument = input.split(" ", 2)[1];
        }
        else{
            command= input.split(" ", 2)[0];
            argument = null;
        }
        boolean end = false;
        while (!end) {
            try {
                end = serverAction(command, argument);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error: "+e);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error: "+e);
            }
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean serverAction(String command, String argument) throws IOException {
        fileManager fm = new fileManager();
        if (command.equals("DIR")) {
            System.out.println("DIR command received");
            out.println(fm.getFileList(directory));
        } else if (command.equals("DOWNLOAD")) {
            System.out.println("Download command received");
            System.out.println("received filename: " + argument);
            String path = fm.findFile(argument, directory).getAbsolutePath();
            String data = fm.readFile(path);
            System.out.println("path found: " + path);
            out.println(data);
            System.out.println("content of file: " + data);
        } else if (command.equals("UPLOAD")) {
            System.out.println("UPLOAD command received " + argument);
            String fileName = argument.split(" ")[0];
            String content = argument.split(" ")[1];
            fm.writeFile(directory+"/"+fileName,content);
        }
        System.out.println("Thread end");
        return true;
    }

}
