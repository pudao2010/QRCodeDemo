package com.pudao.zxingdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pudao.zxingdemo.utils.BitmapUtils;
import com.pudao.zxingdemo.utils.QrCodeUtils;
import com.pudao.zxingdemo.widget.QRCodeFrameView;


/**
 * Created by pucheng on 2017/7/6.
 */

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

//        Bitmap input = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
        Bitmap input = BitmapFactory.decodeResource(getResources(), R.drawable.test1);
        // width=1125, height=2001  (1.5倍像素宽高值) 实际宽750像素,高1334像素
        Log.e("tag", "width=" + input.getWidth() + ", height=" + input.getHeight());
        Rect rect = QrCodeUtils.parseRectFromBitmap(input);
        // Rect(188, 782 - 375, 969)  宽187 高187
        Log.e("tag", rect.toString());
//        int left = (int) (1.5 * rect.left);
//        int top = (int) (rect.top * 1.5);
//        int right = (int) (rect.right * 1.5);
//        int bottom = (int) (rect.bottom * 1.5);
//        rect.set(left, top, right, bottom);
        Bitmap bitmap = BitmapUtils.clipRect(input, rect);
//        ImageView ivImage = (ImageView) findViewById(R.id.imageview);
//        ivImage.setImageBitmap(bitmap);

        final QRCodeFrameView frameView = (QRCodeFrameView) findViewById(R.id.frameview);
        frameView.post(new Runnable() {
            @Override
            public void run() {
                int width = frameView.getWidth();
                int height = frameView.getHeight();
                Log.e("tag", "width=" + width + ",height=" + height);
            }
        });


    }
}
