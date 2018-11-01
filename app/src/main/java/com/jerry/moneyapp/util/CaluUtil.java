package com.jerry.moneyapp.util;

import java.util.ArrayList;
import java.util.LinkedList;

import com.jerry.moneyapp.bean.GBData;
import com.jerry.moneyapp.bean.Point;
import com.jerry.moneyapp.ui.AnalyzeActivity;

/**
 * Created by wzl on 2018/10/1.
 *
 * @Description
 */
public class CaluUtil {

    /**
     * @param ints 原始数据
     * @return 第一个参数表示投什么，第二个参数表示投多少
     */
    public static Point calulate(int[] ints, int position, LinkedList<Point> points) {
        Point point = new Point();
        if (position > ints.length) {
            return point;
        }
        if (position > 0) {
            // 判断是否加倍
            ArrayList<Integer> paint = new ArrayList<>();
            int index = position - 1;
            int tempSize = 1;
            while (index >= 0) {
                if (index == 0) {
                    paint.add(tempSize);
                } else {
                    if (ints[index] == ints[index - 1]) {
                        tempSize++;
                    } else {
                        paint.add(tempSize);
                        tempSize = 1;
                    }
                }
                index--;
            }
            boolean good = false;
            if (paint.size() > 1 && paint.get(0) > 1 && paint.get(1) > 1 && paint.get(0) + paint.get(1) > 5) {
                point.multiple2 = 2;
                point.multiple3 = 2;
                good = true;
            } else if (paint.size() > 2 && paint.get(0) > 1 && paint.get(1) > 1 && paint.get(2) > 1 && paint.get(0) + paint.get(1) +
                    paint.get(2) > 6) {
                point.multiple2 = 2;
                point.multiple3 = 2;
                good = true;
            } else if (paint.size() > 2 && paint.get(0) == 1 && paint.get(1) == 1 && paint.get(2) == 1) {
                point.multiple2 = -1;
                point.multiple3 = -1;
            }
            ArrayList<Integer> tempList = new ArrayList<>();
            int temp = 0;
            for (int num : paint) {
                if (num == 1) {
                    temp++;
                } else {
                    tempList.add(temp);
                    temp = 0;
                }
            }
            if (tempList.size() == 2 && tempList.get(1) > 2) {
                point.manyGudao = true;
            } else if (tempList.size() > 2 && (tempList.get(1) > 2 || tempList.get(2) > 2)) {
                point.manyGudao = true;
            } else if (paint.size() > 5 && (paint.get(0) == 1 && paint.get(1) == 1 && paint.get(2) == 1) && (paint.get(3) > 1 && paint
                    .get(4) > 1 && paint.get(3) + paint.get(4) > 6)) {
                point.manyGudao = true;
            }

            if (!good) {
                int paintSize = paint.size();
                if (paintSize > 1 && paint.get(0) == 1) {
                    if (paintSize > 2 && paint.get(1) > 1) {
                        point.multiple2 = 2;
                    } else if (paintSize == 2) {
                        point.multiple2 = 2;
                    }
                }
                if (paintSize > 2 && paint.get(0) == 1 && paint.get(1) == 1) {
                    if (paintSize > 3 && paint.get(2) > 1) {
                        point.multiple3 = 2;
                    } else if (paintSize == 3) {
                        point.multiple3 = 2;
                    }
                }
            }
            // 记录当前数到第几个
            int gd = 0;
            // 记录当前索引
            int gdIndex = 0;
            int min = Math.min(AnalyzeActivity.GUDAOCOUNT2, position);
            while (gd < min && gdIndex < paint.size() - 1) {
                if (paint.get(gdIndex) == 1) {
                    point.gudao2++;
                }
                gd += paint.get(gdIndex);
                gdIndex++;
            }
            if (point.multiple2 > 0) {
                if (point.gudao2 >= AnalyzeActivity.GUDAOLINIT2) {
                    point.intention2 = GBData.VALUE_NONE;
                } else {
                    point.intention2 = ints[position - 1];
                }
            } else {
                point.intention2 = ints[position - 2];
            }
            // 记录当前数到第几个
            gd = 0;
            // 记录当前索引
            gdIndex = 0;
            min = Math.min(AnalyzeActivity.GUDAOCOUNT3, position);
            while (gd < min && gdIndex < paint.size() - 1) {
                if (paint.get(gdIndex) == 1) {
                    point.gudao3++;
                }
                gd += paint.get(gdIndex);
                gdIndex++;
            }
            if (point.multiple3 > 0) {
                if (point.gudao3 >= AnalyzeActivity.GUDAOLINIT3) {
                    point.intention3 = GBData.VALUE_NONE;
                } else {
                    point.intention3 = ints[position - 1];
                }
            } else {
                point.intention3 = ints[position - 2];
            }
        } else {
            point.intention2 = GBData.VALUE_LONG;
            point.intention3 = GBData.VALUE_LONG;
        }
        return point;
    }
}