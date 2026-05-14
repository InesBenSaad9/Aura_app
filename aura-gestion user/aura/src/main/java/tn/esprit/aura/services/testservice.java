package tn.esprit.aura.services;

import tn.esprit.aura.entities.User;
import tn.esprit.aura.interfaces.IService;

import java.sql.SQLException;
import java.util.List;

public class testservice  implements IService<User> {
    @Override
    public void add(User user) throws SQLException {

    }

    @Override
    public void update(User user) throws SQLException {

    }

    @Override
    public void delete(int id) throws SQLException {

    }

    @Override
    public User getById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<User> getAll() throws SQLException {
        return null;
    }


    public List<User> getAllmmmm() throws SQLException {
        return null;
    }
}
