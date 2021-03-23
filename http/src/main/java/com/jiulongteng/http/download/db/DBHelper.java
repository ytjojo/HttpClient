package com.jiulongteng.http.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 建立一个数据库帮助类
 */
public class DBHelper extends SQLiteOpenHelper {
    //download.db-->数据库名
    public DBHelper(Context context) {
        super(context, "download.db", null, 1);
    }

    /**
     * 在download.db数据库下创建一个download_info表存储下载信息
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if  not exists  block_info(_id integer PRIMARY KEY AUTOINCREMENT, "
                + "start_pos integer, current_offset  integer,content_length integer,download_info_id integer)");
        db.execSQL("create table if  not exists  download_info(_id integer PRIMARY KEY AUTOINCREMENT, etag char ,url char NOT NULL , "
                + "parent_dir_path char NOT NULL, file_name char, ask_only_parent_path  TINYINT(1) DEFAULT 0,chunked TINYINT(1) DEFAULT 0)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS download_info");
//        onCreate(db);
    }

}