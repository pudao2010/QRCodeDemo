package com.pudao.zxingdemo.utils;

import java.util.List;

/**
 * Created by pucheng on 17/7/9.
 */

public class ListUtil {
    public static boolean isEmpty(List list) {
        if (list == null)
            return true;

        return list.size() == 0;
    }

}
