import java.io.File;
import java.io.FileNotFoundException;
import java.lang.module.FindException;
import java.util.*;
import java.util.zip.DataFormatException;

public class DataTable{
    public static final String         EQUALS       = "=";
    public static final String         LESS_THAN    = "<";
    public static final String         GREATER_THAN = ">";
    private final       List<String[]> data         = new ArrayList<>(100);
    private final       String[]       columnNames;

    public DataTable(final File file) throws FileNotFoundException, DataFormatException{
        try(Scanner scanner = new Scanner(file)){
            if(scanner.hasNextLine()){
                this.columnNames = scanner.nextLine().split(",\\s*");
            }
            else{
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
     * Cross product is creating a row for every possible combination of rows between this table and the otherTable.
     * So there will be # of rows in this table times # of rows in the otherTable = resultant # of rows.
     * @param otherTable The table to be crossed in your heart and hope to die with.
     * @return A massive, enormous, humungous, elephantine, gigantic, huge, large, great, big, ginormous, massive, mammoth, voluminous, gargantuan, tremendous, monstrous, giant, hulking, immense, colossal table
     * that is the cross product between this table and the otherTable.
     */
    public DataTable crossWith(final DataTable otherTable){
        ArrayList<String[]> newData = new ArrayList<>(this.data.size() * otherTable.data.size());

        for(int i = 0; i < this.data.size(); i++){
            newData.add(new String[this.columnNames.length + otherTable.columnNames.length]);
            for(int j = 0; j < otherTable.data.size(); j++){
                int newArrayIndex = 0;
                for(int u = 0; u < this.columnNames.length; u++, newArrayIndex++){
                    newData.get(i)[newArrayIndex] = this.data.get(i)[u];
                }
                for(int u = 0; u < otherTable.columnNames.length; u++, newArrayIndex++){
                    newData.get(i)[newArrayIndex] = otherTable.data.get(j)[u];
                }
            }
        }

        ArrayList<String> newColumnNames = new ArrayList<>(this.columnNames.length + otherTable.columnNames.length);
        newColumnNames.addAll(Arrays.asList(this.columnNames));
        newColumnNames.addAll(Arrays.asList(otherTable.columnNames));

        return new DataTable(newColumnNames.toArray(new String[0]), newData);
    }

    /**
     * Performs an intersection between this table and the otherTable. This is essentially the opposite of the MINUS operator.
     * Basically, it only keeps rows that exist in both tables (all field values must be identical).
     * @param otherTable The table to make babies with.
     * @return An intersected table with only the common rows between this table and the otherTable.
     */
    public DataTable intersectWith(final DataTable otherTable){
        if(this.columnNames.length != otherTable.columnNames.length){
            throw new IllegalArgumentException("Cannot intersect tables with different number of columns");
        }
        if(getCommonElementsInArrays(this.columnNames, otherTable.columnNames).length != this.columnNames.length){
            throw new IllegalArgumentException("Cannot intersect tables if all columns don't match exactly.");
        }

        ArrayList<String[]> newData = new ArrayList<>(this.data.size());

        for(String[] datum : this.data){
            if(this.thereExistsAnEquivalentRowIn(otherTable)){
                newData.add(datum);
            }
        }

        return new DataTable(this.columnNames, newData);
    }

    /**
     * The UNION operator simply combines the rows of data of this table and otherTable.
     * That's why, when the union's been on strike, you're down on your luck, it's tough (so tough).
     * @param otherTable The table to append to this one.
     * @return A table with all the rows from this table and the otherTable.
     */
    public DataTable unionWith(final DataTable otherTable){
//        this.data.addAll(otherTable.data);
//        return this;

        ArrayList<String[]> newData = new ArrayList<>(this.data.size() + otherTable.data.size());

        newData.addAll(this.data);
        newData.addAll(otherTable.data);

        return new DataTable(this.columnNames, newData);
    }

    /**
     * Performs the SET DIFFERENCE operator. Basically if any row in this table is equivalent (all field values are equal) to the rows in the otherTable,
     * then they will not be present in the new table after the operation is complete. Only keeps the rows in this table that are not
     * present in the otherTable. This is the best I can explain it, if you don't understand it, boo on you.
     * @param otherTable The table to subtract from this one.
     * @return A new table with the rows in the otherTable subtracted from this table.
     */
    public DataTable minus(final DataTable otherTable){
        if(this.columnNames.length != otherTable.columnNames.length){
            throw new IllegalArgumentException("Cannot subtract tables with different number of columns");
        }
        if(getCommonElementsInArrays(this.columnNames, otherTable.columnNames).length != this.columnNames.length){
            throw new IllegalArgumentException("Cannot subtract tables if all columns don't match exactly.");
        }

        ArrayList<String[]> newData = new ArrayList<>(this.data.size());

        for(String[] datum : this.data){
            if(!this.thereExistsAnEquivalentRowIn(otherTable)){
                newData.add(datum);
            }
        }

        return new DataTable(this.columnNames, newData);
    }

    /**
     * Runs a PROJECT operator on the current DataTable and projects the columns specified.
     * Automatically trims the leading and trailing whitespace from the column names passed in.
     *
     * @param columns An array of all the column names. Leading and trailing whitespace in each element is trimmed.
     * @return A new DataTable with just the specified columns.
     */
    public DataTable project(final String[] columns){
        int[] columnIndices = getIndicesInArrayOfItemsEquivalentTo(this.columnNames, columns);

        ArrayList<String[]> newData = new ArrayList<>(this.data.size());
        for(int i = 0; i < this.data.size(); i++){
            newData.add(new String[columnIndices.length]);
            for(int j = 0; j < columnIndices.length; j++){
                newData.get(i)[j] = this.data.get(i)[columnIndices[j]];
            }
        }

        return new DataTable(columns, newData);
    }

    /**
     * @param whereClause Single condition as String, supporting >, <, and =.
     * @return A new DataTable with a WHERE clause applied to the current one.
     */
    public DataTable selectWhere(final String whereClause){
        char numberOfOperators = 0;
        String operator = "";
        if(whereClause.contains(GREATER_THAN)){
            numberOfOperators++;
            operator = GREATER_THAN;
        }
        if(whereClause.contains(LESS_THAN)){
            numberOfOperators++;
            operator = LESS_THAN;
        }
        if(whereClause.contains(EQUALS)){
            numberOfOperators++;
            operator = EQUALS;
        }

        if(numberOfOperators > 1){
            throw new IllegalArgumentException("This WHERE clause is malformed. More than one operator was detected.");
        }
        else if(numberOfOperators < 1){
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
     * @param otherTable The table to join with.
     * @return A new DataTable that does a full natural join on this table and the otherTable.
     * @throws DataFormatException if weird shit goes down.
     */
    public DataTable joinWith(final DataTable otherTable) throws DataFormatException{
        String[] sharedColumnNames = getCommonElementsInArrays(this.columnNames, otherTable.columnNames);
        String[] newColumnNames = getCombinedArrayWithoutDuplicates(this.columnNames, otherTable.columnNames);

        int[] thisTableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(this.columnNames, sharedColumnNames);
        int[] otherTableSharedColumnIndices = getIndicesInArrayOfItemsEquivalentTo(otherTable.columnNames, sharedColumnNames);

        int maxNumberOfMatchingRows = Math.min(this.data.size(), otherTable.data.size());
        ArrayList<int[]> listOfMatchingIndices = new ArrayList<>(maxNumberOfMatchingRows);

        for(int i = 0; i < this.data.size(); i++){
            for(int j = 0; j < otherTable.data.size(); j++){
                if(areSpecificElementsEqualInArrays(this.data.get(i), thisTableSharedColumnIndices, otherTable.data.get(j), otherTableSharedColumnIndices)){
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
                    newData.get(i)[newRowColumnIndex] = this.data.get(matchingIndices[i][0])[j];
                    newRowColumnIndex++;
                }
            }
            for(int j = 0; j < thisTableSharedColumnIndices.length; j++){ // Add shared items
                if(!this.data.get(matchingIndices[i][0])[thisTableSharedColumnIndices[j]].equals(otherTable.data.get(matchingIndices[i][1])[otherTableSharedColumnIndices[j]])){
                    throw new DataFormatException("Developer is an idiot, as fields that were indicated as matching for merging are clearly not matching.");
                }

                newData.get(i)[newRowColumnIndex] = this.data.get(matchingIndices[i][0])[thisTableSharedColumnIndices[j]];
                newRowColumnIndex++;
            }

            for(int j = 0; j < otherTableNumberOfUniqueColumns; j++){ // Add third table unique items
                if(!containsItemEquivalentTo(otherTableSharedColumnIndices, j)){
                    newData.get(i)[newRowColumnIndex] = otherTable.data.get(matchingIndices[i][1])[j];
                    newRowColumnIndex++;
                }
            }

            if(newRowColumnIndex != (newColumnNames.length - 1)){
                throw new DataFormatException("We obviously added the wrong number of fields to this row. We done goofed.");
            }
        }

        return new DataTable(newColumnNames, newData);
    }

    public boolean thereExistsAnEquivalentRowIn(final DataTable otherTable){
        for(int i = 0; i < otherTable.data.size(); i++){
            boolean isRowEquivalent = true;
            for(int j = 0; j < this.columnNames.length; j++){
                if(!this.data.get(i)[j].equals(otherTable.data.get(i)[j])){
                    isRowEquivalent = false;
                    break;
                }
            }

            if(isRowEquivalent){
                return true;
            }
        }

        return false;
    }

    public static boolean areSpecificElementsEqualInArrays(final String[] array1, final int[] array1Indices, final String[] array2, final int[] array2Indices){
        if(array1Indices.length != array2Indices.length){
            //noinspection GrazieInspection
            throw new IllegalArgumentException("array1Indices and array2Indices must be the same length arrays. Duh.");
        }

        for(int i = 0; i < array1Indices.length; i++){
            if(!array1[array1Indices[i]].equals(array2[array2Indices[i]])){
                return false;
            }
        }

        return true;
    }

    public static int[] getIndicesInArrayOfItemsEquivalentTo(final String[] array, final String[] itemsToFind){
        int[] columnIndices = new int[itemsToFind.length];
        for(int i = 0; i < itemsToFind.length; i++){
            columnIndices[i] = getIndexInArrayOfItemEquivalentTo(array, itemsToFind[i].trim());
        }

        return columnIndices;
    }

    public static int getIndexInArrayOfItemEquivalentTo(final String[] array, final String itemToFind){
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
            throw new NoSuchElementException("The item " + itemToFind + " was not found in the array " + Arrays.toString(array) + ".");
        }
    }

    public static String[] getCombinedArrayWithoutDuplicates(final String[] array1, final String[] array2){
        ArrayList<String> combinedArray = new ArrayList<>(array1.length + array2.length);

        combinedArray.addAll(Arrays.asList(array1));

        for(final String currentItem : array2){
            if(!containsItemEquivalentTo(combinedArray.toArray(new String[0]), currentItem)){
                combinedArray.add(currentItem);
            }
        }

        return combinedArray.toArray(new String[0]);
    }

    public static String[] getCommonElementsInArrays(final String[] array1, final String[] array2){
        ArrayList<String> commonArray = new ArrayList<>(Math.min(array1.length, array2.length));

        for(String element : array1){
            if(containsItemEquivalentTo(array2, element)){
                commonArray.add(element);
            }
        }

        return commonArray.toArray(new String[0]);
    }

    public static <T> boolean containsItemEquivalentTo(final T[] array, final Object item){
        for(final T currentItem : array){
            if(currentItem.equals(item)){
                return true;
            }
        }

        return false;
    }

    public static boolean containsItemEquivalentTo(final int[] array, final int item){
        for(final int currentItem : array){
            if(currentItem == item){
                return true;
            }
        }

        return false;
    }
}
