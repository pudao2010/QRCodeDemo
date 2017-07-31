package com.pudao.zxingdemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pudao.zxingdemo.ui.FilterFragment;
import com.pudao.zxingdemo.ui.QRCodeFragment;
import com.pudao.zxingdemo.ui.StickerFragment;
import com.pudao.zxingdemo.ui.WaterMarkFragment;

import java.util.ArrayList;

/**
 * Created by pucheng on 2017/7/5.
 */

public class EditPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private static final String[] sTitles = {"水印", "贴纸", "二维码", "滤镜"};

    public EditPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new WaterMarkFragment();
                break;
            case 1:
                fragment = new StickerFragment();
                break;
            case 2:
                fragment = new QRCodeFragment();
                break;
            case 3:
                fragment = new FilterFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return sTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return sTitles[position];
    }
}
