package com.pudao.zxingdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pudao.zxingdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> </p>
 *
 * @author pucheng  2017/7/9
 */

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {

    private int count;
    // 图片路径列表
    private List<String> pathList = new ArrayList<>();

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sticker, parent, false));
    }

    @Override
    public void onBindViewHolder(StickerViewHolder holder, int position) {
        //加载贴图
//        final String path = pathList.get(position);
        holder.image.setImageResource(R.mipmap.sticker_01);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Log.e("tag", "点击贴图: 计数" + (count++));
                    listener.onClick("path");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
//        return pathList.size();
        return 10;
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder{

        ImageView image;

        public StickerViewHolder(View itemView) {
            super(itemView);
            image = (ImageView)(itemView.findViewById(R.id.img));
        }
    }

    public void setListener(OnStickerClickListener listener) {
        this.listener = listener;
    }

    private OnStickerClickListener listener;

    public interface OnStickerClickListener{
        void onClick(String path);
    }
}
