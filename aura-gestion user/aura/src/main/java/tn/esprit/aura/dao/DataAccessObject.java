package tn.esprit.aura.dao;

import java.util.List;

/**
 * Generic data-access interface.
 * Migrated from the Aura AI-core module (Interface.DataAccessObject).
 */
public interface DataAccessObject<T> {
    void add(T item);
    List<T> getAll(int userId);
    void update(int id, String newValue);
    void delete(int id);
}
