package com.jerry.moneyapp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.jerry.moneyapp.bean.GBData;
import com.jerry.moneyapp.bean.MyLog;
import com.jerry.moneyapp.bean.Point;
import com.jerry.moneyapp.ui.AnalyzeActivity;
import com.jerry.moneyapp.util.CaluUtil;
import com.jerry.moneyapp.util.DeviceUtil;
import com.jerry.moneyapp.util.WeakHandler;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MyService extends Service {

    private static final int LEFT = 12;//17
    private static final int RIGHT = 1068;//144
    private static final int TOP = 470;//610
    private static final int BOTTOM = 805;//1080
    public static final int ASSIABLEX = 990;//1320
    public static final int ASSIABLEY = 900;//1180

    public static final int MIDDELX = 500;//1180
    public static final int ENTERY = 930;//1180
    public static final int JUDGEY = 1240;//1180

    private int width;
    private int height;

    private int[] pointsX = new int[18];
    private int[] pointsY = new int[6];
    private LinkedList<Integer> data = new LinkedList<>();
    private volatile int length;
    private boolean mBtnClickable;//点击生效
    private Callback mCallback;
    private StringBuilder sb = new StringBuilder();
    private Point lastP;
    private int stopCount = 0;
    private int stopCountx = 0;

    protected WeakHandler mWeakHandler = new WeakHandler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == -1) {
                return false;
            }
            boolean enter = GBData.getCurrentData(pointsX, pointsY, data);
            if (enter) {
                execShellCmd("input tap " + MIDDELX + " " + ENTERY);
                mWeakHandler.sendEmptyMessageDelayed(0, 2000);
                return false;
            }
            mWeakHandler.sendEmptyMessageDelayed(0, 12000);
            if (data.size() == length) {
                return false;
            }
            if (data.size() == 0 && length < 68) {
                return false;
            }
            //点击一下空白处
            length = data.size();
            execShellCmd("input tap " + 400 + " " + 400);
            LinkedList<Integer> paint = new LinkedList<>();
            LinkedList<Point> points = new LinkedList<>();
            int[] ints = new int[data.size()];
            for (int i = 0; i < ints.length; i++) {
                ints[i] = data.get(i);
            }
            lastP = null;
            for (int j = 0; j < ints.length; j++) {
                Point point = CaluUtil.calulate(ints, j + 1, points);
                point.current = ints[j];
                if (lastP != null) {
                    if (lastP.current == point.current && paint.size() > 0) {
                        int temp = paint.getLast();
                        paint.removeLast();
                        paint.addLast(++temp);
                    } else {
                        paint.add(1);
                    }
                    if (lastP.intention2 != GBData.VALUE_NONE) {
                        if (lastP.intention2 == point.current) {
                            point.win2 = lastP.win2 + 9.7 * Math.abs(lastP.multiple2);
                        } else {
                            point.win2 = lastP.win2 - 10 * Math.abs(lastP.multiple2);
                        }
                    } else {
                        point.win2 = lastP.win2;
                    }
                    if (lastP.intention3 != GBData.VALUE_NONE) {
                        if (lastP.intention3 == point.current) {
                            point.win3 = lastP.win3 + 9.7 * Math.abs(lastP.multiple3);
                        } else {
                            point.win3 = lastP.win3 - 10 * Math.abs(lastP.multiple3);
                        }
                    } else {
                        point.win3 = lastP.win3;
                    }
                    if (lastP.intentionn != GBData.VALUE_NONE) {
                        if (lastP.intentionn == point.current) {
                            point.winn = lastP.winn + 9.7 * Math.abs(lastP.multiplen);
                            stopCount = 0;
                        } else {
                            point.winn = lastP.winn - 10 * Math.abs(lastP.multiplen);
                            stopCount++;
                        }
                    } else {
                        point.winn = lastP.winn;
                    }
                    if (lastP.intentionX != GBData.VALUE_NONE) {
                        if (lastP.intentionX == point.current) {
                            point.winX = lastP.winX + 9.7;
                            if (lastP.state == 0) {
                                point.state = 1;
                            } else if (lastP.state == 2) {
                                point.state = 2;
                            }
                            stopCountx = 0;
                        } else {
                            point.winX = lastP.winX - 10;
                            if (lastP.state == 0) {
                                point.state = 2;
                            } else if (lastP.state == 2) {
                                point.state = 1;
                            }
                            stopCountx++;
                        }
                    } else {
                        point.winX = lastP.winX;
                        point.state = lastP.state;
                    }
                    if (lastP.intention != GBData.VALUE_NONE) {
                        if (lastP.intention == point.current) {
                            point.win = lastP.win + 9.7 * Math.abs(lastP.multiple);
                        } else {
                            point.win = lastP.win - 10 * Math.abs(lastP.multiple);
                        }
                    } else {
                        point.win = lastP.win;
                    }
                } else {
                    paint.add(1);
                }
                if (point.winn > AnalyzeActivity.GIVEUPCOUNT && stopCount < AnalyzeActivity.STOPCOUNT) {
                    if (AnalyzeActivity.LASTPOINTNUM2 > 0 && points.size() >= AnalyzeActivity.LASTPOINTNUM2) {
                        point.award2 = point.win2 - points.get(points.size() - AnalyzeActivity.LASTPOINTNUM2).win2;
                    } else {
                        point.award2 = point.win2;
                    }
                    if (AnalyzeActivity.LASTPOINTNUM3 > 0 && points.size() >= AnalyzeActivity.LASTPOINTNUM3) {
                        point.award3 = point.win3 - points.get(points.size() - AnalyzeActivity.LASTPOINTNUM3).win3;
                    } else {
                        point.award3 = point.win3;
                    }
                    if (point.award2 >= point.award3) {
                        point.currentType = 2;
                    } else {
                        point.currentType = 3;
                    }
                    if (lastP != null) {
                        if (j > AnalyzeActivity.START && point.award2 >= AnalyzeActivity.LASTWIN2 && point.award3 >= AnalyzeActivity.LASTWIN3
                                && point.win2 > AnalyzeActivity.WHOLEWIN2 && point.win3 > AnalyzeActivity.WHOLEWIN3) {
                            if (point.currentType == 2 && point.intention2 != GBData.VALUE_NONE) {
                                point.intentionn = point.intention2;
                                point.multiplen = point.multiple2;
                            } else if (point.currentType == 3 && point.intention3 != GBData.VALUE_NONE) {
                                point.intentionn = point.intention3;
                                point.multiplen = point.multiple3;
                            } else {
                                point.intentionn = GBData.VALUE_NONE;
                            }
                        } else {
                            point.intentionn = GBData.VALUE_NONE;
                        }
                    } else {
                        point.intentionn = GBData.VALUE_NONE;
                    }
                } else {
                    point.intentionn = GBData.VALUE_NONE;
                }
                if (point.multiplen > 1 && point.winn - 10 * point.multiplen < AnalyzeActivity.GIVEUPCOUNT) {
                    point.multiplen = 1;
                }

                if (point.winX > AnalyzeActivity.GIVEUPCOUNTX && stopCount < AnalyzeActivity.STOPCOUNT) {
                    if (point.state == 0 && paint.size() > 1 && paint.get(paint.size() - 1) == 1 && paint.get(paint.size() - 2) > 1) {
                        point.intentionX = point.current;
                    } else if (point.state == 1 && paint.size() > 1 && paint.get(paint.size() - 2) == 1) {
                        point.state = 0;
                    } else if (point.state == 2 && paint.size() > 1 && paint.get(paint.size() - 1) == 1 && paint.get(paint.size() - 2) > 1) {
                        point.intentionX = point.current == GBData.VALUE_LONG ? GBData.VALUE_FENG : GBData.VALUE_LONG;
                    } else {
                        point.intentionX = GBData.VALUE_NONE;
                    }
                } else {
                    point.intentionX = GBData.VALUE_NONE;
                }
                if (point.intentionn != GBData.VALUE_NONE && point.intentionX != GBData.VALUE_NONE) {
                    point.intention = point.intentionn;
                    if (point.intentionn == point.intentionX) {
                        point.multiple = point.multiplen + 1;
                    } else {
                        point.multiple = point.multiplen - 1;
                    }
                } else {
                    point.intention = point.intentionn + point.intentionX;
                    if (point.intentionn == GBData.VALUE_NONE) {
                        point.multiple = 1;
                    } else {
                        point.multiple = point.multiplen;
                    }
                }
                if (point.multiple == 0) {
                    point.intention = 0;
                } else if (point.multiple > 1 && point.win - 10 * point.multiple < AnalyzeActivity.GIVEUPCOUNTS) {
                    point.multiple = 1;
                }
                lastP = point;
                points.add(point);
            }
            if (lastP == null) {
                return false;
            }
            if (mBtnClickable && lastP.intention != GBData.VALUE_NONE && data.size() < 69) {
                exeCall(lastP.intention, lastP.multiple);
            }
            showJingsheng();
            if (data.size() >= 69) {
                BmobQuery<MyLog> query = new BmobQuery<>();
                query.setLimit(1).order("-updatedAt").findObjects(new FindListener<MyLog>() {
                    @Override
                    public void done(List<MyLog> list, BmobException e) {
                        if (e != null) {
                            return;
                        }
                        if (list.size() > 0) {
                            long lastTime = 0;
                            try {
                                String lateDate = list.get(0).getCreatedAt();
                                lastTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA).parse(lateDate).getTime();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            if (lastTime > 0) {
                                long second = (System.currentTimeMillis() - lastTime) / 1000;
                                if (second < 200) {
                                    return;
                                }
                            }
                        }
                        Calendar now = Calendar.getInstance();
                        sb.append(now.getTime()).append(":").append(lastP.win).append("元").append("\n");
                        MyLog myLog = new MyLog();
                        myLog.setLog(sb.toString());
                        myLog.setData(data);
                        myLog.setDeviceId(DeviceUtil.getDeviceId());
                        myLog.save();
                        sb.delete(0, sb.length());
                    }
                });
            } else {
                Calendar now = Calendar.getInstance();
                sb.append(now.getTime()).append(":").append(lastP.win).append("元").append("\n");
            }
            return false;
        }
    });

    private String getIntentStr(int intention, int mutiple) {
        if (intention == GBData.VALUE_NONE) {
            return " pass";
        }
        return (intention == GBData.VALUE_LONG ? "  龙" : "  凤") + String.valueOf(mutiple);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        double eachX = (RIGHT - LEFT) / 18d;
        double eachY = (BOTTOM - TOP) / 6d;
        double initX = LEFT + eachX * 0.85d;
        double initY = TOP + eachY / 2d;
        for (int i = 0; i < pointsX.length; i++) {
            pointsX[i] = (int) (initX + i * eachX);
        }
        for (int i = 0; i < pointsY.length; i++) {
            pointsY[i] = (int) (initY + i * eachY);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public void setBtnClickable() {
        mBtnClickable = !mBtnClickable;
        Toast.makeText(this, mBtnClickable ? "点击生效！" : "点击取消!", Toast.LENGTH_SHORT).show();
    }

    public void showJingsheng() {
        if (lastP == null) {
            return;
        }
        mCallback.showText(new StringBuilder()
                .append("Jerry打法：").append(DeviceUtil.m2(lastP.winn)).append("，").append(getIntentStr(lastP.intentionn, lastP.multiplen))
                .append("\nsj打法：").append(DeviceUtil.m2(lastP.winX)).append("，").append(getIntentStr(lastP.intentionX, 1))
                .append("\n模拟净胜：").append(DeviceUtil.m2(lastP.win))
                .append("\t下一局：").append(getIntentStr(lastP.intention, lastP.multiple)).toString());
    }

    public class PlayBinder extends Binder {

        public MyService getPlayService() {
            return MyService.this;
        }
    }

    public void startExe() {
        mWeakHandler.sendEmptyMessage(0);
        showJingsheng();
    }

    private void execShellCmd(String cmd) {
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            outputStream = process.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd + "\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exeCall(int type, int mutiple) {
        int clickX = type == GBData.VALUE_LONG ? (int) (width * 0.25) : (int) (width * 0.75);
        int clickY = (int) (height * 0.9);
        for (int i = 0; i < mutiple; i++) {
            execShellCmd("input tap " + clickX + " " + clickY);
        }
        mWeakHandler.postDelayed(() -> execShellCmd("input tap " + ASSIABLEX + " " + ASSIABLEY), 2000);
    }


    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {

        void showText(String data);
    }
}