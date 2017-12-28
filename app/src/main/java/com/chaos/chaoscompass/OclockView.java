package com.chaos.chaoscompass;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

/**
 * Created by yc.Zhao on 2017/12/26 0026.
 */

public class OclockView extends View {
    //时钟正方形的宽高
    private int width;
    //圆心点x和y坐标,x=y
    private int mCenterPoint;
    //刻度笔,为浅白色
    private Paint paint;
    //当前刻度笔，为白色
    private Paint time_paint;
    //中心圆环笔
    private Paint center_paint;
    //渐变比
    private Paint change_paint;
    //秒针比
    private Paint sPaint;
    //分针比
    private Paint mPaint;
    //是真比
    private Paint hPaint;
    //圆的半径,设置为屏幕的三分之一
    private int radius;
    //屏幕边缘到圆环的距离
    private int distance;
    /* 触摸时作用在Camera的矩阵 */
    private Matrix mCameraMatrix;
    //用于实现3d效果
    private Camera mCamera;
    //处理钟表动画
    private ValueAnimator valueAnimator;
    //camera旋转的最大角度
    private float mMaxCameraRotate = 10;
    /* camera绕X轴旋转的角度 */
    private float mCameraRotateX;
    /* camera绕Y轴旋转的角度 */
    private float mCameraRotateY;
    /* 指针的在x轴的位移 */
    private float mCanvasTranslateX;
    /* 指针的在y轴的位移 */
    private float mCanvasTranslateY;
    /* 指针的最大位移 */
    private float mMaxCanvasTranslate;
    /* 时针角度 */
    private float mHourDegree;
    /* 分针角度 */
    private float mMinuteDegree;
    /* 秒针角度 */
    private float mSecondDegree;

    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;
    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;

    private int weithColor;
    private int grayColor;
    private Canvas mCanvas;

    /* 时针路径 */
    private Path mHourHandPath;
    /* 分针路径 */
    private Path mMinuteHandPath;
    /* 秒针路径 */
    private Path mSecondHandPath;
    private RectF mCircleRectF;
    public OclockView(Context context) {
        this(context,null);
    }

    public OclockView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public OclockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.parseColor("#88ffffff"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        center_paint = new Paint();
        center_paint.setColor(Color.parseColor("#ffffff"));
        center_paint.setStyle(Paint.Style.STROKE);
        center_paint.setAntiAlias(true);
        center_paint.setStrokeWidth(13);
        time_paint = new Paint();
        time_paint.setColor(Color.parseColor("#88ffffff"));
        time_paint.setStyle(Paint.Style.STROKE);
        time_paint.setAntiAlias(true);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{5,5},0);
        time_paint.setPathEffect(dashPathEffect);

        mCameraMatrix = new Matrix();
        mCamera = new Camera();

        mGradientMatrix = new Matrix();

        weithColor = Color.parseColor("#ffffff");
        grayColor = Color.parseColor("#88ffffff");

        mSecondHandPath = new Path();
        mMinuteHandPath = new Path();
        mHourHandPath = new Path();

        sPaint = new Paint();
        sPaint.setColor(Color.parseColor("#ffffff"));
        sPaint.setStyle(Paint.Style.FILL);
        sPaint.setAntiAlias(true);

        change_paint = new Paint();
        change_paint.setStyle(Paint.Style.STROKE);
        change_paint.setAntiAlias(true);
        DashPathEffect dashPathEffect2 = new DashPathEffect(new float[]{5,5},0);
        change_paint.setPathEffect(dashPathEffect2);

