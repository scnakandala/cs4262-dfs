package cs4262.dfs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileRepository {
    
    private static FileRepository instance;
    
    private ArrayList<String> fileNames;
    
    public static FileRepository getInstance(){
        if(FileRepository.instance==null){
            FileRepository.instance = new FileRepository();
        }
        return FileRepository.instance;
    }
    
    
    private FileRepository(){
        try {
            //Initialise random files from fileNames file
            BufferedReader reader = new BufferedReader(new FileReader("filenames"));
            fileNames = new ArrayList<>();
            String temp = reader.readLine();
            while(temp != null && !temp.isEmpty()){
                fileNames.add(temp);
                temp = reader.readLine();
            }
            
            //removing 15 random files to get 5 remaining random files.
            for(int i=0;i<15;i++){
                int rand = (int)Math.random()*fileNames.size();
                fileNames.remove(rand);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRepository.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.exit(-1);
        } catch (IOException ex) {
            Logger.getLogger(FileRepository.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.exit(-1);
        }
    }
    
    
    public boolean checkFileExists(String fileName){
        for(int i=0;i<fileNames.size();i++){
            String temp = fileNames.get(i);
            temp = temp.toLowerCase();
            temp = temp.replaceAll(" ", "");
            fileName = fileName.toLowerCase();
            fileName = fileName.replaceAll(" ", "");
            if(temp.contains(fileName)){
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<String> getAllFilesForQuery(String query){
        //TODO getting the list of files for query
        return new ArrayList<String>();
    }
}
