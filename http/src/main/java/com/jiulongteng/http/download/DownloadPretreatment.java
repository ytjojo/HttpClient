package com.jiulongteng.http.download;

import android.os.StatFs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.DownloadException;
import com.jiulongteng.http.download.cause.ResumeFailedCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.entry.BreakpointInfo;
import com.jiulongteng.http.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadPretreatment {
    private DownloadTask task;
    boolean resumable;

    public DownloadPretreatment(DownloadTask task) {
        this.task = task;
    }


    private void restore() {

        BreakpointInfo breakpointInfo = DownloadCache.getInstance().loadDownloadInfo(task.getUrl());
        if (breakpointInfo == null) {
            breakpointInfo = new BreakpointInfo(-1, task.getUrl(), null, task.getParentFile(),
                    task.getFilename(), task.isFilenameFromResponse());
            DownloadCache.getInstance().saveDownloadInfo(breakpointInfo);

        }else {
            DownloadCache.getInstance().loadBlockInfo(breakpointInfo);
        }
        task.setInfo(breakpointInfo);


    }


    public void execute() throws IOException {
        restore();
        if (task.isStoped()) {
            task.dispatchCancel();
            return;
        }
        task.getCallbackDispatcher().connectTrialStart(task);
        if(!DownloadCache.getInstance().isNetPolicyValid(task)){
            throw new DownloadException(DownloadException.NETWORK_POLICY_ERROR,"invalid network state");
        }
        executeTrial();
        if (task.isStoped()) {
            task.dispatchCancel();
            return;
        }
        localCheck();

    }

    private void localCheck() throws IOException {

        if (resumable) {
            boolean isExists = task.getFile().exists();
            boolean isCorrect = BreakpointInfo.isCorrect(task.getInfo(), task);
            boolean dirty = !isCorrect || !isExists;
            if (dirty) {
                if (task.getFile() != null && task.getFile().exists() && !task.getFile().delete()) {
                    throw new IOException("Delete file failed!");
                }
                Util.assembleBlock(task);
                DownloadCache.getInstance().saveBlockInfo(task.getInfo().getBlockInfoList(),task.getInfo());

            } else {
            }


        } else {
            if (task.getFile() != null && !task.getFile().delete()) {
                throw new IOException("Delete file failed!");
            }
            Util.assembleBlock(task);
            DownloadCache.getInstance().saveBlockInfo(task.getInfo().getBlockInfoList(),task.getInfo());
        }

        if (!task.getParentFile().exists()) {
            task.getParentFile().mkdirs();
        }
        if (!task.getFile().exists()) {
            task.getFile().createNewFile();
        }

        final long totalLength = task.getInfo().getTotalLength();
        boolean isFileScheme = true;
        if (isFileScheme && task.getInfo().isChunked()) {
            final File file = task.getFile();
            final long requireSpace = totalLength - file.length();
            if (requireSpace > 0) {
                StatFs statFs =  new StatFs(file.getAbsolutePath()) ;
                final long freeSpace = Util.getFreeSpaceBytes(statFs);
                if (freeSpace < requireSpace) {
                    throw new DownloadException(DownloadException.PROTOCOL_ERROR, "There is Free space less than Require space: " + freeSpace + " < " + requireSpace);
                }
                DownloadUtils.createFile(file,totalLength);
            }
        } else {
        }
    }

    private void executeTrial() throws IOException {

        boolean isNeedTrialHeadMethod;
        Response response = null;
        try {
            Request request = task.getRawRequest().newBuilder().header(Util.RANGE, "bytes=0-0").build();
            if (!Util.isEmpty(task.getInfo().getEtag())) {
                request = task.getRawRequest().newBuilder().header(Util.IF_MATCH, task.getInfo().getEtag()).build();
            }
            response = task.getClient().newCall(request).execute();
            Headers headers = response.headers();
            task.setResponseCode(response.code());
            task.setResponseEtag(headers.get(Util.ETAG));
            task.getInfo().setChunked(isChunked());
            task.setAcceptRange(DownloadUtils.isAcceptRange(headers, response.code()));
            task.setInstanceLength(DownloadUtils.findInstanceLength(response.headers()));
            task.setResponseEtag(DownloadUtils.findEtag(headers));
            task.setResponseFilename(DownloadUtils.findFilename(headers));

            isNeedTrialHeadMethod = DownloadUtils.isNeedTrialHeadMethodForInstanceLength(task.getInstanceLength(),
                    headers);
            task.setRedirectLocation(getRedirectLocation(response));

        } finally {
            okhttp3.internal.Util.closeQuietly(response);
        }
        if (isNeedTrialHeadMethod) {
            Request request = task.getRawRequest();
            if (!Util.isEmpty(task.getInfo().getEtag())) {
                request = task.getRawRequest().newBuilder().header(Util.IF_MATCH, task.getInfo().getEtag()).build();
            }
            task.setInstanceLength(DownloadUtils.trialHeadMethodForInstanceLength(task.getClient(), request));
        }
        if (TextUtils.isEmpty(task.getFileName())) {
            String fileName = DownloadUtils.determineFilename(task.getResponseFilename(), task.getUrl());
            task.setFileName(fileName);

        }

        final ResumeFailedCause resumeFailedCause = getPreconditionFailedCause(task.getResponseCode(), task.getInfo().getTotalOffset() != 0, task.getInfo(),
                task.getResponseEtag());
        resumable = resumeFailedCause == null;
        task.getInfo().setEtag(task.getResponseEtag());
        if (!isTrialSpecialPass(task.getResponseCode(), task.getInstanceLength(), resumable)
                && isServerCanceled(task.getResponseCode(), false )) {
            throw new DownloadException(DownloadException.SERVER_CANCEL_ERROR, "trial exception code = " + task.getResponseCode());
        }
    }

    public boolean isChunked() {
        return task.getInstanceLength() == Util.CHUNKED_CONTENT_LENGTH;
    }

    public String getRedirectLocation(Response response) {
        final Response priorRes = response.priorResponse();
        if (priorRes != null
                && response.isSuccessful()
                && RedirectUtil.isRedirect(priorRes.code())) {
            // prior response is a redirect response, so current response
            // has redirect location
            return response.request().url().toString();
        }
        return null;
    }

    boolean isTrialSpecialPass(int responseCode, long instanceLength, boolean isResumable) {
        if (responseCode == Util.RANGE_NOT_SATISFIABLE && instanceLength >= 0 && isResumable) {
            // provide valid instance-length & resumable but backend response wrong code 416
            // for the range:0-0, because of values on response header is valid we pass it.
            return true;
        }

        return false;
    }

    public static boolean isServerCanceled(int responseCode, boolean isAlreadyProceed) {
        if (responseCode != HttpURLConnection.HTTP_PARTIAL
                && responseCode != HttpURLConnection.HTTP_OK) {
            return true;
        }

        if (responseCode == HttpURLConnection.HTTP_OK && isAlreadyProceed) {
            return true;
        }

        return false;
    }


    public static ResumeFailedCause getPreconditionFailedCause(int responseCode,
                                                               boolean isAlreadyProceed,
                                                               @NonNull BreakpointInfo info,
                                                               @Nullable String responseEtag) {
        final String localEtag = info.getEtag();
        if (responseCode == HttpURLConnection.HTTP_PRECON_FAILED) {
            return ResumeFailedCause.RESPONSE_PRECONDITION_FAILED;
        }

        if (!Util.isEmpty(localEtag) && !Util.isEmpty(responseEtag) && !responseEtag
                .equals(localEtag)) {
            // etag changed.
            // also etag changed is relate to HTTP_PRECON_FAILED
            return ResumeFailedCause.RESPONSE_ETAG_CHANGED;
        }

        if (responseCode == HttpURLConnection.HTTP_CREATED && isAlreadyProceed) {
            // The request has been fulfilled and has resulted in one or more new resources
            // being created.
            // mark this case is precondition failed for
            // 1. checkout whether accept partial
            // 2. 201 means new resources so range must be from beginning otherwise it can't
            // match local range.
            return ResumeFailedCause.RESPONSE_CREATED_RANGE_NOT_FROM_0;
        }

        if (responseCode == HttpURLConnection.HTTP_RESET && isAlreadyProceed) {
            return ResumeFailedCause.RESPONSE_RESET_RANGE_NOT_FROM_0;
        }

        return null;
    }
}
