package com.startup.colleague.view;

import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Zane on 2015/3/3.
 */
public class XTextView extends TextView {
    private String content;
    private int width;
    private Paint paint;
    private int xPadding;
    private int yPadding;
    private int textHeight;
    private int xPaddingMin;
    int count;
    //记录每个字的二维数组
    int[][] position;

    public XTextView(Context context) {
        super(context);
        init();
    }

    public XTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        //TODO 设置画笔颜色，也就是字体颜色
        paint.setColor(Color.YELLOW);
        ///paint.setTypeface(Typeface.DEFAULT);
        //TODO 设置画笔大小，也就是字体大小
        paint.setTextSize(dip2px(this.getContext(), 19f));
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        String familyName = "宋体";
        
//        Typeface font = Typeface.createFromAsset(getContext().getAssets(),"fonts/huakangwwt.ttf");
        Typeface font = Typeface.create(familyName,Typeface.BOLD);
        paint.setColor(Color.YELLOW);
        paint.setTypeface(font);
        
        Paint.FontMetrics fm = paint.getFontMetrics();// 得到系统默认字体属性
        // TODO 设置字体高度
        textHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
        //TODO 设置字间距
        xPadding = dip2px(this.getContext(), 4f);
        //TODO 设置行间距
        yPadding = dip2px(this.getContext(), 5f);
        //TODO 设置比较小的字间距（字母和数字应紧凑）
        xPaddingMin = dip2px(this.getContext(), 2f);
    }

    public void setText(String str) {
        //获得设备的宽
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        width = display.getWidth()-100;
        getPositions(str);
        //重新画控件，将会调用onDraw方法
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(content)) {
            for (int i = 0; i < count; i++) {
                canvas.drawText(String.valueOf(content.charAt(i)), position[i][0],position[i][1], paint);
            }
        }
    }

    public void getPositions(String content) {
        this.content = content;
        char ch;
        //输入点的 x的坐标
        int x = 30;
        //当前行数
        int lineNum = 1;
        count = content.length();
        //初始化字体位置数组
        position=new int[count][2];
        for (int i = 0; i < count; i++) {
            ch =content.charAt(i);
            String str = String.valueOf(ch);

            //根据画笔获得每一个字符的显示的rect 就是包围框（获得字符宽度）
            Rect rect = new Rect();
            paint.getTextBounds(str, 0, 1, rect);
            int strwidth = rect.width();

            //TODO 对有些标点做些处理
            if (str.equals("《") || str.equals("（") || str.equals("，") || str.equals("。")) {
                strwidth += xPaddingMin * 2;
            }
            //当前行的宽度
            float textWith = strwidth;
            //没画字前预判看是否会出界
            x += textWith;
            //出界就换行
            if (x > width) {
                lineNum++;// 真实的行数加一
                x = 30;
            } else {
                //回到预判前的位置
                x -= textWith;
            }
            //记录每一个字的位置
            position[i][0]=x;
            position[i][1]=textHeight * lineNum + yPadding * (lineNum - 1);
            //判断是否是数字还是字母 （数字和字母应该紧凑点）
            //每次输入完毕 进入下一个输入位置准备就绪
            if (isNumOrLetters(str)) {
                x += textWith + xPaddingMin;
            } else {
                x += textWith + xPadding;
            }
        }
        //根据所画的内容设置控件的高度
        this.setHeight((textHeight +yPadding) * lineNum);
    }

    //工具类：判断是否是字母或者数字
    public boolean isNumOrLetters(String str){
        String regEx="^[A-Za-z0-9_]+$";
        Pattern p= Pattern.compile(regEx);
        Matcher m=p.matcher(str);
        return m.matches();
    }

    // 工具类：在代码中使用dp的方法（因为代码中直接用数字表示的是像素）
    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }
}
