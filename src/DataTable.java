import com.sun.source.tree.LambdaExpressionTree;

import javax.management.monitor.StringMonitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.module.FindException;
import java.util.*;
import java.util.zip.DataFormatException;

public class DataTable{
    public static final String EQUALS = "=";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    private final List<String[]> data = new ArrayList<>(100);
    private final String[]       columnNames;

    public DataTable(final File file) throws FileNotFoundException, DataFormatException{
        try(Scanner scanner = new Scanner(file)){
            if(scanner.hasNextLine()){
                this.columnNames = scanner.nextLine().split(",\\s*");
            }else{
                throw new DataFormatException("This file has no data.");
            }

            while(scanner.hasNextLine()){
                this.data.add(scanner.nextLine().split(",\\s*"));
            }
        }
    }

    public DataTable(String[] columnNames, List<String[]> data){
        this.data.addAll(data);
        this.columnNames = columnNames.clone();
    }

    public List<String[]> getData(){
//        return Collections.unmodifiableList(this.data);

        ArrayList<String[]> copyOfData = new ArrayList<>(this.data.size());
        copyOfData.addAll(this.data);

        return copyOfData;
    }

    public String[] getColumnNames(){
        return this.columnNames.clone();
    }

    public void addRow(String[] rowData){
        this.data.add(rowData);
    }

    /**
     * Runs a PROJECT operator on the current DataTable and projects the columns specified.
     * Automatically trims the leading and trailing whitespace from the column names passed in.
     * @param columns An array of all the column names. Leading and trailing whitespace in each element is trimmed.
     * @return A new DataTable with just the specified columns.
     */
    public DataTable project(String[] columns){
        int[] columnIndices = getIndicesInArrayOfItemsEquivalentTo(this.columnNames, columns);

        ArrayList<String[]> newData = new ArrayList<>(this.data.size());
        for(int i = 0; i < this.data.size(); i++){
            newData.add(new String[columnIndices.length]);
            for(int j = 0; j < columnIndices.length; j++){
                newData.get(i)[j] = this.data.get(i)[columnIndices[j]];
            }
        }

        return new DataTable(columns,  newData);
    }

    /**
     * @param whereClause Single condition as String, supporting >, <, and =.
     * @return A new DataTable with a WHERE clause applied to the current one.
     */
    public DataTable selectWhere(String whereClause){
        char numberOfOperators = 0;
        String operator = "";
        if(whereClause.contains(GREATER_THAN)){
            numberOfOperators++;
            operator = GREATER_THAN;
        }if(whereClause.contains(LESS_THAN)){
            numberOfOperators++;
            operator = LESS_THAN;
        }if(whereClause.contains(EQUALS)){
            numberOfOperators++;
            operator = EQUALS;
        }

        if(numberOfOperators > 1){
            throw new IllegalArgumentException("This WHERE clause is malformed. More than one operator was detected.");
        }else if(numberOfOperators < 1){
            throw new IllegalArgumentException("This WHERE clause has no valid comparison operator. Use '>', '<', or '='.");
        }

        String columnName = whereClause.substring(0, whereClause.indexOf(operator)).trim();
        String comparedValue = whereClause.substring(whereClause.indexOf(operator) + 1).trim();

        ArrayList<String[]> newData = new ArrayList<>(this.data.size());

        int columnIndex = getIndexInArrayOfItemEquivalentTo(this.columnNames, columnName);
        for(int i = 0; i < this.data.size(); i++){
            //noinspection SwitchStatementWithoutDefaultBranch
            switch(operator){
                case GREATER_THAN:
                    if(Double.parseDouble(this.data.get(i)[columnIndex]) > Double.parseDouble(comparedValue)){
                        newData.add(this.data.get(i));
                    }
                    break;
                case LESS_THAN:
                    if(Double.parseDouble(this.data.get(i)[columnIndex]) < Double.parseDouble(comparedValue)){
                        newData.add(this.data.get(i));
                    }
                    break;
                case EQUALS:
                    if(this.data.get(i)[columnIndex].equals(comparedValue)){
                        newData.add(this.data.get(i));
                    }
                    break;
            }
        }

        return new DataTable(this.columnNames, newData);
    }

