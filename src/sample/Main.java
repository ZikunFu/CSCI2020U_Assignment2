package sample;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {
    String path ="/shared";
    int port=6666;
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Arguments
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        //Panes
        BorderPane border = new BorderPane();
        HBox hbox = new HBox(20);

        //Buttons
        Button upload = new Button("Upload");
        Button download = new Button("Download");
        Button browse = new Button("Browse");

        //TreeView
        TreeItem<String> root = new TreeItem<String>("Shared Folder");
        root.setExpanded(true);
        //List<String> fileList = client(args.get(0),port,"DIR");

        List<String> fileList=new ArrayList<>();
        if(fileList!=null){
            fileList.add("@dir1");
            fileList.add("dir1-1");
            fileList.add("@dir2");
            fileList.add("dir2-1");

            TreeItem<String> subDir = null;
            for(String str : fileList){
                System.out.println("looping list");
                if(str.matches("(.*)@(.*)")){
                    System.out.println("found@!");
                    subDir = new TreeItem<String>(str);
                    root.getChildren().add(subDir);
                }
                else if(subDir==null){
                    TreeItem<String> temp = new TreeItem<String>(str);
                    root.getChildren().add(temp);
                }
                else {
                    System.out.println("adding file");
                    TreeItem<String> temp = new TreeItem<String>(str);
                    subDir.getChildren().add(temp);
                }
            }
        }
        else {
            System.out.println("Cannot get file List!");
        }
        TreeView<String> tree = new TreeView<String>(root);

        //ListView
        ListView<String> list = new ListView<String>();

        //TextArea
        TextArea textArea = new TextArea();

        //Button action
        String filename="";
        upload.setOnAction(actionEvent -> {
            client(args.get(0),port,"UPLOAD "+filename);
        });
        download.setOnAction(actionEvent -> {
            if(!list.getItems().contains(filename)){
                List<String> data = client(args.get(0),port,"Download "+filename);
                list.getItems().add(filename);
            }
            else {
                System.out.println("Already exists");
            }
        });

        //Scene config
        hbox.getChildren().addAll(upload,download,browse);
        border.setTop(hbox);
        border.setLeft(tree);
        primaryStage.setTitle("File Sharer - Client");
        primaryStage.setScene(new Scene(border, 500, 600));

        primaryStage.show();
    }
    private List<String> client(String ip, int port, String arg){
        Socket socket;
        Scanner in;
        PrintWriter out;

        //Establish connection
        try {
            socket = new Socket(ip, port);
            in = new Scanner( new InputStreamReader(socket.getInputStream()) );
            out = new PrintWriter( socket.getOutputStream() );
            out.print(arg);
            out.flush();
        }catch (Exception e) {
            System.out.println(
                    "Can't make connection to server at " + ip + ".");
            System.out.println("Error:  " + e);
            return null;
        }

        try{
            if(arg.equals("DIR")){
                List<String> temp= new ArrayList<String>();
                while (in.hasNext()){
                    temp.add(in.nextLine());
                }
                return temp;
            }
            else if(arg.equals("UPLOAD")){
                String filename=arg.split(" ")[1];
                out.println("UPLOAD");
                out.println(filename);
                fileManager fm = new fileManager();
                fm.readFile(path+"/"+filename);
                out.println(fm);
                return null;
            }
            else if(arg.contains("DOWNLOAD")){
                String filename=arg.split(" ")[1];
                out.println("DOWNLOAD");
                out.println(filename);
                List<String> temp = null;
                while (in.hasNext()){
                    temp.add(in.nextLine());
                }
                return temp;
            }
            else {
                System.out.println("Error in args");
                return null;
            }
        }catch (Exception e){
            System.out.println("An error occurred while reading data from the server.");
            System.out.println("Error: " + e);
            return null;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}