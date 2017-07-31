package com.pudao.zxingdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pudao.zxingdemo.sticker.StickerView;
import com.pudao.zxingdemo.sticker.TextStickerView;
import com.pudao.zxingdemo.ui.FilterFragment;
import com.pudao.zxingdemo.ui.QRCodeFragment;
import com.pudao.zxingdemo.ui.StickerFragment;
import com.pudao.zxingdemo.ui.WaterMarkFragment;
import com.pudao.zxingdemo.utils.BitmapUtils;
import com.pudao.zxingdemo.utils.Util;


public class PosterEditActivity extends AppCompatActivity implements View.OnClickListener {

    private String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .getAbsolutePath().concat("/").concat("qutui360").concat("/").concat("test.jpg");

    public String saveFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            .getAbsolutePath().concat("/").concat("qutui360").concat("/").concat("edit"+SystemClock.currentThreadTimeMillis()).concat(".jpg");// 生成的新图片路径

    private FrameLayout mFrameLayout;
    public ImageView mImageView;
    public Bitmap srcBitmap;
    private TextView mBack;
    private TextView mDone;
    private TextView mDraft;

    public StickerView mStickerView; //贴纸,水印
    private TextStickerView mTextStickerView; //文本,暂未使用

    private RadioGroup mRadioGroup;
    private WaterMarkFragment waterMarkFragment;
    private StickerFragment stickerFragment;
    private QRCodeFragment qrCodeFragment;
    private FilterFragment filterFragment;
    private Bitmap stickerBitmap;
    private Bitmap resultBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster_edit);
        initView();
        liadImage();
        initData();
    }

    //加载图片,采样后加载防止OOM
    private void liadImage() {
        int screenWidth = Util.getScreenWidth(this);
        int screenHeight = Util.getScreenHeight(this);
        //对图片进行压缩后加载,防止OOM
        Bitmap sampledBitmap = BitmapUtils.getSampledBitmap(path, screenWidth, screenHeight);
        srcBitmap = sampledBitmap;
//        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
        mImageView.setImageBitmap(sampledBitmap);
    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.fl_content);
        mImageView = (ImageView) findViewById(R.id.iv_bgimg);
        mStickerView = (StickerView) findViewById(R.id.stickerview);
        mTextStickerView = (TextStickerView) findViewById(R.id.textsticker);
        mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        mBack = (TextView) findViewById(R.id.tv_back);
        mDone = (TextView) findViewById(R.id.tv_done);
        mDraft = (TextView) findViewById(R.id.tv_draft);
        //初始化一个fragment
        if (waterMarkFragment == null) {
            waterMarkFragment = new WaterMarkFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_editcontainer, waterMarkFragment, "watermark")
                .commitAllowingStateLoss();
    }

    private void initData() {
        mBack.setOnClickListener(this);
        mDraft.setOnClickListener(this);
        mDone.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (checkedId) {
                    case R.id.radio_watermark:
                        if (waterMarkFragment == null) {
                            waterMarkFragment = new WaterMarkFragment();
                        }
                        //// TODO: 2017/7/9 应该优化为add hide show的方式
                        transaction.replace(R.id.fl_editcontainer, waterMarkFragment, "watermark");
                        break;
                    case R.id.radio_sticker:
                        if (stickerFragment == null) {
                            stickerFragment = new StickerFragment();
                        }
                        transaction.replace(R.id.fl_editcontainer, stickerFragment, "sticker");
                        break;
                    case R.id.radio_qrcode:
                        if (qrCodeFragment == null) {
                            qrCodeFragment = new QRCodeFragment();
                        }
                        transaction.replace(R.id.fl_editcontainer, qrCodeFragment, "qrcode");
                        break;
                    case R.id.radio_filter:
                        if (filterFragment == null) {
                            filterFragment = new FilterFragment();
                        }
                        transaction.replace(R.id.fl_editcontainer, filterFragment, "filter");
                        break;
                }
                transaction.commitAllowingStateLoss();
            }
        });
    }


    /**
     * 处理贴图
     * @param path
     */
    public void onStickerClick(String path){
        Log.e("tag", "Activity响应贴图点击");
        stickerBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.sticker_01);
        mStickerView.addBitImage(stickerBitmap);
        mStickerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_done:
                waterMarkFragment.applyStickers();
                break;
            case R.id.tv_draft:
                // TODO 存入草稿箱
                Log.e("tag", "存入草稿箱");
                break;
        }
    }




}
