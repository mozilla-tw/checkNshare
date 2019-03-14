package org.mozilla.check.n.share.widget

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View

class VerticalTextView : View {

    private val paint = Paint()
    private var mTextPosx = 0// x坐标
    private var mTextPosy = 0// y坐标
    //获取实际宽度
    var textWidth = 0
        private set// 绘制宽度
    private var mFontHeight = 0// 绘制字体高度
    private var mFontSize = 24f// 字体大小
    private var mRealLine = 0// 字符串真实的行数
    private var mLineWidth = 0//列宽度
    private var TextLength = 0//字符串长度
    private var MaxHeight = 500
    private var text = ""//待显示的文字
    private val textStartAlign = Align.RIGHT//draw start left or right.//default right
    private var drawable: BitmapDrawable? = null

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        paint.textAlign = Align.CENTER//文字居中
        paint.isAntiAlias = true//平滑处理
        paint.color = Color.BLACK//默认文字颜色
        try {
            mFontSize = java.lang.Float.parseFloat(attrs.getAttributeValue(null, "textSize"))//获取字体大小属性
        } catch (e: Exception) {
        }

    }

    override fun setBackground(background: Drawable?) {
        super.setBackground(background)
        if(background is BitmapDrawable){
            drawable = background
        }
    }

    //设置文字
    fun setText(text: String) {
        this.text = text
        this.TextLength = text.length
    }

    //设置字体大小
    fun setTextSize(size: Float) {
        if (size != paint.textSize) {
            mFontSize = size
        }
    }

    //设置字体颜色
    fun setTextColor(color: Int) {
        paint.color = color
    }

    //设置字体颜色
    fun setTextARGB(a: Int, r: Int, g: Int, b: Int) {
        paint.setARGB(a, r, g, b)
    }

    //设置字体
    fun setTypeface(tf: Typeface) {
        if (this.paint.typeface !== tf) {
            this.paint.typeface = tf
        }
    }

    //设置行宽
    fun setLineWidth(LineWidth: Int) {
        mLineWidth = LineWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.v("VerticalTextView", "onDraw")
        if (drawable != null) {
            //画背景
            val b = Bitmap.createBitmap(drawable!!.bitmap, 0, 0, textWidth, MaxHeight)
            canvas.drawBitmap(b, matrix, paint)
        }
        //画字
        draw(canvas, this.text)
    }

    private fun draw(canvas: Canvas, thetext: String) {
        var ch: Char
        mTextPosy = 0//初始化y坐标
        mTextPosx = if (textStartAlign == Align.LEFT) mLineWidth else textWidth - mLineWidth//初始化x坐标
        var i = 0
        while (i < this.TextLength) {
            ch = thetext[i]
            if (ch == '\n') {
                if (textStartAlign == Align.LEFT) {
                    mTextPosx += mLineWidth// 换列
                } else {
                    mTextPosx -= mLineWidth// 换列
                }
                mTextPosy = 0
            } else {
                mTextPosy += mFontHeight
                if (mTextPosy > this.MaxHeight) {
                    if (textStartAlign == Align.LEFT) {
                        mTextPosx += mLineWidth// 换列
                    } else {
                        mTextPosx -= mLineWidth// 换列
                    }
                    i--
                    mTextPosy = 0
                } else {
                    canvas.drawText(ch.toString(), mTextPosx.toFloat(), mTextPosy.toFloat(), paint)
                }
            }
            i++
        }

        //调用接口方法
        //activity.getHandler().sendEmptyMessage(TestFontActivity.UPDATE);
    }

    //计算文字行数和总宽
    private fun GetTextInfo(heightMeasureSpec: Int) {
        Log.v("VerticalTextView", "GetTextInfo")
        var ch: Char
        var h = 0
        paint.textSize = mFontSize
        //获得字宽
        if (mLineWidth == 0) {
            val widths = FloatArray(1)
            paint.getTextWidths("正", widths)//获取单个汉字的宽度
            mLineWidth = Math.ceil(widths[0] * 1.1 + 2).toInt()
        }

        val fm = paint.fontMetrics
        mFontHeight = (Math.ceil((fm.descent - fm.top).toDouble()) * 0.9).toInt()// 获得字体高度

        val split = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var maxTextLength = 0
        for (aSplit in split) {
            val length = aSplit.length
            if (length > maxTextLength) {
                maxTextLength = length
            }
        }

        val measureHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        if (measureHeight > 0) {
            val fontHeight = maxTextLength * mFontHeight
            if (fontHeight > measureHeight) {
                MaxHeight = measureHeight
            } else {
                MaxHeight = fontHeight
            }
        }

        //计算文字行数
        mRealLine = 0
        var i = 0
        while (i < this.TextLength) {
            ch = this.text[i]
            if (ch == '\n') {
                mRealLine++// 真实的行数加一
                h = 0
            } else {
                h += mFontHeight
                if (h > (if (MaxHeight > 0) MaxHeight else 500)) {
                    mRealLine++// 真实的行数加一
                    i--
                    h = 0
                } else {
                    if (i == this.TextLength - 1) {
                        mRealLine++// 真实的行数加一
                    }
                }
            }
            i++
        }

        mRealLine++//额外增加一行
        textWidth = mLineWidth * mRealLine//计算文字总宽度
        measure(textWidth, MaxHeight)//重新调整大小
        layout(left, top, left + textWidth, top + MaxHeight)//重新绘制容器
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.v("宽高", "$textWidth:$MaxHeight")
        if (textWidth == 0 || MaxHeight == 0) {
            GetTextInfo(heightMeasureSpec)
        }
        setMeasuredDimension(textWidth, MaxHeight)
    }
}