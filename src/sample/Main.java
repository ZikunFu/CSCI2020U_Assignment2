package sample;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**Creating the UI for clients
 * make this could connect to the server
 * set the buttons actions
 * for realizing functions "DOWNLOAD" "UPDATE" "VIEW"
 * "Download" button transmits files from server to the client
 * "Update" button transports files to the server
 * "View" can check the content in files
 * The method is used to build the Scene of the client.
 */

public class Main extends Application {

    //initializing global variables
    String LOCAL_PATH ="local_Shared/";
    String SERVER_IP = "127.0.0.1";
    int SERVER_PORT = 16789;
    ListView<String> list;

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        //loading resources
        Image img1 = new Image("sample/resources/upload.png",20,20,true,true);
        Image img2 = new Image("sample/resources/download.png",20,20,true,true);
        Image img5 = new Image("sample/resources/file.png",20,20,true,true);

        ImageView up = new ImageView(img1);
        ImageView down = new ImageView(img2);
        ImageView file = new ImageView(img5);


        //Command line Arguments
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        //setting variables via arguments
        SERVER_IP = args.get(0);
        LOCAL_PATH = args.get(1);
        System.out.println("command line arguments received: " + args.toString());

        //Panes
        BorderPane border = new BorderPane();
        HBox hbox = new HBox(20);

        //Buttons
        Button upload = new Button("Upload");
        upload.setGraphic(up);
        Button download = new Button("Download");
        download.setGraphic(down);
        Button view = new Button("View");
        view.setGraphic(file);

        //TreeView
        System.out.println("updating TreeView");
        TreeItem<String> root = updateTree();
        TreeView<String> tree = new TreeView<>(root);

        //ListView
        list = new ListView<>();
        updateList();

        //TextArea
        TextArea textArea = new TextArea();

        //Button action
        upload.setOnAction(actionEvent -> {
            String fileName= list.getSelectionModel().getSelectedItem();
            boolean exist = false;

            //Checking duplicates
            for(TreeItem<String> sub : root.getChildren()){
                if(sub.getValue().equals(fileName)){ exist = true; }
            }

            if(exist){
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setTitle("Upload to server");
                a.setContentText(fileName + " already exists");
                a.show();
            }
            else {
                //find file in local by its name
                String content="";
                fileManager fm = new fileManager();
                File target = fm.findFile(fileName,new File(LOCAL_PATH));

                //read file content
                try { content = fm.readPath(target.getAbsolutePath()); }
                catch (FileNotFoundException e) { e.printStackTrace(); }

                //uploading to server
                try { client(SERVER_IP,SERVER_PORT,"UPLOAD "+fileName+" "+content); }
                catch (Exception e){
                    System.err.println("Error while uploading");
                }
                root.getChildren().add(new TreeItem<>(fileName));
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Upload to server");
                a.setContentText(fileName+ " uploaded to server");
                a.show();
            }

        });
        download.setOnAction(actionEvent -> {
            //getting filename from selected tree
            String selected = tree.getSelectionModel()
                    .getSelectedItem().getValue();

            if(!list.getItems().contains(selected)){
                //sending download command and read networkInput
                List<String> data = client(SERVER_IP,SERVER_PORT,"DOWNLOAD "+selected);

                //writing file
                if(data!=null){
                    fileManager fm = new fileManager();
                    try {
                        String content="";
                        for(String str : data){
                            content += str+" ";
                        }
                        fm.writeFile(LOCAL_PATH+selected, content);
                        textArea.setText(content);
                    } catch (IOException e) { e.printStackTrace(); }
                    //update local listview
                    updateList();
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                    a.setTitle("Download from server");
                    a.setContentText(selected+ " downloaded to <"+LOCAL_PATH+">");
                    a.show();
                }
                else {
                    Alert a = new Alert(Alert.AlertType.NONE);
                    a.setAlertType(Alert.AlertType.INFORMATION);
                    a.setTitle("Download from server");
                    a.setContentText("Directory is selected");
                    a.show();
                }
            }
            else {
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setTitle("Download from server");
                a.setContentText(selected + " already exists");
                a.show();
            }

        });
        view.setOnAction(actionEvent -> {
            //getting filename from selected list
            String selected = list.getSelectionModel().getSelectedItem();
            //find file in local then read file
            if(selected!=null){
                String content="";
                fileManager fm = new fileManager();
                File target = fm.findFile(selected,new File(LOCAL_PATH));
                try { content = fm.readPath(target.getAbsolutePath()); }
                catch (FileNotFoundException e) { e.printStackTrace(); }
                textArea.setText(content);
            }
            //check for incorrect selection in tree
            else if(tree.getSelectionModel()
                    .getSelectedItem().getValue()!=null){
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setAlertType(Alert.AlertType.INFORMATION);
                a.setContentText("Error: Can only view file in local folder");
                a.show();
            }
        });