    /**
     *
     * @param otherTable The table to join with.
     * @return A new DataTable that does a full natural join on this table and the otherTable.
     * @throws DataFormatException if weird shit goes down.
     */
    public DataTable joinWith(DataTable otherTable) throws DataFormatException{
        String[] sharedColumnNames = getCommonElementsInArrays(this.columnNames, otherTable.columnNames);
        String[] newColumnNames = getCombinedArrayWithoutDuplicates(this.columnNames, otherTable.columnNames);

        int[] table1TableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(this.columnNames, sharedColumnNames);
        int[] otherTableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(otherTable.columnNames, sharedColumnNames);

        int maxNumberOfMatchingRows = Math.min(this.data.size(), otherTable.data.size());
        ArrayList<int[]> listOfMatchingIndices = new ArrayList<>(maxNumberOfMatchingRows);

        for(int i = 0; i < this.data.size(); i++){
            for(int j = 0; j < otherTable.data.size(); j++){
                if(areSpecificElementsEqualInArrays(this.data.get(i), table1TableSharedColumnIndices, otherTable.data.get(i), otherTableSharedColumnIndices)){
                    listOfMatchingIndices.add(new int[]{i, j});
                }
            }
        }

        int[][] matchingIndices = listOfMatchingIndices.toArray(new int[0][]);
        ArrayList<String[]> newData = new ArrayList<>(Math.max(this.data.size(), otherTable.data.size()));

        int table1TableNumberOfUniqueColumns = this.columnNames.length - sharedColumnNames.length, otherTableNumberOfUniqueColumns = otherTable.columnNames.length - sharedColumnNames.length;
        for(int i = 0; i < matchingIndices.length; i++){
            newData.add(new String[newColumnNames.length]);

            int newRowColumnIndex = 0;
            for(int j = 0; j < table1TableNumberOfUniqueColumns; j++){ // Add items from first table (except shared columns)
                if(!containsItemEquivalentTo(table1TableSharedColumnIndices, j)){
                    newData.get(i)[newRowColumnIndex] = this.data.get(matchingIndices[i][0])[j];
                    newRowColumnIndex++;
                }
            }
            for(int j = 0; j < table1TableSharedColumnIndices.length; j++){ // Add shared items
                if(!this.data.get(i)[table1TableSharedColumnIndices[j]].equals(otherTable.data.get(i)[otherTableSharedColumnIndices[j]])){
                    throw new DataFormatException("Developer is an idiot, as fields that were indicated as matching for merging are clearly not matching.");
                }

                newData.get(i)[newRowColumnIndex] = this.data.get(i)[table1TableSharedColumnIndices[j]];
                newRowColumnIndex++;
            }

            for(int j = 0; j < otherTableNumberOfUniqueColumns; j++){ // Add third table unique items
                if(!containsItemEquivalentTo(otherTableSharedColumnIndices, j)){
                    newData.get(i)[newRowColumnIndex] = this.data.get(matchingIndices[i][1])[j];
                    newRowColumnIndex++;
                }
            }

            if(newRowColumnIndex != (newColumnNames.length - 1)){
                throw new DataFormatException("We obviously added the wrong number of fields to this row. We done goofed.");
            }
        }

        return new DataTable(newColumnNames, newData);
    }

    public static boolean areSpecificElementsEqualInArrays(String[] array1, int[] array1Indices, String[] array2, int[] array2Indices){
        if(array1Indices.length != array2Indices.length){
            throw new IllegalArgumentException("array1Indices and array2Indices must be the same length arrays. Duh.");
        }

        for(int i = 0; i < array1Indices.length; i++){
            if(!array1[array1Indices[i]].equals(array2[array2Indices[i]])){
                return false;
            }
        }

        return true;
    }

    public static int[] getIndicesInArrayOfItemsEquivalentTo(String[] array, String[] itemsToFind){
        int[] columnIndices = new int[itemsToFind.length];
        for(int i = 0; i < itemsToFind.length; i++){
            columnIndices[i] = getIndexInArrayOfItemEquivalentTo(array, itemsToFind[i]);
        }

        return columnIndices;
    }

    public static int getIndexInArrayOfItemEquivalentTo(String[] array, String itemToFind){
        boolean itemFound = false;
        int foundIndex = -1;
        for(int i = 0; i < array.length; i++){
            if(array[i].equals(itemToFind)){
                if(!itemFound){
                    foundIndex = i;
                    itemFound = true;
                }
                else{
                    throw new FindException("More than one item in the array matches the itemToFind.");
                }
            }
        }

        if(itemFound){
            return foundIndex;
        }
        else{
            throw new NoSuchElementException("The item was not found in the array.");
        }
    }

    public static String[] getCombinedArrayWithoutDuplicates(String[] array1, String[] array2){
        ArrayList<String> combinedArray = new ArrayList<>(array1.length + array2.length);

        combinedArray.addAll(Arrays.asList(array1));

        for(final String currentItem : array2){
            if(!containsItemEquivalentTo(combinedArray.toArray(new String[0]), currentItem)){
                combinedArray.add(currentItem);
            }
        }

        return combinedArray.toArray(new String[0]);
    }

    public static String[] getCommonElementsInArrays(String[] array1, String[] array2){
        ArrayList<String> commonArray = new ArrayList<>(Math.min(array1.length, array2.length));

        for(String element : array1){
            if(containsItemEquivalentTo(array2, element)){
                commonArray.add(element);
            }
        }

        return commonArray.toArray(new String[0]);
    }

    public static <T> boolean containsItemEquivalentTo(T[] array, Object item){
        for(final T currentItem : array){
            if(currentItem.equals(item)){
                return true;
            }
        }

        return false;
    }

    public static boolean containsItemEquivalentTo(int[] array, int item){
        for(final int currentItem : array){
            if(currentItem == item){
                return true;
            }
        }

        return false;
    }
}
