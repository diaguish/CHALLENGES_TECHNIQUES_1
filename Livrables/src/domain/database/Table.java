package domain.database;

public interface Table {
    String getName();
    ArrayList<Column> getColumns();
}