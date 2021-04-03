package sample;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main extends Application {
    String path ="local_Shared/";
    int port = 16789;
    @Override
    public void start(Stage primaryStage) {
        //loading resources
        Image img1 = new Image("sample/resources/upload.png",20,20,true,true);
        Image img2 = new Image("sample/resources/download.png",20,20,true,true);
        Image img3 = new Image("sample/resources/export.png",20,20,true,true);
        Image img4 = new Image("sample/resources/browser1.png",20,20,true,true);
        Image img5 = new Image("sample/resources/file.png",20,20,true,true);
        Image img6 = new Image("sample/resources/browser2.png",20,20,true,true);
        ImageView up = new ImageView(img1);
        ImageView down = new ImageView(img2);
        ImageView export = new ImageView(img3);
        ImageView browser_white = new ImageView(img4);
        ImageView file = new ImageView(img5);
        ImageView browser_dark = new ImageView(img6);

        //Arguments
        Parameters params = getParameters();
        List<String> args = params.getRaw();
        System.out.println("arguments: " + args.toString());
        //Panes
        BorderPane border = new BorderPane();
        HBox hbox = new HBox(20);

        //Buttons
        Button upload = new Button("Upload");
        upload.setGraphic(up);
        Button download = new Button("Download");
        download.setGraphic(down);
        Button browse = new Button("Browse");
        browse.setGraphic(browser_white);

        //TreeView
        System.out.println("updating TreeView");
        TreeItem<String> root = updateTree(args.get(0));
        TreeView<String> tree = new TreeView<>(root);
        System.out.println("TreeView updated");
        //ListView
        ListView<String> list = new ListView<>();

        //TextArea
        TextArea textArea = new TextArea();

        //Button action
        String filename="";
        upload.setOnAction(actionEvent -> {
            try {
                client(args.get(0),port,"UPLOAD "+filename);
            }catch (Exception e){
                System.out.println("Error while uploading");
                System.out.println(e);
            }

        });
        download.setOnAction(actionEvent -> {
            String selected = tree.getSelectionModel().getSelectedItem().getValue();
            if(!list.getItems().contains(selected)){
                List<String> data = client(args.get(0),port,"DOWNLOAD "+selected);
                if(data!=null){
                    list.getItems().add(selected);
                    fileManager fm = new fileManager();
                    try {
                        fm.writeFile(path+selected, data.toString());
                        textArea.setText(data.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("Error, return from class client is null");
                }
            }
            else { System.out.println("Already exists"); }

        });

        //Scene config
        hbox.getChildren().addAll(upload,download,browse);
        border.setTop(hbox);
        border.setLeft(tree);
        border.setRight(list);
        border.setBottom(textArea);
        primaryStage.setTitle("File Sharer - Client");
        primaryStage.setScene(new Scene(border, 500, 600));

        primaryStage.show();
    }
    private List<String> client(String ip, int port, String arg){
        Socket socket;
        BufferedReader in;
        PrintWriter out;

        //Establish connection
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter( socket.getOutputStream(),true);
            out.println(arg);
            System.out.println("outputting to server" + arg);
        }catch (IOException e) {
            System.out.println(
                    "IOEXception while opening a read/write connection");
            System.out.println("Error:  " + e);
            return null;
        }

        try{
            if(arg.equals("DIR")){
                System.out.println("DIR command passed to server");
                String data = in.readLine();
                System.out.println("received: " + data);
                List<String> temp =
                        Arrays.asList(data.split(" "));
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
                String filename = arg.split(" ")[1];
                System.out.println("Download command passed to server");
                out.println("DOWNLOAD "+filename);

                String data = in.readLine();
                System.out.println("received: " + data);
                List<String> temp =
                        Arrays.asList(data.split(" "));
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
    private TreeItem<String> updateTree(String ip){
        System.out.println("establishing connect");
        List<String> fileList = client(ip,port,"DIR");
        System.out.println("fileList received:");
        System.out.println(fileList.get(0));


        TreeItem<String> root = new TreeItem<String>("Server root");
        root.setExpanded(true);
        if(fileList!=null){
            TreeItem<String> subDir = null;
            for(String str : fileList){
                System.out.println("looping list");
                if(str.matches("(.*)@(.*)")){
                    System.out.println("found @");
                    subDir = new TreeItem<String>(str.split("@")[1]);
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
        return root;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
