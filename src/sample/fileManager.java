package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class fileManager {
    fileManager(){}

    public String readPath(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner fileReader = new Scanner(file);
        StringBuilder data = new StringBuilder();
        while (fileReader.hasNextLine()) {
            data.append(fileReader.nextLine());
        }
        fileReader.close();
        return data.toString();
    }
    public String readFile(File file) throws FileNotFoundException {
        Scanner fileReader = new Scanner(file);
        StringBuilder data = new StringBuilder();
        while (fileReader.hasNextLine()) {
            data.append(fileReader.nextLine());
        }
        fileReader.close();
        return data.toString();
    }

    public void writeFile(String path,String data) throws IOException {
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.append(data);
        fileWriter.flush();
        fileWriter.close();
    }
    public File findFile(String fileName, File dir){
        for (File file : dir.listFiles()) {
            if(file.getName().equals(fileName)){ return file; }
            else if(file.isDirectory()){
                for(File file1 : file.listFiles()){
                    if(file1.getName().equals(fileName)){ return file1; }
                }
            }
        }
        return null;
    }
    public String getFileList(File dir){
        String temp="";
        for (File file : dir.listFiles()) {
            if(file.isFile()){ temp+=file.getName()+" "; }
        }

        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
                temp+="@"+file.getName()+" ";
                for(File subFile : file.listFiles()){
                    temp+=subFile.getName()+" ";
                }
            }
        }
        return temp;
    }

}
