package com.jiulongteng.http.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.DownloadException;
import com.jiulongteng.http.download.cause.ResumeFailedCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.entry.BreakpointInfo;
import com.jiulongteng.http.util.TextUtils;

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

    private String mineType;


    private void restore() {
        BreakpointInfo breakpointInfo = DownloadCache.getInstance().loadDownloadInfo(task.getUrl());
        if (breakpointInfo == null) {

        } else {
            DownloadCache.getInstance().loadBlockInfo(breakpointInfo);
        }
        task.setInfo(breakpointInfo);

    }

    private void createNewInfo() throws IOException {
        task.getTargetProvider().setFileName(task.getFileName());
        if (task.getTargetProvider().getParentFile() == null) {
            task.getTargetProvider().createNewTarget(mineType);
        }
        BreakpointInfo breakpointInfo = new BreakpointInfo(-1, task.getUrl(), task.getResponseEtag(), task.getTargetProvider().getParentFile(),
                task.getFilename(), task.isFilenameFromResponse());
        DownloadCache.getInstance().saveDownloadInfo(breakpointInfo);
        task.setInfo(breakpointInfo);
    }


    public void execute() throws IOException {
        restore();
        if (task.isStoped()) {
            task.dispatchCancel();
            return;
        }
        task.getCallbackDispatcher().connectTrialStart(task);
        if (!DownloadCache.getInstance().isNetPolicyValid(task)) {
            throw new DownloadException(DownloadException.NETWORK_POLICY_ERROR, "invalid network state");
        }
        executeTrial();
        if (task.isStoped()) {
            task.dispatchCancel();
            return;
        }
        localCheck();

    }

    private void localCheck() throws IOException {
        boolean isExists = task.getTargetProvider().isExists();
        if (resumable) {
            boolean isCorrect = BreakpointInfo.isCorrect(task.getInfo(), task);
            boolean dirty = !isCorrect || !isExists;
            if (dirty) {
                if (isExists && !task.getTargetProvider().delete()) {
                    throw new IOException("Delete file failed!");
                }
                Util.assembleBlock(task);
                DownloadCache.getInstance().saveBlockInfo(task.getInfo().getBlockInfoList(), task.getInfo());
            }

        } else {
            if (isExists && !task.getTargetProvider().delete()) {
                throw new IOException("Delete file failed!");
            }
            Util.assembleBlock(task);
            DownloadCache.getInstance().saveBlockInfo(task.getInfo().getBlockInfoList(), task.getInfo());
        }

        if (!isExists) {
            task.getTargetProvider().createNewTarget(mineType);
        }


        final long totalLength = task.getInfo().getTotalLength();
        if (!task.getInfo().isChunked()) {
            task.getTargetProvider().allocateLength(totalLength);
        }
    }

    private void executeTrial() throws IOException {

        boolean isNeedTrialHeadMethod;
        Response response = null;
        boolean isEtagRequest = false;
        String localEtag = task.getInfo() == null ? null : task.getInfo().getEtag();
        try {
            Request request = task.getRawRequest().newBuilder().header(Util.RANGE, "bytes=0-0").build();
            if (!Util.isEmpty(localEtag)) {
                isEtagRequest = true;
                request = task.getRawRequest().newBuilder().header(Util.IF_MATCH, localEtag).build();
            }
            response = task.getClient().newCall(request).execute();
            mineType = response.body().contentType().toString();
            Headers headers = response.headers();
            task.setResponseHeaders(headers);
            task.setResponseCode(response.code());
            task.setResponseEtag(headers.get(Util.ETAG));
            task.setMd5Code(headers.get(Util.CONTENT_MD5));
            task.setAcceptRange(DownloadUtils.isAcceptRange(headers, response.code()));
            task.setInstanceLength(DownloadUtils.findInstanceLength(response.headers()));
            task.setResponseFilename(DownloadUtils.findFilename(headers));
            if (TextUtils.isEmpty(task.getFileName())) {
                String fileName = DownloadUtils.determineFilename(task.getResponseFilename(), task.getUrl());
                task.setFileName(fileName);
            }

            isNeedTrialHeadMethod = DownloadUtils.isNeedTrialHeadMethodForInstanceLength(task.getInstanceLength(),
                    headers);
            task.setRedirectLocation(getRedirectLocation(response));

        } finally {
            okhttp3.internal.Util.closeQuietly(response);
        }
        if (isNeedTrialHeadMethod) {
            Request request = task.getRawRequest();
            if (!Util.isEmpty(localEtag)) {
                request = task.getRawRequest().newBuilder().header(Util.IF_MATCH, localEtag).build();
            }
            task.setInstanceLength(DownloadUtils.trialHeadMethodForInstanceLength(task.getClient(), request));
        }


        final ResumeFailedCause resumeFailedCause = getPreconditionFailedCause(task.getResponseCode(), task.getInfo() != null && task.getInfo().getTotalOffset() != 0, localEtag,
                task.getResponseEtag());
        resumable = resumeFailedCause == null;
        if(task.getInfo() == null){
           createNewInfo();
        }
        task.getInfo().setChunked(isChunked());
        task.getInfo().setEtag(task.getResponseEtag());
        if (!isTrialSpecialPass(task.getResponseCode(), task.getInstanceLength(), resumable)
                && isServerCanceled(task.getResponseCode(), !isEtagRequest)) {
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
                                                               @NonNull String localEtag,
                                                               @Nullable String responseEtag) {
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
