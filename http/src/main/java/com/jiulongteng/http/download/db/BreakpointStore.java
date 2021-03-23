package com.jiulongteng.http.download.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface BreakpointStore {


    /**
     * 保存 下载的具体信息
     */
    void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo);

    /**
     * 得到下载具体信息
     */
    public List<BlockInfo> getBlockInfo(BreakpointInfo info);

    /**
     * 更新数据库中的下载信息
     */
    public void updateBlockInfo(int blockInfoId, long currentOffset);


    /**
     * 下载完成后删除数据库中的数据
     */
    public  void deleteInfo(int downloadInfoId);

    public  void saveDownloadInfo(BreakpointInfo info);

    public BreakpointInfo getDownloadInfo(String url);

    public void updateDownloadInfo(BreakpointInfo info);
}
