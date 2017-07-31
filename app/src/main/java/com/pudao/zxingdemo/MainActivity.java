package com.pudao.zxingdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.pudao.zxingdemo.utils.QrCodeUtils;


public class MainActivity extends AppCompatActivity {
    ImageView qrcode1;
    ImageView qrcode2;
    ImageView qrcode3;
    ImageView qrcode4;
    ImageView qrcode5;
    ImageView qrcode6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrcode1 = (ImageView) findViewById(R.id.qrcode1);
        qrcode2 = (ImageView) findViewById(R.id.qrcode2);
        qrcode3 = (ImageView) findViewById(R.id.qrcode3);
        qrcode4 = (ImageView) findViewById(R.id.qrcode4);
        qrcode5 = (ImageView) findViewById(R.id.qrcode5);
        qrcode6 = (ImageView) findViewById(R.id.qrcode6);

        qrcode1.setImageBitmap(QRCode.createQRCode("http://www.jianshu.com/users/8c33164dcc05/timeline"));
        qrcode2.setImageBitmap(QRCode.createQRCodeWithLogo2("http://www.jianshu.com/users/8c33164dcc05/timeline", 500, drawableToBitmap(getResources().getDrawable(R.drawable.head))));
        qrcode3.setImageBitmap(QRCode.createQRCodeWithLogo3("http://www.jianshu.com/users/8c33164dcc05/timeline", 500, drawableToBitmap(getResources().getDrawable(R.drawable.head))));
        qrcode4.setImageBitmap(QRCode.createQRCodeWithLogo4("http://u.wechat.com/MLYzQMGw0rhCiMEeJK6n7G8", 500, drawableToBitmap(getResources().getDrawable(R.drawable.head))));
        qrcode5.setImageBitmap(QRCode.createQRCodeWithLogo5("http://u.wechat.com/MLYzQMGw0rhCiMEeJK6n7G8", 500, drawableToBitmap(getResources().getDrawable(R.drawable.head))));
        qrcode6.setImageBitmap(QRCode.createQRCodeWithLogo6("http://u.wechat.com/MLYzQMGw0rhCiMEeJK6n7G8", 500, drawableToBitmap(getResources().getDrawable(R.drawable.head))));


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test1);
        Rect rect = QrCodeUtils.parseRectFromBitmap(bitmap);
        if (rect != null) {
            Log.e("tag", rect.toString());
        }



        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test2);
        Result[] results = QrCodeUtils.parseMultiFromBitmap(bitmap);
        if (results != null) {
            byte[] bytes = results[0].getRawBytes();
            Bitmap bitmap1 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            for (int i = 0; i < results.length; i++) {
                Log.e("tag", results[i].getText());
            }
        }*/


    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
