package com.ytjojo.common.glide;

import android.content.Context;

import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

/**
 *
 * String baseImageUrl = "https://futurestud.io/images/example.png";
 *CustomImageSizeModel customImageRequest = new CustomImageSizeModelFutureStudio( baseImageUrl );
 * Glide
 .with( context )
 .using( new CustomImageSizeUrlLoader( context ) )
 .load( customImageRequest )
 .into( imageView );
 */
public class CustomImageSizeUrlLoader extends BaseGlideUrlLoader<CustomImageSizeModel> {
    public CustomImageSizeUrlLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(CustomImageSizeModel model, int width, int height) {
        return model.requestCustomSizeUrl( width, height );
    }
}