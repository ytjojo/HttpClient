package com.ytjojo.domin.database;

import java.util.List;

/**
 * Created by Administrator on 2016/3/29 0029.
 */
public interface DatabaseRepository<T> {
        void save(T model);
        void saveOrUpdate(T model);
        void saveList(List<T> list);
        void delete(T model);
        void clearAll();
        T  queryByName(String name);
        T queryByDataBaseId(long id);
        T queryByBussId(String id);
        T queryByBussId(int id);
        int queryDataCount();
        List<T> queryAll();
        List<T> query(String where, String... selectionArg);
        boolean isContain(T model);
        boolean isExpired();
        boolean isExpired(T model);
        void setageMillis(long ageMillis);


}
