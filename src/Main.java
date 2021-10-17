import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main{
    public static void main(String[] args){
        File queriesFile = new File("RAqueries.txt");
        if(!queriesFile.exists()){
            System.out.println("The RAQueries.txt file was not found.");
            return;
        }

        try(Scanner queriesScanner = new Scanner(queriesFile)){
            while(queriesScanner.hasNextLine()){
                System.out.println(executeQuery(queriesScanner.nextLine()).toString());
            }
        }
        catch(FileNotFoundException e){
            // This should never happen
        }
    }

    public static DataTable executeQuery(final String query){
        //noinspection SwitchStatementWithoutDefaultBranch
        switch(query.substring(0, 4)){
            case "SELE":
                return executeQuery(getItemInsideOuterParentheses(query.substring(query.indexOf("(")))).selectWhere(getItemInsideOuterCurlyBraces(query.substring(query.indexOf("{"))));
            case "PROJ":
                return executeQuery(getItemInsideOuterParentheses(query.substring(query.indexOf("(")))).project(getItemInsideOuterCurlyBraces(query.substring(query.indexOf("{"))).split(","));
        }

        return null;
    }

    public static String getItemInsideOuterParentheses(final String query){
        if(!query.substring(0, 1).equals("(")){
//            throw new IllegalArgumentException("This query does not start with a parenthesis.");
            return query;
        }

        int parenthesisOffset = 0;
        for(int i = 0; i < query.length(); i++){
            if(query.substring(i, i + 1).equals("(")){
                parenthesisOffset++;
            }else if(query.substring(i, i + 1).equals(")")){
                parenthesisOffset--;
            }

            if(parenthesisOffset == 0){
                return getItemInsideOuterParentheses(query.substring(0, i));
            }
        }

        if(parenthesisOffset > 0){
            throw new IllegalArgumentException("There are more opening parentheses than closing parentheses in this query substring.");
        }else{
            throw new UnsupportedOperationException("This should never have happened, definitely sus.");
        }
    }

    public static String getItemInsideOuterCurlyBraces(final String query){
        if(!query.substring(0, 1).equals("{")){
//            throw new IllegalArgumentException("This query does not start with a curly brace.");
            return query;
        }

        int parenthesisOffset = 0;
        for(int i = 0; i < query.length(); i++){
            if(query.substring(i, i + 1).equals("{")){
                parenthesisOffset++;
            }else if(query.substring(i, i + 1).equals("}")){
                parenthesisOffset--;
            }

            if(parenthesisOffset == 0){
                return getItemInsideOuterCurlyBraces(query.substring(0, i));
            }
        }

        if(parenthesisOffset > 0){
            throw new IllegalArgumentException("There are more opening curly braces than closing braces in this query substring.");
        }else{
            throw new UnsupportedOperationException("Oh noes, this is bad because this is a logically impossible case.");
        }
    }
}
