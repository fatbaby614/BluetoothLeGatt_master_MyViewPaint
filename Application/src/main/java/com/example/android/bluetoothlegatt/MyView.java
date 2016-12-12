package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by lifangping on 16/5/9.
 */
public class MyView extends View {
    private int view_width = 0;		//屏幕的宽度
    private int view_height = 0;	//屏幕的高度
    private float preX;				//起始点的x坐标
    private float preY;				//起始点的y坐标
    private Path path;				//路径
    public Paint paint = null;		//画笔
    private Bitmap cacheBitmap = null;		//定义一个内存中的图片，该图片将作为缓冲区

    private Paint mBitmapPaint;
    private   Canvas cacheCanvas = null;		//定义cacheBitmap上的Canvas对象
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
//    private Canvas  mCanvas;

    public float pointX;
    public float pointY;
    public int pressValue;
    public float currentX ;
    public float currentY ;

    private float preX1;				//起始点的x坐标
    private float preY1;				//起始点的y坐标

    public float oldx;
    public float oldy;

    //路径对象
    class DrawPath{
        Path path;
        Paint paint;
    }
    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    private DrawPath dp;

    public MyView(Context context){
        super(context);
        //getDisplayMetrics()方法获取DisplayMetrics对象，用于获取屏幕信息
        view_width = context.getResources().getDisplayMetrics().widthPixels;	//获取屏幕宽度
        view_height = context.getResources().getDisplayMetrics().heightPixels;	//获取屏幕高度
        System.out.println("view_width:"+view_width);
        System.out.println("view_height"+view_height);
        initCanvas();
        savePath = new ArrayList();
        deletePath = new ArrayList();
//        path.moveTo(200,200);
//        //设置辅助点坐标 300，200       终点坐标400，200
//
//        path.quadTo(300, 300, 400, 200);

//        cacheCanvas.drawPath(path,paint);
        invalidate();
    }

    public void initCanvas(){


        //创建一个与该view相同大小的缓存区,Config.ARGB_8888 --> 一种32位的位图,意味着有四个参数,即A,R,G,B,每一个参数由8bit来表示.
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();		//创建一个新画布
        cacheCanvas.setBitmap(cacheBitmap);		//在cacheCanvas上绘制cacheBitmap
//        cacheCanvas.drawColor(Color.WHITE);
        paint = new Paint(Paint.DITHER_FLAG);	//Paint.DITHER_FLAG 防抖动
        paint.setColor(Color.BLUE);				//设置默认的画笔颜色为红色
        //设置画笔风格
        paint.setStyle(Paint.Style.STROKE);		//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);	//设置笔刷的图形样式
        paint.setStrokeCap(Paint.Cap.ROUND);	//设置画笔转弯处的连接风格
        paint.setStrokeWidth(6);				//设置默认笔触的宽度为1像素
        paint.setAntiAlias(true);				//设置抗锯齿功能
        paint.setDither(true);					//设置抖动效果

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pressValue>0){
            if (oldx>0||oldy>0){

            }else {
                oldx = pointX;
                oldy = pointY;
            }
            path.moveTo(oldx,oldy);

//            path.quadTo((oldx + pointX)/2,(oldy + pointY)/2,pointX,pointY);
            path.lineTo(pointX,pointY);

            oldx = pointX;
            oldy = pointY;
            invalidate();

        }else {
            oldx = 0;
            oldy = 0;
        }

        canvas.drawColor(0xffffff);				//设置背景颜色
//        Paint bmpPaint = new Paint();			//采用默认设置创建一个画笔
        canvas.drawBitmap(cacheBitmap, 0, 0, mBitmapPaint);		//绘制cacheBitmap
        canvas.drawPath(path, paint);			//绘制路径
        canvas.save(Canvas.ALL_SAVE_FLAG);		//保存canvas状态，最后所有的信息都会保存在第一个创建的Bitmap中
        canvas.restore();		//恢复canvas之前保存的状态，防止保存后对canvas执行的操作队后续的绘制有影响
//        canvas.drawCircle(currentX,currentY,pointX,paint);

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        System.out.println("x值"+x);
        System.out.println("y值"+y);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);			//将绘图的起点移动到（x,y）坐标的位置
                preX = x;
                preY = y;
                dp = new DrawPath();
                dp.paint = paint;
                dp.path = path;

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - preX);	//Math.abs 返回绝对值
                float dy = Math.abs(y - preY);
                if(dx >= 5 || dy >= 5) {            //判断是否在允许的范围内
                    path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);
                    preX = x;
                    preY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);		//绘制路径
                savePath.add(dp);
                path.reset();
                break;
        }
        invalidate();		//更新视图
        return true;		//返回true表示处理方法已经处理该事件
    }
    /*
     * 橡皮擦功能
     */
    public void clear(){
//        paint.setAlpha(0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));	//设置图形重叠时候的处理方式
        paint.setStrokeWidth(50);		//设置笔触的宽度
        paint.setColor(Color.WHITE);
    }
    /*
     * 保存功能
     */
    public void save(){
        try {
            saveBitmap("myPicture");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void erase(){
        //调用初始化画布函数以清空画布
        initCanvas();
        invalidate();//刷新
        savePath.clear();
        deletePath.clear();

    }
    public void saveBitmap(String fileName) throws IOException {
        File file = new File("/sdcard/pictures/" + fileName + ".png");	//创建文件对象
        file.createNewFile();	//创建一个新文件
        FileOutputStream fileOS = new FileOutputStream(file);	//创建一个文件输出流对象
        //将绘图内容压缩为png格式输出到输出流对象中,其中100代表品质
        cacheBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOS);
        fileOS.flush();		//将缓冲区中的数据全部写出到输出流中
        fileOS.close();		//关闭文件输出流对象
    }

    //撤销
    public void undo(){

        System.out.println(savePath.size()+"--------------");
        DrawPath drawPath = savePath.get(savePath.size() - 1);
        deletePath.add(drawPath);
        savePath.remove(savePath.size() - 1);
        invalidate();// 刷新

//        System.out.println(savePath.size()+"--------------");
//        if(savePath != null && savePath.size() > 0){
//            //调用初始化画布函数以清空画布
////            initCanvas();
//
//            //将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
//            DrawPath drawPath = savePath.get(savePath.size() - 1);
//            deletePath.add(drawPath);
//            savePath.remove(savePath.size() - 1);
//
//            //将路径保存列表中的路径重绘在画布上
//            Iterator iter = savePath.iterator();		//重复保存
//            while (iter.hasNext()) {
//                DrawPath dp = (DrawPath) iter.next();
//                cacheCanvas.drawPath(dp.path, dp.paint);
//                invalidate();// 刷新
//            }
////            invalidate();// 刷新
//        }
    }

}
