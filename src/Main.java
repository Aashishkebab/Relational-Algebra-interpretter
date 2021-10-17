import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main{
    public static void main(String[] args){
        File queriesFile = new File("RAqueries.txt");
        if(!queriesFile.exists()){
            System.out.println("The RAQueries.txt file was not found.");
            return;
        }

        try{
            Scanner queriesScanner = new Scanner(queriesFile);
            while(queriesScanner.hasNextLine()){
                executeQuery(queriesScanner.nextLine());
            }
        }
        catch(FileNotFoundException e){
            // This should never happen
        }
    }

    private static void executeQuery(final String query){

    }
}
