/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiulongteng.http.download.entry;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.util.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BreakpointInfo {
    int id;
    private final String url;
    private String etag;

    @NonNull
    final File parentFile;
    @Nullable
    private File targetFile;

    private String fileName;
    private final List<BlockInfo> blockInfoList;
    private final boolean taskOnlyProvidedParentPath;
    private boolean chunked;

    private String md5Code;

    public BreakpointInfo(int id, @NonNull String url,String etag, @NonNull File parentFile,
                          @Nullable String filename) {
        this(id,url,etag,parentFile,filename,!TextUtils.isEmpty(filename));
    }

    public BreakpointInfo(int id, @NonNull String url,String etag, @NonNull File parentFile,
                   @Nullable String filename, boolean taskOnlyProvidedParentPath) {
        this.id = id;
        this.url = url;
        this.parentFile = parentFile;
        this.blockInfoList = new ArrayList<>();

        if (TextUtils.isEmpty(filename)) {
        } else {
            this.fileName = filename;
            targetFile = new File(parentFile, filename);
        }
        this.setEtag(etag);
        this.taskOnlyProvidedParentPath = taskOnlyProvidedParentPath;
    }

    public int getId() {
        return id;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void addBlock(BlockInfo blockInfo) {
        this.blockInfoList.add(blockInfo);
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public boolean isLastBlock(int blockIndex) {
        return blockIndex == blockInfoList.size() - 1;
    }

    public boolean isSingleBlock() {
        return blockInfoList.size() == 1;
    }

    public boolean isTaskOnlyProvidedParentPath() {
        return taskOnlyProvidedParentPath;
    }

    public BlockInfo getBlock(int blockIndex) {
        return blockInfoList.get(blockIndex);
    }

    public void resetInfo() {
        this.blockInfoList.clear();
        this.etag = null;
    }

    public void resetBlockInfos() {
        this.blockInfoList.clear();
    }

    public int getBlockCount() {
        return blockInfoList.size();
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public long getTotalOffset() {
        long offset = 0;
        final Object[] blocks = blockInfoList.toArray();
        if (blocks != null) {
            for (Object block : blocks) {
                if (block instanceof BlockInfo) {
                    offset += ((BlockInfo) block).getCurrentOffset();
                }
            }
        }
        return offset;
    }

    public long getTotalLength() {
        if (isChunked()) return getTotalOffset();

        long length = 0;
        final Object[] blocks = blockInfoList.toArray();
        if (blocks != null) {
            for (Object block : blocks) {
                if (block instanceof BlockInfo) {
                    length += ((BlockInfo) block).getContentLength();
                }
            }
        }

        return length;
    }

    public @Nullable
    String getEtag() {
        return this.etag;
    }

    public String getUrl() {
        return url;
    }

    @Nullable public String getFilename() {
        return fileName;
    }


    @Nullable public File getFile() {
        if (fileName == null) return null;
        if (targetFile == null) targetFile = new File(parentFile, fileName);

        return targetFile;
    }

    public BreakpointInfo copy() {
        final BreakpointInfo info = new BreakpointInfo(id, url,etag, parentFile, fileName,
                taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public BreakpointInfo copyWithReplaceId(int replaceId) {
        final BreakpointInfo info = new BreakpointInfo(replaceId, url,etag, parentFile,
                fileName, taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public void reuseBlocks(BreakpointInfo info) {
        blockInfoList.clear();
        blockInfoList.addAll(info.blockInfoList);
    }

    /**
     * You can use this method to replace url for using breakpoint info from another task.
     */
    public BreakpointInfo copyWithReplaceIdAndUrl(int replaceId, String newUrl) {
        final BreakpointInfo info = new BreakpointInfo(replaceId, newUrl,etag, parentFile,
                fileName, taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public boolean isSameFrom(DownloadTask task) {
        if (!parentFile.equals(task.getParentFile())) {
            return false;
        }

        if (!url.equals(task.getUrl())) return false;

        final String otherFilename = task.getFilename();
        if (otherFilename != null && otherFilename.equals(fileName)) return true;

        if (taskOnlyProvidedParentPath) {
            // filename is provided by response.
            if (!task.isFilenameFromResponse()) return false;

            return otherFilename == null || otherFilename.equals(fileName);
        }

        return false;
    }

    @Override public String toString() {
        return "id[" + id + "]" + " url[" + url + "]" + " etag[" + etag + "]"
                + " taskOnlyProvidedParentPath[" + taskOnlyProvidedParentPath + "]"
                + " parent path[" + parentFile + "]" + " filename[" + fileName + "]"
                + " block(s):" + blockInfoList.toString();
    }


    public static boolean isCorrect(BreakpointInfo info,DownloadTask task){
        final int blockCount = info.getBlockCount();

        if (blockCount <= 0) return false;
        if (info.isChunked()) return false;
        if (info.getFile() == null) return false;
        final File fileOnTask = task.getFile();
        if (!info.getFile().equals(fileOnTask)) return false;
        if (info.getFile().length() > info.getTotalLength()) return false;

        if (task.getInstanceLength() > 0 && info.getTotalLength() != task.getInstanceLength()) {
            return false;
        }

        for (int i = 0; i < blockCount; i++) {
            BlockInfo blockInfo = info.getBlock(i);
            if (blockInfo.getContentLength() <= 0) return false;
        }

        return true;
    }

    @NonNull
    public File getParentFile() {
        return parentFile;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addAllBlockInfo(List<BlockInfo> blockInfos){
        this.blockInfoList.addAll(blockInfos);
    }

    public List<BlockInfo> getBlockInfoList() {
        return blockInfoList;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5Code() {
        return md5Code;
    }

    public void setMd5Code(String md5Code) {
        this.md5Code = md5Code;
    }
}
