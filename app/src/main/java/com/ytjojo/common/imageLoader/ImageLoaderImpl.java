package com.ytjojo.common.imageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;

import java.io.File;
import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/4/5 0005.
 */
public class ImageLoaderImpl<T> implements ImageLoader {
    T source;
    String url;
    WeakReference<ImageView> imageViewWeakReference;
    Context context;
    int width = -1, height = -1;
    int errorDrawable = -1;
    int placeHolder = -1;
    boolean cacheInMemery = true;
    boolean cacheInDisk = true;
    boolean isCircle = false;
    static volatile ImageLoaderImpl instance;
    SourceType mSourceType = SourceType.URL;
    Uri uri;
    private File file;
    private byte[] bytes;
    private int drawableId;
    private int cornerDp;
    private float angle;

    enum SourceType {
        BYTES, URL, URI, FILE, DRAWABLEID
    }

    public static ImageLoaderImpl with(Context context) {
        if (instance == null) {
            instance = new ImageLoaderImpl();
        }
        return instance.context(context);
    }

    public ImageLoaderImpl context(Context context) {
        this.context = context.getApplicationContext();
        return this;
    }

    private <T> BitmapTypeRequest<T> finalInto(T source) {
        BitmapTypeRequest<T> request = Glide.with(context).load(source).asBitmap();
        if (placeHolder > 0) {
            request.placeholder(placeHolder);
        }
        if (errorDrawable > 0) {
            request.error(errorDrawable);
        }
        if (Util.isValidDimensions(width, height)) {
            request.override(width, height);
        }
        if (!cacheInDisk) {
            request.diskCacheStrategy(DiskCacheStrategy.NONE);
        }
        if (!cacheInMemery) {
            request.skipMemoryCache(true);
        }
        if (angle != 0) {

        }
        if (cornerDp > 0) {

        }
        if (isCircle) {

        }
        return request;

    }

    @Override
    public void into(ImageView imageView) {
        switch (mSourceType) {
            case URI:
                finalInto(uri).into(imageView);
                break;
            case URL:
                finalInto(url).into(imageView);
                break;
            case FILE:
                finalInto(file).into(imageView);
                break;
            case BYTES:
                finalInto(bytes).into(imageView);
                break;
            case DRAWABLEID:
                finalInto(drawableId).into(imageView);
                break;
        }
        resetting();

    }

    public void resetting() {
        this.angle = 0;
        this.width = -1;
        this.height = -1;
        this.placeHolder = -1;
        this.errorDrawable = -1;
        this.bytes = null;
        this.uri = null;
        this.url = null;
        this.drawableId = -1;
        this.cacheInDisk = true;
        this.cacheInMemery = true;
        isCircle = false;
    }

    @Override
    public ImageLoader load(String url) {
        mSourceType = SourceType.URL;
        this.url = url;
        return this;
    }

    @Override
    public ImageLoader load(Uri uri) {
        this.uri = uri;
        mSourceType = SourceType.URI;
        return this;
    }

    @Override
    public ImageLoader load(File file) {
        this.file = file;
        mSourceType = SourceType.FILE;
        return this;
    }

    @Override
    public ImageLoader load(byte[] bytes) {
        this.bytes = bytes;
        mSourceType = SourceType.BYTES;
        return this;
    }

    @Override
    public ImageLoader load(Integer drawableId) {
        this.drawableId = drawableId;
        mSourceType = SourceType.DRAWABLEID;
        return this;
    }

    @Override
    public ImageLoader size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public ImageLoader corner(int dp) {
        this.cornerDp = dp;
        isCircle = false;
        return this;
    }

    @Override
    public ImageLoader centerCrop() {
        return this;
    }

    @Override
    public ImageLoader placeholder(int drawable) {
        placeHolder = drawable;
        return this;
    }

    @Override
    public ImageLoader error(int drawable) {
        errorDrawable = drawable;
        return this;
    }

    @Override
    public ImageLoader fade() {
        return this;
    }

    @Override
    public ImageLoader circle() {
        isCircle = true;
        cornerDp = 0;
        return this;
    }

    @Override
    public ImageLoader rotate(float angle) {
        this.angle = angle;
        return null;
    }

    @Override
    public ImageLoader cacheInMemery(boolean b) {
        cacheInMemery = b;
        return this;
    }

    @Override
    public ImageLoader cacheInDisk(boolean b) {
        cacheInDisk = b;
        return this;
    }

    @Deprecated
    @Override
    public Observable<Bitmap> asBitmapObservable() {
        return null;
    }

    @Override
    public ImageLoader cancel() {
        if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
            Glide.clear(imageViewWeakReference.get());
        }
        return this;
    }

    public void cancleAll(Context context) {
        Glide.with(context).onDestroy();
    }

    public void clearDisk(Context context) {
        Glide.get(context.getApplicationContext()).clearDiskCache();
    }

    public void clearMemory(Context context) {
        Glide.get(context.getApplicationContext()).clearMemory();
    }

    public void subject(final Subscriber<Bitmap> bitmapSubscriber) {

        BitmapTypeRequest<?> urlRequest = null;
        switch (mSourceType) {
            case URI:
                urlRequest = finalInto(uri);
                break;
            case URL:
                urlRequest = finalInto(url);
                break;
            case FILE:
                urlRequest = finalInto(file);
                break;
            case BYTES:
                urlRequest = finalInto(bytes);
                break;
            case DRAWABLEID:
                urlRequest = finalInto(drawableId);
                break;
        }

        urlRequest.listener(new RequestListener<Object, Bitmap>() {
            @Override
            public boolean onException(Exception e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                bitmapSubscriber.onError(e);
                bitmapSubscriber.onCompleted();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                bitmapSubscriber.onNext(resource);
                bitmapSubscriber.onCompleted();
                return false;
            }
        });
        resetting();
    }

    @Override
    public void subject(final Action1<Bitmap> bitmapAction1) {
        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                bitmapAction1.call(bitmap);
            }
        };
        BitmapTypeRequest<?> urlRequest = null;

        switch (mSourceType) {
            case URI:
                urlRequest = finalInto(uri);
                break;
            case URL:
                urlRequest = finalInto(url);
                break;
            case FILE:
                urlRequest = finalInto(file);
                break;
            case BYTES:
                urlRequest = finalInto(bytes);
                break;
            case DRAWABLEID:
                urlRequest = finalInto(drawableId);
                break;
        }
        urlRequest.into(simpleTarget);
        resetting();
    }

    @Override
    public boolean isExistInFile(String url) {

        return false;
    }
}
