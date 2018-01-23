package anthony.cameralibrary.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import anthony.cameralibrary.util.SizeUtils;

/**
 * Created by Administrator on 2017/6/5.
 */

public class RingView extends View {

    private Paint mInCirPaint;
    private Paint mArcPaint;
    private int mWidth;
    private int mHeight;
    private float mArcWidth;
    private float percentRing;
    private float percentCircle;
    private RectF rectFRing;
    private long downTimeMillisme;
    private long uPTimeMillis;
    private Paint mOutCirPaint;

    private float outRadius = 0;//外圆半径
    private float inRadius = 0;//内圆半径
    private int ringRadius = 0;//外圈半径
    private int widthRing;
    private int outChange;
    private int inChange;
    private int time;

    private OnRecordListener onRecordListener;
    private boolean isTakePhoto = false;

    public RingView(Context context) {
        this(context, null);
    }

    public RingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化画笔
        initPaint();
        //初始化半径值
        outRadius = SizeUtils.dp2px(context, 50);
        inRadius = SizeUtils.dp2px(context, 40);
        ringRadius = SizeUtils.dp2px(context, 68);
        //外圆变大的变化值
        outChange = SizeUtils.dp2px(context, 20);
        //内圆变小的变化值
        inChange = SizeUtils.dp2px(context, 10);
        //外边环的宽度
        widthRing = SizeUtils.dp2px(context, 4);

    }

    private void initPaint() {
        //内圆画笔
        mInCirPaint = new Paint();
        mInCirPaint.setColor(Color.WHITE);
        mInCirPaint.setStyle(Paint.Style.FILL);//填充模式
        //外圆画笔
        mOutCirPaint = new Paint();
        mOutCirPaint.setColor(Color.parseColor("#999999"));
        mOutCirPaint.setStyle(Paint.Style.FILL);//填充模式
        //进度条画笔
        mArcPaint = new Paint();
        mArcPaint.setColor(Color.GREEN);//画笔颜色
        mArcPaint.setAntiAlias(true);//抗锯齿
        mArcPaint.setStyle(Paint.Style.STROKE);//空心
        mArcPaint.setDither(true);//防抖动
        mArcPaint.setStrokeCap(Paint.Cap.SQUARE);//在画笔的起始处是平的
        mArcPaint.setPathEffect(new CornerPathEffect(2));//画笔效果
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        rectFRing = new RectF(mWidth / 2 - ringRadius, mHeight / 2 - ringRadius, mWidth / 2 + ringRadius, mHeight / 2 + ringRadius);
        //画笔的宽度一定要在这里设置才能自适应
        mArcPaint.setStrokeWidth(widthRing);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOutCircle(canvas);
        drawInCircle(canvas);
        drawRingCircle(canvas);
    }


    private void drawOutCircle(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, outRadius + outChange * percentCircle, mOutCirPaint);
    }

    private void drawInCircle(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, inRadius - inChange * percentCircle, mInCirPaint);
    }

    private void drawRingCircle(Canvas canvas) {
        canvas.drawArc(rectFRing, 270, percentRing * 360, false, mArcPaint);
    }


    private void getAngle() {
        //圆环动画
        final ValueAnimator percentRingAnimator = ValueAnimator.ofFloat(0, 1);
        percentRingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //手指抬起取消录制
                if (isTouchup) {
                    percentRingAnimator.cancel();
                    //内圆外圆重置
                    percentCircle = 0;
                    percentRing = 0;
                    invalidate();
                    //停止录制
                    stopRecordVideo();
                } else {
                    percentRing = (float) animation.getAnimatedValue();
                    invalidate();
                    if (percentRing == 1) {//停止录制
                        stopRecordVideo();
                        //内圆外圆重置
                        percentCircle = 0;
                        percentRing = 0;
                        invalidate();
                    }
                }
            }


        });
        percentRingAnimator.setDuration(10000);//单位：秒
        percentRingAnimator.start();
    }


    private boolean isRecord = true;
    private boolean isTouchup = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //如果处于拍照时就不能在进行点击
                if (isTakePhoto) {
                    return false;
                }
                //要保证按的是在按钮之上
                if (mWidth / 2 - inRadius > x || x > mWidth / 2 + inRadius || mHeight / 2 - inRadius > y || y > mHeight / 2 + inRadius) {
                    return false;
                }
                //设置录像状态
                isRecord = true;
                //设置抬起状态
                isTouchup = false;
                //绘制内圆和外圆的变化
                setCircleAni();
                //按下时间
                downTimeMillisme = System.currentTimeMillis();

                break;
            case MotionEvent.ACTION_UP:
                uPTimeMillis = System.currentTimeMillis();
                //如果手指按下到抬起时间小于1秒就是拍照
                if (uPTimeMillis - downTimeMillisme <= 1000) {
                    isRecord = false;
                    //拍照
                }
                //手指抬起事件取消
                isTouchup = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void setCircleAni() {
        //圆环动画
        final ValueAnimator cirlPercentAnimator = ValueAnimator.ofFloat(0, 1);
        cirlPercentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //手指抬起取消录制,一秒之内就是拍照
                if (isTouchup) {
                    //内圆外圆重置
                    if (null != onRecordListener) {
                        //这个时候就是拍照
                        isTakePhoto = true;
                        onRecordListener.takePhoto();
                    }
                    percentCircle = 0;
                    //这个地方是去更新内圆外圆的大小变化,重置内圆外圆原来大小
                    invalidate();
                    cirlPercentAnimator.cancel();
                } else {
                    percentCircle = (float) animation.getAnimatedValue();
                    //更新外圆内圆大小
                    invalidate();
                    if (percentCircle == 1) {//长按到了一分钟(这个时间都是自己控制的),说明动画结束了,就要开始录制视频
                        startRecord();
                    }
                }
            }


        });
        cirlPercentAnimator.setDuration(1000);
        cirlPercentAnimator.start();

    }

    /**
     * 开始录制
     */
    private void startRecord() {
        //绘制扫描的角度
        getAngle();
        //调接口开始录像
        startRecordVideo();
    }

    /**
     * 开始录制视频
     */
    private void startRecordVideo() {
        if (null != onRecordListener) {
            onRecordListener.startRecord();
        }
    }

    /**
     * 停止录制
     */
    private void stopRecordVideo() {
        if (null != onRecordListener) {
            onRecordListener.stopRecord();
        }
    }

    public interface OnRecordListener {
        void startRecord();

        void stopRecord();

        void takePhoto();
    }

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    public void setTakePhoto(boolean takePhoto) {
        isTakePhoto = takePhoto;
    }
}
