package com.pudao.zxingdemo.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pudao.zxingdemo.PosterEditActivity;
import com.pudao.zxingdemo.R;
import com.pudao.zxingdemo.adapter.StickerAdapter;
import com.pudao.zxingdemo.sticker.StickerItem;
import com.pudao.zxingdemo.sticker.StickerView;
import com.pudao.zxingdemo.task.StickerTask;
import com.pudao.zxingdemo.utils.BitmapUtils;

import java.util.LinkedHashMap;


/**
 * Created by pucheng on 2017/7/5.
 * 水印
 */

public class WaterMarkFragment extends Fragment {

    private PosterEditActivity activity;

    private RecyclerView mRecyclerView;
    private StickerAdapter mStickerAdapter;

    private StickerView mStickerView;
    private SaveStickersTask mSaveTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watermake, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (PosterEditActivity) getActivity();
        mStickerView = activity.mStickerView;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mStickerAdapter = new StickerAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mStickerAdapter);
        initData();
    }

    private void initData() {
        mStickerAdapter.setListener(new StickerAdapter.OnStickerClickListener() {
            @Override
            public void onClick(String path) {
                //告知外层Activity点击事件
                activity.onStickerClick(path);
            }
        });
    }

    /**
     * 保存贴图任务
     */
    private final class SaveStickersTask extends StickerTask {
        public SaveStickersTask(PosterEditActivity activity) {
            super(activity);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            LinkedHashMap<Integer, StickerItem> addItems = mStickerView.getBank();
            for (Integer id : addItems.keySet()) {
                StickerItem item = addItems.get(id);
                item.matrix.postConcat(m);// 乘以底部图片变化矩阵
                canvas.drawBitmap(item.bitmap, item.matrix, null);
            }// end for
        }

        @Override
        public void onPostResult(Bitmap result) {
            //保存图片到本地
            BitmapUtils.saveBitmap(result, activity.saveFilePath);
            mStickerView.clear();
        }
    }

    /**
     * 保存贴图层 合成一张图片
     */
    public void applyStickers() {
        // System.out.println("保存 合成图片");
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
        }
        mSaveTask = new SaveStickersTask((PosterEditActivity) getActivity());
        mSaveTask.execute(((((PosterEditActivity) getActivity()).srcBitmap)));
    }
}
