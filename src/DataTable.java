import com.sun.source.tree.LambdaExpressionTree;

import java.lang.module.FindException;
import java.util.*;
import java.util.zip.DataFormatException;

public class DataTable{
    private final List<String[]> data = new ArrayList<>(100);
    private       String[]       columnNames;

    public DataTable(){

    }

    public DataTable(String[] columnNames){
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

    public void joinWith(DataTable otherTable) throws DataFormatException{
        String[] sharedColumnNames = getCommonElementsInArrays(this.columnNames, otherTable.columnNames);
        String[] newColumnNames = getCombinedArrayWithoutDuplicates(this.columnNames, otherTable.columnNames);

        int[] thisTableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(this.columnNames, sharedColumnNames);
        int[] otherTableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(otherTable.columnNames, sharedColumnNames);

        int maxNumberOfMatchingRows = Math.min(this.data.size(), otherTable.data.size());
        ArrayList<int[]> listOfMatchingIndices = new ArrayList<>(maxNumberOfMatchingRows);

        for(int i = 0; i < this.data.size(); i++){
            for(int j = 0; j < otherTable.data.size(); j++){
                if(areSpecificElementsEqualInArrays(this.data.get(i), thisTableSharedColumnIndices, otherTable.data.get(i), otherTableSharedColumnIndices)){
                    listOfMatchingIndices.add(new int[]{i, j});
                }
            }
        }

        int[][] matchingIndices = listOfMatchingIndices.toArray(new int[0][]);
        ArrayList<String[]> newData = new ArrayList<>(Math.max(this.data.size(), otherTable.data.size()));

        int thisTableNumberOfUniqueColumns = this.columnNames.length - sharedColumnNames.length, otherTableNumberOfUniqueColumns = otherTable.columnNames.length - sharedColumnNames.length;
        for(int i = 0; i < matchingIndices.length; i++){
            newData.add(new String[newColumnNames.length]);


            int newRowColumnIndex = 0;
            for(int j = 0; j < thisTableNumberOfUniqueColumns; j++){ // Add items from first table (except shared columns)
                if(!containsItemEquivalentTo(thisTableSharedColumnIndices, j)){
                    newData.get(i)[newRowColumnIndex] = this.data.get(i)[j];
                    newRowColumnIndex++;
                }
            }
            for(int j = 0; j < thisTableSharedColumnIndices.length; j++){ // Add shared items
                if(!this.data.get(i)[thisTableSharedColumnIndices[j]].equals(otherTable.data.get(i)[otherTableSharedColumnIndices[j]])){
                    throw new DataFormatException("Developer is an idiot, as fields that were indicated as matching for merging are clearly not matching.");
                }

                newData.get(i)[newRowColumnIndex] = this.data.get(i)[thisTableSharedColumnIndices[j]];
                newRowColumnIndex++;
            }

            for(int j = 0; j < otherTableNumberOfUniqueColumns; j++){ // Add third table unique items
                if(!containsItemEquivalentTo(otherTableSharedColumnIndices, j)){
                    newData.get(i)[newRowColumnIndex] = this.data.get(i)[j];
                    newRowColumnIndex++;
                }
            }

            if(newRowColumnIndex != (newColumnNames.length - 1)){
                throw new DataFormatException("We obviously added the wrong number of fields to this row. We done goofed.");
            }
        }

        this.data.clear();
        this.data.addAll(newData);

        this.columnNames = newColumnNames;
    }

    public static boolean areSpecificElementsEqualInArrays(String[] array1, int[] array1Indices, String[] array2, int[] array2Indices){
        if(array1Indices.length != array2Indices.length){
            throw new IllegalArgumentException("array1Indices and array2Indices must be the same length arrays. Duh.");
        }

        for(int i = 0; i < array1Indices.length; i++){
            if(array1[array1Indices[i]] != array2[array2Indices[i]]){
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
