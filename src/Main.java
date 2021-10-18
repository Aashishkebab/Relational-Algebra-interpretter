import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.DataFormatException;

public class Main{
    public static final String MINUS         = "-";
    public static final String UNION         = "U";
    public static final String INTERSECT     = "INTE";
    public static final String JOIN          = "*";
    public static final String SELECT        = "SELE";
    public static final String PROJECT       = "PROJ";
    public static final String CROSS_PRODUCT = "X";

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
        catch(FileNotFoundException exception){
            // This should never happen
        }
        catch(DataFormatException | IllegalArgumentException exception){
            System.out.println(exception.getMessage());
        }
    }

    /**
     * This method is very recursive. It accounts for any number of parentheses and curly braces, and intelligently ignores whitespace.
     *
     * @param query The query to run as a single line.
     * @return A DataTable with the query executed on just the portion passed in as a parameter, which will only occur when there is only one word (being the table name).
     * @throws DataFormatException
     */
    @SuppressWarnings("DuplicateExpressions")
    public static DataTable executeQuery(String query) throws DataFormatException, FileNotFoundException{
        query = query.trim();
        if(query.isBlank()){
            throw new IllegalArgumentException("The query cannot be blank.");
        }

        //noinspection SwitchStatementWithoutDefaultBranch
        switch(query.substring(0, 4)){
            case SELECT:
                return executeQuery(getItemInsideOuterParentheses(query.substring(query.lastIndexOf("}") + 1))).selectWhere(getItemInsideOuterCurlyBraces(query.substring(query.indexOf("{"), query.lastIndexOf("}") + 1)));
            case PROJECT:
                return executeQuery(getItemInsideOuterParentheses(query.substring(query.lastIndexOf(query.lastIndexOf("}") + 1)))).project(getItemInsideOuterCurlyBraces(query.substring(query.indexOf("{"), query.lastIndexOf("}") + 1)).split(","));
        }

        int firstOperator = Math.min(Math.min(Math.min(Math.min(query.indexOf(MINUS), query.indexOf(UNION)), query.indexOf(INTERSECT)), query.indexOf(JOIN)), query.indexOf(CROSS_PRODUCT)); // This is atrocious

        if(firstOperator >= 0){
            String beforeOperator = query.substring(0, firstOperator);
            String afterOperator = query.substring(firstOperator + 1);

            // Since “INTE“ is not a single character, it won't work in the switch above.
            return query.startsWith(INTERSECT, firstOperator) ? executeQuery(beforeOperator).intersectWith(executeQuery(afterOperator)) : switch(query.substring(firstOperator, firstOperator + 1)){
                case MINUS -> executeQuery(beforeOperator).minus(executeQuery(afterOperator));
                case UNION -> executeQuery(beforeOperator).unionWith(executeQuery(afterOperator));
                case JOIN -> executeQuery(beforeOperator).joinWith(executeQuery(afterOperator));
                case CROSS_PRODUCT -> executeQuery(beforeOperator).crossWith(executeQuery(afterOperator));
                default -> throw new IllegalArgumentException("An unsupported operator was used in \"" + query + "\". But also likely developer was dumb.");
            };
        }

        // If none of the above are satisfied, then we are at a raw table name, so we need to fetch that.
        String tableName = getItemInsideOuterParentheses(query);
        if(tableName.contains(")") || tableName.contains(DataTable.EQUALS) || tableName.contains(DataTable.LESS_THAN) || tableName.contains(DataTable.GREATER_THAN) || tableName.contains(MINUS) || tableName.contains(UNION) || tableName.contains(
                INTERSECT) || tableName.contains(JOIN) || tableName.contains(SELECT) || tableName.contains(PROJECT) || tableName.contains(CROSS_PRODUCT)){
            throw new DataFormatException(
                    "The string parsing algorithm was done incorrectly. Please try a simpler query so that the developer does not lose points on this very hard project. Thanks much. The algorithm thinks the table name is " + "\"" + tableName +
                    "\"");
        }

        File tableFile = new File(tableName + ".txt");
        if(!tableFile.exists()){
            throw new FileNotFoundException("Bruh, please state a table that actually exists, as " + tableFile + ".txt doesn't. The file must be of the extension \".txt\" (all lowercase).");
        }

        return new DataTable(tableFile);
    }

    public static String getItemInsideOuterParentheses(String query){
        query = query.trim();
        if(query.charAt(0) != '('){
//            throw new IllegalArgumentException("This query does not start with a parenthesis.");
            return query;
        }

        int parenthesisOffset = 0;
        for(int i = 0; i < query.length(); i++){
            if(query.charAt(i) == '('){
                parenthesisOffset++;
            }
            else if(query.charAt(i) == ')'){
                parenthesisOffset--;
            }

            if(parenthesisOffset == 0){
                return getItemInsideOuterParentheses(query.substring(1, i));
            }
        }

        if(parenthesisOffset > 0){
            throw new IllegalArgumentException("There are more opening parentheses than closing parentheses in this query substring.");
        }
        else{
            throw new UnsupportedOperationException("This should never have happened, definitely sus.");
        }
    }

    public static String getItemInsideOuterCurlyBraces(String query){
        query = query.trim();
        if(query.charAt(0) != '{'){
//            throw new IllegalArgumentException("This query does not start with a curly brace.");
            return query;
        }

        int parenthesisOffset = 0;
        for(int i = 0; i < query.length(); i++){
            if(query.charAt(i) == '{'){
                parenthesisOffset++;
            }
            else if(query.charAt(i) == '}'){
                parenthesisOffset--;
            }

            if(parenthesisOffset == 0){
                return getItemInsideOuterCurlyBraces(query.substring(1, i));
            }
        }

        if(parenthesisOffset > 0){
            throw new IllegalArgumentException("There are more opening curly braces than closing braces in this query substring.");
        }
        else{
            throw new UnsupportedOperationException("Oh noes, this is bad because this is a logically impossible case.");
        }
    }
}
