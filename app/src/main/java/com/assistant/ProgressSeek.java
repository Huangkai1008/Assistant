package com.assistant;

/**
 * Created by virtualC9 on 2017/9/14.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class ProgressSeek extends View {
    /**
     * 进度条的宽度
     */
    private int view_width;
    /**
     * 画布的宽度
     */
    private int view_base_width;
    /**
     * 控件的宽度
     */
    private int view_edge_width;
    /**
     * 进度
     */
    private int progress;
    private Canvas cacheCanvas;
    /**
     * 背景颜色的画笔
     */
    private Paint backgroundPaint;
    /**
     * 进度条的画笔
     */
    private Paint progressPaint;
    /**
     * 进度末端的图
     */
    private Bitmap bitmap;
    private int bitmapWidth;
    private int bitmapHeight;
    private Context context;
    //渐变色开始
    private static final int DEFAULT_START_COLOR = Color.parseColor("#34DAB5");
    //渐变色结束
    private static final int DEFAULT_END_COLOR = Color.parseColor("#27A5FE");
    /**
     * 缓存图片
     */
    private Bitmap cacheBitmap;
    public ProgressSeek(Context context) {
        super(context);
        initView(context);
    }
    public ProgressSeek(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    public ProgressSeek(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context) {
        this.context = context;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.thumb);
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        backgroundPaint = new Paint();
        backgroundPaint.setStrokeWidth(bitmapWidth);
        backgroundPaint.setColor(Color.parseColor("#cccccc"));
        backgroundPaint.setDither(true);
        backgroundPaint.setAntiAlias(true);
        progressPaint = new Paint();
        progressPaint.setStrokeWidth(bitmapWidth);
        progressPaint.setDither(true);
        progressPaint.setAntiAlias(true);
        DisplayMetrics d = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(d);
        view_base_width = d.widthPixels;
    }
    public void init(int progress) {
        this.progress = progress;
        if (view_width == 0) {
            view_width = view_base_width * progress / 100;
        } else {
            view_width = view_edge_width * progress / 100;
        }
        if (cacheBitmap != null) {
            if (!cacheBitmap.isRecycled()) {
                cacheBitmap.recycle();
                cacheBitmap = null;
            }
            cacheCanvas = null;
        }
        cacheBitmap = Bitmap.createBitmap(view_base_width, bitmapHeight * 2, Bitmap.Config.ARGB_8888);
        if (cacheCanvas == null) {
            cacheCanvas = new Canvas();
            cacheCanvas.setBitmap(cacheBitmap);
        }
/**
 * 画背景
 */
        RectF r = new RectF();
        r.left = 0;
        r.top = bitmapHeight;
        r.right = view_base_width;
        r.bottom = bitmapWidth + 10;
        cacheCanvas.drawRoundRect(r, 5f, 5f, backgroundPaint);
        if (progress > 0) {
            LinearGradient lg = new LinearGradient(0, 0, view_width, bitmapWidth, DEFAULT_START_COLOR, DEFAULT_END_COLOR, Shader.TileMode.CLAMP);
            progressPaint.setShader(lg);
            RectF r1 = new RectF();
            r.left = 0;
            r.top = bitmapHeight;
            r.right = view_width;
            r.bottom = bitmapWidth + 10;
            cacheCanvas.drawRoundRect(r, 5f, 5f, progressPaint);
            cacheCanvas.drawBitmap(bitmap, view_width - bitmapWidth+8, bitmapHeight / 2 + 6, new Paint());
        }
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint bmpPaint = new Paint();
//将cacheBitmap绘制到该View组件
        if (cacheBitmap != null) {
            canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
        }
        view_edge_width = this.getWidth();
        //Log.e("打出来看看控件的宽度:", view_edge_width + "");
        init(progress);
    }
}