        mCircleRectF = new RectF();
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#ffffff"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        hPaint = new Paint();
        hPaint.setColor(Color.parseColor("#88ffffff"));
        hPaint.setStyle(Paint.Style.FILL);
        hPaint.setAntiAlias(true);
    }

    private void getTime() {
        Calendar calendar = Calendar.getInstance();
        //毫秒
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        //秒
        float second = calendar.get(Calendar.SECOND)+milliSecond/1000;
        //分
        float minute = calendar.get(Calendar.MINUTE)+second/60;
        //时
        float hour = calendar.get(Calendar.HOUR)+minute/60;
        //求出三个指针的角度
        mSecondDegree = second*6;
        mMinuteDegree = 6*minute;
        mHourDegree = 30*hour;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas=canvas;
        //Camera实现3D效果
        mCameraMatrix.reset();
        mCamera.save();
        mCamera.rotateX(mCameraRotateX);
        mCamera.rotateY(mCameraRotateY);
        mCamera.getMatrix(mCameraMatrix);
        mCamera.restore();
        mCameraMatrix.preTranslate(-getWidth()/2,-getHeight()/2);
        mCameraMatrix.postTranslate(getWidth()/2,getHeight()/2);
        canvas.concat(mCameraMatrix);

        //获取系统时间
        getTime();


        //1、先把3,6,9,12四大刻度文字写出来，涉及到文字基准线和字体长度，所以微调一下
        paint.setTextSize(30);
        canvas.drawText("12",width/2-20,distance+10,paint);
        canvas.drawText("3",width-distance-10,distance+10+radius,paint);
        canvas.drawText("6",width/2-10,width-distance+10,paint);
        canvas.drawText("9",distance-10,distance+radius+10,paint);

        //2、把时钟外层轮廓 补全,注意留下一点间距(给圆心角流出5度的宽余),也就是画圆弧,
        canvas.drawArc(distance,distance,distance+radius*2,distance+radius*2,-85,80,false,paint);
        canvas.drawArc(distance,distance,distance+radius*2,distance+radius*2,5,80,false,paint);
        canvas.drawArc(distance,distance,distance+radius*2,distance+radius*2,95,80,false,paint);
        canvas.drawArc(distance,distance,distance+radius*2,distance+radius*2,185,80,false,paint);

        //确定圆心圆环
//        canvas.drawCircle(mCenterPoint,mCenterPoint,20,center_paint);


        //外弧到刻度的距离,
        canvas.save();
        canvas.translate(mCanvasTranslateX,mCanvasTranslateY);
        //处理颜色渐变
        mGradientMatrix.setRotate(mSecondDegree-90,mCenterPoint,mCenterPoint);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        change_paint.setShader(mSweepGradient);
        int innerdistance = radius/8;
        change_paint.setStrokeWidth(30);
        canvas.drawCircle(mCenterPoint,mCenterPoint,radius-innerdistance,change_paint);
        canvas.restore();

        //画秒针
        drawSPointer();
        //画时针，被分针覆盖
        drawHPointer();
        //画分针
        drawMPointer();

        invalidate();
    }

    private void drawHPointer() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX * 1.2f, mCanvasTranslateY * 1.2f);
        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);
        mHourHandPath.reset();
        float offset = distance;
        mHourHandPath.moveTo(getWidth() / 2 - 0.018f * radius, getHeight() / 2 - 0.03f * radius);
        mHourHandPath.lineTo(getWidth() / 2 - 0.009f * radius, offset + 0.48f * radius);
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.46f * radius,
                getWidth() / 2 + 0.009f * radius, offset + 0.48f * radius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.018f * radius, getHeight() / 2 - 0.03f * radius);
        mHourHandPath.close();
        hPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, hPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * radius, getHeight() / 2 - 0.03f * radius,
                getWidth() / 2 + 0.03f * radius, getHeight() / 2 + 0.03f * radius);
        hPaint.setStyle(Paint.Style.STROKE);
        hPaint.setStrokeWidth(0.01f * radius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, hPaint);
        mCanvas.restore();
    }

    private void drawMPointer() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f);
        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);
        mMinuteHandPath.reset();
        float offset = distance-30;
        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * radius, getHeight() / 2 - 0.03f * radius);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * radius, offset + 0.365f * radius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.345f * radius,
                getWidth() / 2 + 0.008f * radius, offset + 0.365f * radius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * radius, getHeight() / 2 - 0.03f * radius);
        mMinuteHandPath.close();
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * radius, getHeight() / 2 - 0.03f * radius,
                getWidth() / 2 + 0.03f * radius, getHeight() / 2 + 0.03f * radius);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(0.02f * radius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mPaint);
        mCanvas.restore();
    }

    private void drawSPointer() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX,mCanvasTranslateY);
        mCanvas.rotate(mSecondDegree,mCenterPoint,mCenterPoint);
        mSecondHandPath.reset();
        mSecondHandPath.moveTo(mCenterPoint,distance-20+0.26f*radius);
        mSecondHandPath.lineTo(mCenterPoint - 0.05f * radius, distance-20+0.34f * radius);
        mSecondHandPath.lineTo(mCenterPoint+ 0.05f * radius, distance-20 + 0.34f * radius);
        mSecondHandPath.close();
        mCanvas.drawPath(mSecondHandPath, sPaint);
        mCanvas.restore();



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        //圆心坐标
        mCenterPoint = width/2;
        //圆半径
        radius = width/3;
        //圆环距屏幕边缘距离,都会算吧  哈哈哈
        distance = width/6;
        mMaxCanvasTranslate = 0.02f * radius;

        mSweepGradient = new SweepGradient(mCenterPoint, mCenterPoint,
                new int[]{grayColor, weithColor}, new float[]{0.75f, 1});
        setMeasuredDimension(width, width );
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (valueAnimator!=null&&valueAnimator.isRunning()){
                    valueAnimator.cancel();
                }
                getViewRotate(event);
                getViewTranslate(event);
                break;
            case MotionEvent.ACTION_MOVE:
                getViewRotate(event);
                getViewTranslate(event);
                break;
            case MotionEvent.ACTION_UP:
                startOclockAnmi();
                break;
        }
        return true;
    }

    private void startOclockAnmi() {
        final String camerRotateXName = "cameraRotateX";
        final String camerRotateYName = "cameraRotateY";
        final String canvasTranslateXName = "canvasTranslateX";
        final String canvasTranslateYName = "canvasTranslateY";
        PropertyValuesHolder cameraRotateXHolder =
                PropertyValuesHolder.ofFloat(camerRotateXName, mCameraRotateX, 0);
        PropertyValuesHolder cameraRotateYHolder =
                PropertyValuesHolder.ofFloat(camerRotateYName, mCameraRotateY, 0);
        PropertyValuesHolder canvasTranslateXHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateXName, mCanvasTranslateX, 0);
        PropertyValuesHolder canvasTranslateYHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateYName, mCanvasTranslateY, 0);
        valueAnimator= ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder,
                cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder);

        valueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                //http://inloop.github.io/interpolator/
                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCameraRotateX = (float) animation.getAnimatedValue(camerRotateXName);
                mCameraRotateY = (float) animation.getAnimatedValue(camerRotateXName);
                mCanvasTranslateX = (float) animation.getAnimatedValue(canvasTranslateXName);
                mCanvasTranslateY = (float) animation.getAnimatedValue(canvasTranslateYName);
            }
        });
        valueAnimator.start();

    }

    private void getViewTranslate(MotionEvent event) {
        float translateX = (event.getX() - getWidth() / 2);
        float translateY = (event.getY() - getHeight() / 2);
        //求出此时位移的大小与半径之比
        float[] percentArr = getPercent(translateX, translateY);
        //最终位移的大小按比例匀称改变
        mCanvasTranslateX = percentArr[0] * mMaxCanvasTranslate;
        mCanvasTranslateY = percentArr[1] * mMaxCanvasTranslate;
    }

    //获取camera旋转角度
    private void getViewRotate(MotionEvent event) {
        float retateX = -(event.getY()-getHeight()/2);
        float retateY = -(event.getX()-getWidth()/2);
        //求出自旋转大小与半径比
        float[] precentArc = getPercent(retateX,retateY);
        mCameraRotateX = precentArc[0]*mMaxCameraRotate;
        mCameraRotateY = precentArc[1]*mMaxCameraRotate;

    }

    // 获取一个操作旋转或位移大小的比例
    private float[] getPercent(float x, float y) {
        float[] percentArr = new float[2];
        float percentX = x / radius;
        float percentY = y / radius;
        if (percentX > 1) {
            percentX = 1;
        } else if (percentX < -1) {
            percentX = -1;
        }
        if (percentY > 1) {
            percentY = 1;
        } else if (percentY < -1) {
            percentY = -1;
        }
        percentArr[0] = percentX;
        percentArr[1] = percentY;
        return percentArr;
    }
}
