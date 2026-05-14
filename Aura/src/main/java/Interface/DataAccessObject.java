package Interface;

import java.util.List;

public interface DataAccessObject<T> {
    void add(T item);
    List<T> getAll(int userId);
    void update(int id, String newValue);
    void delete(int id);
}