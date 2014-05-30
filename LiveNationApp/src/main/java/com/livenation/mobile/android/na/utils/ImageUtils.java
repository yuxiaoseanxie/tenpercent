package com.livenation.mobile.android.na.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;

/**
 * Created by elodieferrais on 5/9/14.
 */
public class ImageUtils {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radiusPixel, int strokePixel) {

        int minEdge = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final int strokeColor = 0x3B000000;
        final Paint paint = new Paint();
        final Rect rect = new Rect((bitmap.getWidth() - minEdge)/ 2, (bitmap.getHeight() - minEdge) / 2, minEdge, minEdge);
        final RectF rectF = new RectF(rect);
        final float roundPx = radiusPixel;

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) strokePixel);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        return output;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap, int strokePixel) {
        int minEdge = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap input = ThumbnailUtils.extractThumbnail(bitmap, minEdge, minEdge);

        return getRoundedCornerBitmap(input, minEdge, strokePixel);
    }
}
