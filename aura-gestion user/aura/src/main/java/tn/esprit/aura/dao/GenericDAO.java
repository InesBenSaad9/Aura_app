package tn.esprit.aura.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    T insert(T entity);
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> findAll(int limit, int offset);
    boolean exists(ID id);
    int count();
    T update(T entity);
    boolean deleteById(ID id);
    boolean delete(T entity);
    int deleteAll();
    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();
    void close();
}
