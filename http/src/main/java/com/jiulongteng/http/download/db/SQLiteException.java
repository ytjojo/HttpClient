package com.jiulongteng.http.download.db;

import java.io.IOException;

public class SQLiteException extends IOException {
    public SQLiteException(String message) {
        super(message);
    }
}