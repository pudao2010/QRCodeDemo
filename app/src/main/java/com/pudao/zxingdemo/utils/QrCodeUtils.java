package com.pudao.zxingdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 使用Zxing进行二维码处理
 * Created by Tesla on 2015/3/22.
 */
public final class QrCodeUtils {

    private QrCodeUtils() {}

    private static final int RED = 0xFFFF0000;
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private final static Handler mHandler = new Handler(Looper.getMainLooper());

    // 默认生成图片外边距
    private static final int QR_MARGIN = 5;
    // 默认生成图片深色块颜色 (黑色)
    private static final int QR_DEEP_COLOR = 0XFF000000;
    // 默认生成图片浅色块颜色（白色）
    private static final int QR_LIGHT_COLOR = 0XFFFFFFFF;
    // 默认生成图片容错率 15% （容错率越高，色块越多，越密集）
    private static final int QR_ERROR_CORRECTION = 0x00;

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> allFormats = new ArrayList<>();
        allFormats.add(BarcodeFormat.AZTEC);
        allFormats.add(BarcodeFormat.CODABAR);
        allFormats.add(BarcodeFormat.CODE_39);
        allFormats.add(BarcodeFormat.CODE_93);
        allFormats.add(BarcodeFormat.CODE_128);
        allFormats.add(BarcodeFormat.DATA_MATRIX);
        allFormats.add(BarcodeFormat.EAN_8);
        allFormats.add(BarcodeFormat.EAN_13);
        allFormats.add(BarcodeFormat.ITF);
        allFormats.add(BarcodeFormat.MAXICODE);
        allFormats.add(BarcodeFormat.PDF_417);
        allFormats.add(BarcodeFormat.QR_CODE);
        allFormats.add(BarcodeFormat.RSS_14);
        allFormats.add(BarcodeFormat.RSS_EXPANDED);
        allFormats.add(BarcodeFormat.UPC_A);
        allFormats.add(BarcodeFormat.UPC_E);
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);

        HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    public static void generateAsync(final Handler handler,
                                     @NonNull final String content,
                                     final int width, final int height,
                                     final int margin, final int deepColor,
                                     final int lightColor, final int errorCorrection,
                                     @NonNull final QrCodeCallback<Bitmap> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler use = null == handler ? mHandler : handler;
                final Bitmap ret = generateQrCodeImage(content, width, height, margin,
                        deepColor, lightColor, errorCorrection);
                use.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(null != ret && !ret.isRecycled(), ret);
                    }
                });
            }
        }).start();
    }

    public static void parseFromPathAsync(final Handler handler, @NonNull final String path,
                                      @NonNull final QrCodeCallback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler use = null == handler ? mHandler : handler;
                final String ret = parseFromFile(path);
                use.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(!TextUtils.isEmpty(ret), ret);
                    }
                });
            }
        }).start();
    }

    public static void parseFromBitmapAsync(final Handler handler, @NonNull final Bitmap input,
                                      @NonNull final QrCodeCallback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler use = null == handler ? mHandler : handler;
                final String ret = parseFromBitmap(input);
                use.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete(!TextUtils.isEmpty(ret), ret);
                    }
                });
            }
        }).start();
    }

    /**
     * 生成二维码位图Bitmap对象
     *
     * @param content 输入内容
     * @param width  图片宽度
     * @param height 图片高度
     * @param margin 图片外边距
     * @param deepColor 深色块颜色
     * @param lightColor 浅色块颜色
     * @param errorCorrection 浅色块颜色
     * @return 位图对象
     */
    public static Bitmap generateQrCodeImage(@NonNull String content,
                                             int width, int height,
                                             int margin, int deepColor,
                                             int lightColor, int errorCorrection) {

        int qrWidth = width;
        int qrHeight = height;
        int qrMargin = 0 > margin ? QR_MARGIN : margin;
        int qrDeepColor = -1 == deepColor ? QR_DEEP_COLOR : deepColor;
        int qrLightColor = -1 == lightColor ? QR_LIGHT_COLOR : lightColor;
        int qrErrorCorrection = -1 == errorCorrection ? QR_ERROR_CORRECTION : errorCorrection;

        Bitmap output = null;

        try {
            // 判断合法性
            if (TextUtils.isEmpty(content)) {
                throw new IllegalArgumentException("Url is must not be null or empty");
            }
            if (qrWidth < 10 || qrHeight < 10) {
                throw new IllegalArgumentException("Width and height are must not be less than 9");
            }
            if (qrDeepColor > qrLightColor) {
                throw new IllegalArgumentException("The deep color should be larger " +
                        "than light color. For example, 0xff000000, colors must be larger than" +
                        " zero and within alpha value.");
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, qrMargin);
            hints.put(EncodeHintType.ERROR_CORRECTION,
                    ErrorCorrectionLevel.forBits(qrErrorCorrection));
            BitMatrix bitMatrix = new QRCodeWriter().encode(content,
                    BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
            int[] pixels = new int[qrWidth * qrHeight];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < qrHeight; y++) {
                for (int x = 0; x < qrWidth; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * qrWidth + x] = QR_DEEP_COLOR; //深色块
                    } else {
                        pixels[y * qrWidth + x] = QR_LIGHT_COLOR; //浅色块
                    }
                }
            }

            output = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_4444);
            output.setPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrHeight);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * 对图片进行二维码解析
     * @param input Bitmap
     */
    public static String parseFromBitmap(@NonNull Bitmap input) {
        try {
            final StringBuilder builder = new StringBuilder();
            Result result = parseResultFromBitmap(input);
            if (null != result) {
                builder.append(result.getText());
            }
            return builder.toString();
        } catch (Exception e) {}

        return "";
    }

    /**
     * 从位图中获取解析结果
     * @param input
     * @return
     */
    private static Result parseResultFromBitmap(@NonNull Bitmap input) {
        LuminanceSource source = null;
        try {
            float scale = 1;
            // 图片过大会延长解析时间，所以等比缩放
            if (1000 < input.getWidth()) {
                scale = 1000f / Math.max(input.getWidth(), input.getHeight());
            }
            Bitmap newBp = Bitmap.createScaledBitmap(input,
                    (int) (input.getWidth() * scale),
                    (int) (input.getHeight() * scale), false);
            int[] rgbArray = new int[newBp.getWidth() * newBp.getHeight()];
            newBp.getPixels(rgbArray, 0, newBp.getWidth(), 0, 0, newBp.getWidth(), newBp.getHeight());
            source = new RGBLuminanceSource(newBp.getWidth(), newBp.getHeight(), rgbArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader formatReader = new MultiFormatReader();
            return formatReader.decode(bitmap, HINTS);
        } catch (Exception e) {
            e.printStackTrace();
            if (source != null) {
                try {
                    return new MultiFormatReader().decode(
                            new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * 对图片进行二维码解析
     * @param imagePath 资源路径
     * @return 解析结果
     */
    public static String parseFromFile(final String imagePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            options.inJustDecodeBounds = false;

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            int inSampleSize = 1;
            if (originalWidth > 1000 || originalHeight > 1000) {
                int halfWidth = originalWidth / 2;
                int halfHeight = originalHeight / 2;
                while ((halfWidth / inSampleSize >= 1000)
                        && (halfHeight / inSampleSize >= 1000)) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            Bitmap input = BitmapFactory.decodeFile(imagePath, options);
            return parseFromBitmap(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 对图片进行二维码解析
     * @param resId 资源ID
     * @return 解析结果
     */
    public static String parseFromRes(@NonNull Context context, @DrawableRes int resId) {
        try {
            if (0 >= resId) {
                return "";
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resId, options);
            options.inJustDecodeBounds = false;

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            int inSampleSize = 1;
            if (originalWidth > 1000 || originalHeight > 1000) {
                int halfWidth = originalWidth / 2;
                int halfHeight = originalHeight / 2;
                while ((halfWidth / inSampleSize >= 1000)
                        && (halfHeight / inSampleSize >= 1000)) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            Bitmap input = BitmapFactory.decodeResource(context.getResources(), resId, options);
            return parseFromBitmap(input);
        } catch (Exception e) {}

        return "";
    }

    public static String parseFromInputStream(@NonNull InputStream is) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inJustDecodeBounds = false;

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            int inSampleSize = 1;
            if (originalWidth > 1000 || originalHeight > 1000) {
                int halfWidth = originalWidth / 2;
                int halfHeight = originalHeight / 2;
                while ((halfWidth / inSampleSize >= 1000)
                        && (halfHeight / inSampleSize >= 1000)) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            is.reset();
            Bitmap input = BitmapFactory.decodeStream(is, null, options);
            return parseFromBitmap(input);
        } catch (Exception e) {}

        return "";
    }

    /**
     * 对图片进行二维码解析，获取二维码区域(如果有)
     * @param imagePath 资源路径
     * @return 解析结果
     */
    public static Rect parseRectFromFile(final String imagePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            options.inJustDecodeBounds = false;

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;
            int inSampleSize = 1;
            if (originalWidth > 1000 || originalHeight > 1000) {
                int halfWidth = originalWidth / 2;
                int halfHeight = originalHeight / 2;
                while ((halfWidth / inSampleSize >= 1000)
                        && (halfHeight / inSampleSize >= 1000)) {
                    inSampleSize *= 2;
                }
            }

            options.inSampleSize = inSampleSize;
            Bitmap input = BitmapFactory.decodeFile(imagePath, options);
            return parseRectFromBitmap(input);
        } catch (Exception e) {}

        return null;
    }

    public static Rect parseRectFromBitmap(final @NonNull Bitmap input) {
        Rect ret = null;
        try {
            Result result = parseResultFromBitmap(input);
            if (null != result) {
                ResultPoint[] points = result.getResultPoints();
                if (points.length > 0) {
                    int x = Math.round(points[1].getX());
                    int y = Math.round(points[1].getY());
                    int width = Math.round(Math.abs(points[1].getX() - points[2].getX()));
                    int height = Math.round(Math.abs(points[0].getY() - points[1].getY()));
                    // 将区域修正为方形
                    int size = Math.max(width, height);
                    ret = new Rect(x, y, size + x, size + y);
                    ret.inset(-Math.round(size * 0.2f), -Math.round(size * 0.2f));
                }
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static RectF parseRectFFromBitmap(final @NonNull Bitmap input) {
        RectF ret = null;
        try {
            Result result = parseResultFromBitmap(input);
            if (null != result) {
                ResultPoint[] points = result.getResultPoints();
                if (points.length > 0) {
                    int x = Math.round(points[1].getX());
                    int y = Math.round(points[1].getY());
                    int width = Math.round(Math.abs(points[1].getX() - points[2].getX()));
                    int height = Math.round(Math.abs(points[0].getY() - points[1].getY()));
                    // 将区域修正为方形
                    int size = Math.max(width, height);
                    ret = new RectF(x, y, size + x, size + y);
                    ret.inset(-Math.round(size * 0.2f), -Math.round(size * 0.2f));
                }
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 解析多个
     * @param input
     * @return
     */
    public static Result[] parseMultiFromBitmap(@NonNull Bitmap input) {
        LuminanceSource source = null;
        try {
            float scale = 1;
            // 图片过大会延长解析时间，所以等比缩放
//            if (1000 < input.getWidth()) {
//                scale = 1000f / Math.max(input.getWidth(), input.getHeight());
//            }
            Bitmap newBp = Bitmap.createScaledBitmap(input,
                    (int) (input.getWidth() * scale),
                    (int) (input.getHeight() * scale), false);
            int[] rgbArray = new int[newBp.getWidth() * newBp.getHeight()];
            newBp.getPixels(rgbArray, 0, newBp.getWidth(), 0, 0, newBp.getWidth(), newBp.getHeight());
            source = new RGBLuminanceSource(newBp.getWidth(), newBp.getHeight(), rgbArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();
            return qrCodeMultiReader.decodeMultiple(bitmap);
        } catch (Exception e) {
            Log.e("tag", "解析多个二维码异常");
            e.printStackTrace();
            if (source != null) {
                try {
                    return new QRCodeMultiReader().decodeMultiple(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 解析含有多个二维码的图片
     * @param bitmap
     * @return
     */
    public static Rect[] parsesMultiFromBitmap(Bitmap bitmap){
        Result[] results = parseMultiFromBitmap(bitmap);
        if (results != null && results.length > 0) {
            Rect[] rects = new Rect[results.length];
            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                Rect ret = null;
                if (null != result) {
                    ResultPoint[] points = result.getResultPoints();
                    if (points.length > 0) {
                        int x = Math.round(points[1].getX());
                        int y = Math.round(points[1].getY());
                        int width = Math.round(Math.abs(points[1].getX() - points[2].getX()));
                        int height = Math.round(Math.abs(points[0].getY() - points[1].getY()));
                        // 将区域修正为方形
                        int size = Math.max(width, height);
                        ret = new Rect(x, y, size + x, size + y);
                        ret.inset(-Math.round(size * 0.2f), -Math.round(size * 0.2f));
                        rects[i] = ret;
                    }
                }
            }
            return rects;
        }
        return null;
    }

    public static RectF[] parsesRectFsFromBitmap(Bitmap bitmap){
        Result[] results = parseMultiFromBitmap(bitmap);
        if (results != null && results.length > 0) {
            RectF[] rects = new RectF[results.length];
            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                RectF ret = null;
                if (null != result) {
                    ResultPoint[] points = result.getResultPoints();
                    if (points.length > 0) {
                        int x = Math.round(points[1].getX());
                        int y = Math.round(points[1].getY());
                        int width = Math.round(Math.abs(points[1].getX() - points[2].getX()));
                        int height = Math.round(Math.abs(points[0].getY() - points[1].getY()));
                        // 将区域修正为方形
                        int size = Math.max(width, height);
                        ret = new RectF(x, y, size + x, size + y);
                        ret.inset(-Math.round(size * 0.2f), -Math.round(size * 0.2f));
                        rects[i] = ret;
                    }
                }
            }
            return rects;
        }
        return null;
    }

}
