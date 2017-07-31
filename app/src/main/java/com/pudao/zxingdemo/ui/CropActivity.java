package com.pudao.zxingdemo.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.pudao.zxingdemo.R;
import com.pudao.zxingdemo.utils.QrCodeUtils;
import com.pudao.zxingdemo.utils.StreamUtil;
import com.pudao.zxingdemo.utils.Util;
import com.pudao.zxingdemo.widget.CropLayout;

import java.io.FileOutputStream;

public class CropActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CropActivity";

    private CropLayout mCropLayout;
    private String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .getAbsolutePath().concat("/").concat("qutui360").concat("/").concat("test3.jpg");

    //裁剪框的位置信息
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    private float translateY;
    private float translateX;

    private float widthScale;
    private float heightScale;
    private TextView tvCrop;

    private boolean hasMoreQRCode; //含有多个二维码
    private boolean hasOnlyQRCode; //只含有一个二维码
    private boolean noQRCode;      //没有二维码
    private Bitmap srcBitmap; //数据源Bitmap
    private Rect qrRect;  //二维码所在的矩形

    private int count;//有多个二维码时识别记录index
    private Rect[] rects; //记录所有的二维码的矩形

    /**
     * 将图片转换为新的宽高
     *
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏可以保证平移计算更准确
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_crop);
        String path = getIntent().getStringExtra("path");
        mCropLayout = (CropLayout) findViewById(R.id.cropLayout);
        srcBitmap = BitmapFactory.decodeFile(path);

        // 保证图片的宽高不大于屏幕的宽高
        if (srcBitmap.getWidth() > Util.getScreenWidth(this) || srcBitmap.getHeight() > Util.getScreenHeight(this)) {
            //先计算原始的宽高比
            float ratio = srcBitmap.getWidth() * 1.0f / (srcBitmap.getHeight());
            srcBitmap = zoomImg(srcBitmap, (int) (Util.getScreenHeight(this) * ratio), Util.getScreenHeight(this));
        }
        mCropLayout.getImageView().setImageBitmap(srcBitmap);
        tvCrop = (TextView) findViewById(R.id.tv_crop);
        tvCrop.setOnClickListener(this);
        // 二维码所在矩形
        qrRect = null;

        //含有多个二维码的图片
        rects = QrCodeUtils.parsesMultiFromBitmap(srcBitmap);
        if (rects != null && rects.length > 1) {
            Log.e("tag", "此图含有二维码数=" + rects.length);
            for (int i = 0; i < rects.length; i++) {
                Log.e("tag", "第"+i+"个二维码:"+rects[i].toString());
            }
            hasMoreQRCode = true;
        } else if (rects != null && rects.length == 1) {
            Log.e("tag", "此图含有一个二维码");
            qrRect = rects[0];
            hasOnlyQRCode = true;
        } else {
            Log.e("tag", "此图没有二维码");
            noQRCode = true;
        }
        if (hasOnlyQRCode) {
            cacluateScaleAndTranslate(srcBitmap, qrRect);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasMoreQRCode) {
            tvCrop.setText("识别下一个二维码");
        }
        if (hasOnlyQRCode) {
            tvCrop.setText("识别二维码");
            String text = QrCodeUtils.parseMultiFromBitmap(srcBitmap)[0].getText();
            Log.e(TAG, "onResume: "+text);
        }
        if (noQRCode) {
            tvCrop.setText("没有二维码");
        }
    }

    /**
     * 计算缩放倍率和平移值
     *
     * @param bitmap 数据源bitmap
     * @param qrRect 二维码所在矩形
     */
    private void cacluateScaleAndTranslate(Bitmap bitmap, Rect qrRect) {
        Log.e("tag", "qrRect: " + qrRect.toString());
        float qrCenterX = qrRect.centerX() + (Util.getScreenWidth(this) - bitmap.getWidth()) / 2;
        float qrCenterY = qrRect.centerY() + (Util.getScreenHeight(this) - bitmap.getHeight()) / 2;
        Log.e("tag", "二维码中心点: x=" + qrCenterX + ",y=" + qrCenterY);

        // 原始二维码的宽高
        float originWidth = qrRect.right - qrRect.left;
        float originHeight = qrRect.bottom - qrRect.top;
        Log.e("tag", "原始二维码: width=" + originWidth + ", height=" + originHeight);

        widthScale = 600 * 1.0f / originWidth;
        Log.e("tag", "widthScale=" + widthScale);
        heightScale = 600 * 1.0f / originHeight;
        Log.e("tag", "heightScale=" + heightScale);

        int width = Util.getScreenWidth(this);
        int height = Util.getScreenHeight(this);
        mLeft = (width - 600) / 2;
        mTop = (height - 600) / 2;
        mRight = (width + 600) / 2;
        mBottom = (height + 600) / 2;
        // 裁剪框所在矩形
        Rect cropRect = new Rect(mLeft, mTop, mRight, mBottom);

        Log.e("tag", "原始距离" + (qrRect.centerY() - cropRect.centerY()));
        translateY = heightScale * (qrRect.centerY() + (Util.getScreenHeight(this) - bitmap.getHeight()) / 2 - cropRect.centerY());
        Log.e("tag", "Y方向平移=" + translateY);
        translateX = (qrRect.centerX() + (Util.getScreenWidth(this) - bitmap.getWidth()) / 2 - cropRect.centerX()) * widthScale;
        Log.e("tag", "X方向平移=" + translateX);

        mCropLayout.setCropWidth(600);
        mCropLayout.setCropHeight(600);
        mCropLayout.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_crop:
                if (hasMoreQRCode) {
                    cacluateScaleAndTranslate(srcBitmap, rects[count]);
                    count++;
                    if (count > rects.length - 1) {
                        count = 0;
                    }
                }
                mCropLayout.getImageView().clearAction();
                //缩放
                mCropLayout.getImageView().scale(widthScale, heightScale);
                //平移
                mCropLayout.getImageView().translate(-translateX, -translateY);
                break;
        }
    }

    private void cropBitmap() {
        Bitmap bitmap = null;
        FileOutputStream os = null;
        try {
//            bitmap = mCropLayout.cropBitmap();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crop.jpg";
            Log.e("tag", "path=" + path);
            os = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("tag", "exception");
            e.printStackTrace();
        } finally {
            if (bitmap != null) bitmap.recycle();
            StreamUtil.close(os);
        }
    }
}
