package com.jiulongteng.http.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jiulongteng.http.download.Util;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 一个业务类
 */
public class Dao implements BreakpointStore {
    private static volatile Dao instance = null;
    private Context mContext;

    private Dao(Context mContext) {
        this.mContext = mContext;
    }

    public static void init(Context c) {
        instance = new Dao(c);
    }

    private static volatile DBHelper sDBHelper;

    public static Dao getInstance() {
        if (instance == null) {
            new IllegalArgumentException("init(Context c");
        }
        return instance;
    }

    private SQLiteDatabase getDatabase() {
        if (sDBHelper == null) {
            synchronized (Dao.class) {
                if (sDBHelper == null) {
                    try {
                        sDBHelper = new DBHelper(mContext);
                    } catch (Exception e) {
                    }
                }
            }
        }

        return sDBHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        if (sDBHelper != null && sDBHelper.getWritableDatabase().isOpen()) {
            // Closing database
            sDBHelper.getWritableDatabase().close();
        }
    }

    /**
     * 查看数据库中是否有数据
     */
    public boolean isContainBlockInfo(int downloadInfoId) {
        SQLiteDatabase database = getDatabase();
        int count = -1;
        Cursor cursor = null;
        try {
            String sql = "select count(*)  from block_info where download_info_id=?";
            cursor = database.rawQuery(sql, new String[]{downloadInfoId + ""});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != cursor) {
                cursor.close();
            }
        }
        return count != 0;
    }

    public int getMaxDownloadId() {
        SQLiteDatabase database = getDatabase();
        int id = -1;
        Cursor cursor = null;
        try {
            String sql = "select max(_id) _id from download_info limit 1";
            cursor = database.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != cursor) {
                cursor.close();
            }
        }
        return id;
    }


    public boolean isContainDownloadInfo(String url) {
        SQLiteDatabase database = getDatabase();
        int count = -1;
        Cursor cursor = null;
        try {
            String sql = "select count(*)  from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{url});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != cursor) {
                cursor.close();
            }
        }
        return count == 0;
    }

    /**
     * 保存 下载的具体信息
     */
    @Override
    public void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo) {
        SQLiteDatabase database = getDatabase();
        database.beginTransaction();
        try {
            database.delete("block_info", "download_info_id=?", new String[]{breakpointInfo.getId() + ""});
            for (BlockInfo info : blockInfo) {
                String sql = "insert into block_info(start_pos,current_offset,content_length,download_info_id) values (?,?,?,?)";
                Object[] bindArgs = {info.getStartOffset(),
                        info.getCurrentOffset(), info.getContentLength(),
                        breakpointInfo.getId()};
                database.execSQL(sql, bindArgs);
            }
            Dao.getInstance().loadBlockInfo(breakpointInfo);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();

        }
    }

    /**
     * 得到下载进度信息
     */
    @Override
    public List<BlockInfo> loadBlockInfo(BreakpointInfo info) {
        List<BlockInfo> list = new ArrayList<BlockInfo>();
        SQLiteDatabase database = getDatabase();
        Cursor cursor = null;
        try {
            String sql = "select _id, start_pos,content_length,current_offset from block_info where download_info_id=?";
            cursor = database.rawQuery(sql, new String[]{info.getId() + ""});
            while (cursor.moveToNext()) {
                BlockInfo blockInfo = new BlockInfo(cursor.getInt(0),
                        cursor.getLong(1), cursor.getLong(2), cursor.getLong(3));
                list.add(blockInfo);
                Util.i("BlockInfo",blockInfo.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != cursor) {
                cursor.close();
            }
        }
        info.resetBlockInfos();
        info.addAllBlockInfo(list);
        return list;
    }

    /**
     * 得到下载进度信息
     */
    public List<BreakpointInfo> loadAllDownloadInfo() {
        List<BreakpointInfo> list = new ArrayList<BreakpointInfo>();
        HashMap<Integer, ArrayList<BlockInfo>> allBlocks = new HashMap<>();
        SQLiteDatabase database = getDatabase();
        Cursor blockCursor = null;
        Cursor breakpointInfoCursor = null;
        try {
            String sql = "select _id, start_pos,content_length,current_offset,download_info_id from block_info";
            blockCursor = database.rawQuery(sql, null);
            while (blockCursor.moveToNext()) {
                int downloadInfoId = blockCursor.getInt(4);
                ArrayList<BlockInfo> blockInfos = allBlocks.get(downloadInfoId);
                if (blockInfos == null) {
                    blockInfos = new ArrayList<>();
                    allBlocks.put(downloadInfoId, blockInfos);
                }
                BlockInfo blockInfo = new BlockInfo(blockCursor.getInt(0),
                        blockCursor.getLong(1), blockCursor.getLong(2), blockCursor.getLong(3));
                blockInfos.add(blockInfo);
            }

            sql = "select _id, url,etag,parent_dir_path, file_name, ask_only_parent_path from download_info";
            breakpointInfoCursor = database.rawQuery(sql, null);
            while (blockCursor.moveToNext()) {
                BreakpointInfo info = new BreakpointInfo(breakpointInfoCursor.getInt(0),
                        breakpointInfoCursor.getString(1), breakpointInfoCursor.getString(2), new File(breakpointInfoCursor.getString(3)), breakpointInfoCursor.getString(4), breakpointInfoCursor.getInt(5) == 1);
                list.add(info);
            }
            for (BreakpointInfo info : list) {
                info.addAllBlockInfo(allBlocks.get(info.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (null != blockCursor) {
                blockCursor.close();
            }
            if (null != breakpointInfoCursor) {
                breakpointInfoCursor.close();
            }
        }
        return list;
    }

    /**
     * 更新数据库中的下载信息
     */
    @Override
    public void updateBlockInfo(int blockInfoId, long currentOffset) {
        SQLiteDatabase database = getDatabase();
        try {
            String sql = "update block_info set current_offset=? where _id =?";
            Object[] bindArgs = {currentOffset, blockInfoId};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


    /**
     * 下载完成后删除数据库中的数据
     */
    public void deleteInfo(int downloadInfoId) {
        SQLiteDatabase database = getDatabase();
        database.beginTransaction();
        try {
            database.delete("download_info", "_id=?", new String[]{downloadInfoId + ""});
            database.delete("block_info", "download_info_id=?", new String[]{downloadInfoId + ""});
            database.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public void saveDownloadInfo(BreakpointInfo info) {
        SQLiteDatabase database = getDatabase();
        Cursor cursor = null;
        try {
            String sql = "insert into download_info(url,etag,parent_dir_path, file_name, ask_only_parent_path, chunked) values (?,?,?,?,?,?)";
            Object[] bindArgs = {info.getUrl(), info.getEtag(),
                    info.getParentFile().getAbsoluteFile(), info.getFilename(),
                     info.isTaskOnlyProvidedParentPath() ? 1 : 0, info.isChunked() ? 1 : 0};
            database.execSQL(sql, bindArgs);
            String selectSQL = "select _id from download_info where url=?";
            cursor = database.rawQuery(selectSQL, new String[]{info.getUrl()});
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                info.setId(id);
            } else {
                throw new SQLiteException("insert block " + info + " failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
    }


    public BreakpointInfo loadDownloadInfo(String url) {
        SQLiteDatabase database = getDatabase();
        Cursor cursor = null;
        try {
            String sql = "select _id, url,etag,parent_dir_path, file_name, ask_only_parent_path from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{url});
            while (cursor.moveToNext()) {
                BreakpointInfo info = new BreakpointInfo(cursor.getInt(0),
                        cursor.getString(1), cursor.getString(2), new File(cursor.getString(3)), cursor.getString(4), cursor.getInt(5) == 1);
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return null;
    }


    public void updateDownloadInfo(BreakpointInfo info) {
        SQLiteDatabase database = getDatabase();
        try {
            String sql = "update download_info set file_name=?,etag=?,chunked=? where _id =?";
            Object[] bindArgs = {info.getFilename(), info.getEtag(), info.isChunked() ? 1 : 0, info.getId()};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


}  