        //Scene config
        hbox.getChildren().addAll(upload,download,view);
        border.setTop(hbox);
        border.setLeft(tree);
        border.setRight(list);
        border.setBottom(textArea);
        primaryStage.setTitle("File Sharer - Client");
        primaryStage.setScene(new Scene(border, 500, 600));

        primaryStage.show();
    }

    /**
     * this method is used to handle sending and receiving data with server
     * first it makes connection with the server
     * and then reads the received argument from the server
     * and it will check if it fits the conditions "DIR" "DOWNLOAD" "UPLOAD"
     * then it reacts with the right action
     *
     * @param ip an String that contains the ip address
     * @param port the int that the port of the ip address
     * @param arg the argument that need identify "Download" "update" "DIR"
     * @return String fileList into list of strings
     */
    private List<String> client(String ip, int port, String arg){
        Socket socket;      //socket
        BufferedReader in;  //networkInput
        PrintWriter out;    //networkOutput

        //Establish connection
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter( socket.getOutputStream(),true);

            //this line send command to server via network output
            out.println(arg);
            System.out.println("sending <" + arg + "> command to server");
        }catch (IOException e) {
            System.out.println(
                    "IOException while opening a read/write connection");
            System.out.println("Error:  " + e);
            return null;
        }

        //reading received argument from server
        try{
            //check for command types
            if(arg.contains("DIR")){
                System.out.println("DIR command passed to server");
                //receiving networkInput
                String data = in.readLine();
                System.out.println("received: " + data);

                //split string fileList into lists of strings
                return Arrays.asList(data.split(" "));

            } else if(arg.contains("DOWNLOAD")){
                //String filename = arg.split(" ")[1];
                System.out.println("Download command passed to server");
                //out.println("DOWNLOAD "+filename);

                //receiving networkInput
                String data = in.readLine();
                System.out.println("received: " + data);
                return Arrays.asList(data.split(" "));

            }else if(arg.contains("UPLOAD")){
                System.out.println("Upload command passed to server");
                return null;
            }
            else {
                System.err.println("arg <"+arg+"> is undefined");
                return null;
            }
        }catch (Exception e){
            System.err.println("Error: cannot read data from server");
            System.err.println("Error: " + e);
            return null;
        }
    }
    /**
     * this method will update the listview by reading all files in the local folder
     * it will read the local folder
     * then put them in a filename list
     * ending up with updating the list
     */
    private void updateList(){
        fileManager fm = new fileManager();
        //reading local folder
        String data = fm.getFileList(new File(LOCAL_PATH));

        //split fileNameList into lists of strings
        String[] temp =
                data.split(" ");
        //clear current listView
        list.getItems().clear();
        //add new items
        for(String str : temp){ list.getItems().add(str); }
    }

    //update TreeView by returning a new TreeView

    /**
     *this method is used to update the new tree-view
     *it will check all the files as order
     * if it meet a "@" in front of the file
     * it would be recognized as a sub directory
     * then it will enter the sub directory and add the new files inside first
     * then come back to the last node after the "@"
     *
     * @return a new tree view
     * @throws InterruptedException
     */
    private TreeItem<String> updateTree() throws InterruptedException {
        System.out.println("establishing connect");
        List<String> fileList = client(SERVER_IP,SERVER_PORT,"DIR");
        TreeItem<String> root = new TreeItem<>("Server root");
        root.setExpanded(true);
        //Check networkInput
        if(fileList!=null){
            System.out.println("fileList received:");
            System.out.println(fileList.toString());

            TreeItem<String> subDir = null;
            for(String str : fileList){
                //check for directory
                if(str.matches("(.*)@(.*)")){
                    subDir = new TreeItem<>(str.split("@")[1]);
                    root.getChildren().add(subDir);
                }
                //check for file in root folder
                else if(subDir==null){
                    TreeItem<String> temp = new TreeItem<>(str);
                    root.getChildren().add(temp);
                }
                //check for file in sub directory
                else {
                    TreeItem<String> temp = new TreeItem<>(str);
                    subDir.getChildren().add(temp);
                }
            }
        }

        //Server down or no connection
        else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error: Cannot connect to server");
            a.setContentText("Please start server before client. Program closing in 5 seconds");
            a.show();
            TimeUnit.SECONDS.sleep(5);
            Platform.exit();
            System.exit(0);
        }
        return root;
    }

    public static void main(String[] args) { launch(args); }
}