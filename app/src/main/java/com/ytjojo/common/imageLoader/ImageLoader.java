package com.ytjojo.common.imageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/4/5 0005.
 */
public interface ImageLoader {
    void into(ImageView imageView);

    ImageLoader load(String url);

    ImageLoader load(Uri uri);

    ImageLoader load(File file);

    ImageLoader load(byte[] bytes);

    ImageLoader load(Integer drawableId);

    ImageLoader size(int width, int height);

    ImageLoader centerCrop();

    ImageLoader placeholder(int drawable);

    ImageLoader error(int drawable);

    ImageLoader fade();

    ImageLoader corner(int dp);

    ImageLoader circle();

    ImageLoader rotate(float angle);

    ImageLoader cacheInMemery(boolean b);

    ImageLoader cacheInDisk(boolean b);

    Observable<Bitmap> asBitmapObservable();

    ImageLoader cancel();
    void resetting();
    void cancleAll(Context context);

    void clearDisk(Context context);

    void clearMemory(Context context);
    void subject(Subscriber<Bitmap> bitmapSubscriber);
    void subject(Action1<Bitmap> bitmapAction1);
    boolean isExistInFile(String url);
}
