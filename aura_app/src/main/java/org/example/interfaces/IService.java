package org.example.interfaces;

import java.sql.SQLException;
import java.util.ArrayList;

public interface IService<T> {
    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    ArrayList<T> afficherAll() throws SQLException;
    T chercherParId(int id) throws SQLException;
}