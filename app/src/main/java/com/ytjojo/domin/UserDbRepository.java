package com.ytjojo.domin;

import com.ytjojo.domin.database.DatabaseRepository;
import com.ytjojo.domin.vo.User;

import java.util.List;

/**
 * Created by Administrator on 2016/3/31 0031.
 */
public class UserDbRepository implements DatabaseRepository<User> {
    @Override
    public void save(User model) {

    }

    @Override
    public void saveOrUpdate(User model) {

    }

    @Override
    public void saveList(List<User> list) {

    }

    @Override
    public void delete(User model) {

    }

    @Override
    public void clearAll() {

    }

    @Override
    public User queryByName(String name) {
        return null;
    }

    @Override
    public User queryByDataBaseId(long id) {
        return null;
    }

    @Override
    public User queryByBussId(String id) {
        return null;
    }

    @Override
    public User queryByBussId(int id) {
        return null;
    }

    @Override
    public int queryDataCount() {
        return 0;
    }

    @Override
    public List<User> queryAll() {
        return null;
    }

    @Override
    public List<User> query(String where, String... selectionArg) {
        return null;
    }

    @Override
    public boolean isContain(User model) {
        return false;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isExpired(User model) {
        return false;
    }

    @Override
    public void setageMillis(long ageMillis) {

    }
}
