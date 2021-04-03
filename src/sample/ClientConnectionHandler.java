package sample;

import java.io.*;
import java.net.Socket;

/**
 * this is the class that initializes a single thread
 * it will be used to handle the functions of the server
 * it will recognized the command from client
 * and react with proper actions such as "INSTALL" "UPDATE" "DIR"
 */
class ClientConnectionHandler extends Thread {
    Socket clientSocket;//Socket
    BufferedReader in;  //networkInput
    PrintWriter out;    //networkOutput
    File directory;     //server folder

    /**
     * this method is used to initialize single a thread
     * its features including read, import, export files
     * @param socket the socket of the client
     * @param dir the file of the directory
     * @throws IOException
     */
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
            System.out.println("listening for command");
            input = in.readLine();
            System.out.println("command received: <"+input+">");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: "+e);
        }
        if(input!=null){
            //check for single line command
            if(input.split(" ").length!=1){
                command = input.split(" ", 2)[0];
                argument = input.split(" ", 2)[1];
            }
            else{
                command= input.split(" ", 2)[0];
                argument = null;
            }
            boolean threadEnd = false;
            while (!threadEnd) {
                try { threadEnd = serverAction(command, argument); }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.err.println("Error: "+e);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Error: "+e);
                }
            }

            //closing socket
            try { clientSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        else { System.err.println("Error: Command is null"); }
    }

    /**
     * this method is used to react with the command
     * which is sent by the client
     * for "DIR" it will return list of files that from the server folder
     * for "Download" it will send the files mentioned in command to client
     * for "upload" server will receive the file mentioned in command from client
     * then store the file into the server folder
     * then the thread will end
     * @param command the string that describe the command like "download" "upload" "dir"
     * @param argument the string describe the file name
     * @return boolean true
     * @throws IOException
     */
    private boolean serverAction(String command, String argument) throws IOException {
        fileManager fm = new fileManager();
        //Checking for types of command
        switch (command) {
            case "DIR" -> {
                //no argument in this case
                System.out.println("DIR command received");
                out.println(fm.getFileList(directory));
            }
            case "DOWNLOAD" -> {
                System.out.println("Download command received");
                System.out.println("received argument(fileName): <" + argument+">");

                //find file in server folder
                File file = fm.findFile(argument, directory);

                //Check if directory is incorrectly passed by client
                if(file==null||file.isDirectory()){
                    return true;
                }
                else {
                    System.out.println("path found: <" + file.getAbsolutePath()+">");
                    String data = fm.readFile(file);

                    //passing data to client via networkOutput
                    out.println(data);
                    System.out.println("content of file: <" + data+">");
                }

            }
            case "UPLOAD" -> {
                System.out.println("UPLOAD command received");

                //resolving received argument
                String fileName = argument.split(" ")[0];
                String content = argument.split(" ")[1];
                System.out.println("Resolved fileName <"+fileName+">");
                System.out.println("Resolved content <"+content+">");

                //writing file into server folder
                fm.writeFile(directory + "/" + fileName, content);
            }
        }
        System.out.println("Thread END");
        System.out.println();
        return true;
    }

}
