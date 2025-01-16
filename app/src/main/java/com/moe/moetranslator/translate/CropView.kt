/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.translate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

const val RECT_MIN_WIDTH = 50f//框选的最小宽度px
const val RECT_MIN_HEIGHT = 50f//框选的最小高度px

class CropView(ctx:Context) : View(ctx) {
    private val mActionMovePoint = Point()  //point，储存x，y两个坐标
    private val mOriPoint = Point() //point，储存原来的x，y两个坐标
    val absolutePointOffset = Point() //point，绝对坐标偏移量
    lateinit var mRect : RectF  //矩形信息，mRect即为最初的矩形
    private lateinit var mInitRect : RectF  //最开始的裁剪框
    private var mRectLeft : RectF = RectF() //左侧矩形阴影区域
    private var mRectTop : RectF = RectF()  //上侧矩形阴影区域
    private var mRectRight : RectF = RectF()    //右侧矩形阴影区域
    private var mRectBottom : RectF = RectF()   //下侧矩形阴影区域
    private var mPressPointIndex = -1   //按下的8个顶点的编号，0-7表示顶点，-1表示松手
    private var actionDownRectLeft = 0f //按下时的左
    private var actionDownRectTop = 0f  //按下时的顶
    private var actionDownRectRight = 0f    //按下时的右
    private var actionDownRectBottom = 0f   //按下时的底

    fun setRect(rectF: RectF) {  //设置初始矩形宽高
        mRect = RectF(rectF)
        mInitRect = RectF(rectF)

        // 等待视图布局完成后再获取位置
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 获取位置
                absolutePointOffset.x = getViewOffset().x
                absolutePointOffset.y = getViewOffset().y
                Log.d("OFFSET", "x: ${absolutePointOffset.x} y: ${absolutePointOffset.y}")

                // 移除监听器，避免重复调用
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint() //新建画笔
        paint.color = Color.argb(136, 250, 199, 183) //画笔颜色，未被框选位置的透明黑色
        paint.style = Paint.Style.FILL //设置画笔为填充，即填充未被框选部位的颜色（默认也是填充，这句代码可不写）
        mRectTop.set(0f, 0f, width.toFloat(), mRect.top)
        mRectLeft.set(0f, mRect.top, mRect.left, mRect.bottom)
        mRectRight.set(mRect.right, mRect.top, width.toFloat(), mRect.bottom)
        mRectBottom.set(0f, mRect.bottom, width.toFloat(), height.toFloat())
        canvas.drawRect(mRectLeft, paint)
        canvas.drawRect(mRectTop, paint)
        canvas.drawRect(mRectRight, paint)
        canvas.drawRect(mRectBottom, paint)

        paint.color = Color.rgb(93,79,87) //画笔颜色，不必多说
        paint.style = Paint.Style.STROKE //设置填充样式
        paint.strokeWidth = 4f //画笔宽度
        canvas.drawRect(mRect, paint) //开画选择框

        //下面这些是画8个较粗边框的代码
        val midHorizontal = (mRect.left + mRect.right) / 2 //水平方向中点
        val midVertical = (mRect.top + mRect.bottom) / 2 //垂直方向中点
        paint.strokeWidth = 10f
        val pts = floatArrayOf(
            mRect.left - 5, mRect.top, mRect.left + 40, mRect.top,
            midHorizontal - 20, mRect.top, midHorizontal + 20, mRect.top,
            mRect.right - 40, mRect.top, mRect.right + 5, mRect.top,
            mRect.left, mRect.top, mRect.left, mRect.top + 40,
            mRect.right, mRect.top, mRect.right, mRect.top + 40,
            mRect.left, midVertical - 20, mRect.left, midVertical + 20,
            mRect.right, midVertical - 20, mRect.right, midVertical + 20,
            mRect.left, mRect.bottom, mRect.left, mRect.bottom - 40,
            mRect.right, mRect.bottom, mRect.right, mRect.bottom - 40,
            mRect.left - 5, mRect.bottom, mRect.left + 40, mRect.bottom,
            midHorizontal - 20, mRect.bottom, midHorizontal + 20, mRect.bottom,
            mRect.right - 40, mRect.bottom, mRect.right + 5, mRect.bottom
        )
        canvas.drawLines(pts, paint)
    }

    private fun getViewOffset(): Point {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return Point(location[0], location[1])
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        mActionMovePoint.x = event.x.toInt() //获取触摸的x坐标
        mActionMovePoint.y = event.y.toInt() //获取触摸的y坐标

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mOriPoint.x = event.x.toInt()
                mOriPoint.y = event.y.toInt()
                actionDownRectLeft = mRect.left //按下左 = 原来左
                actionDownRectTop = mRect.top //按下顶 = 原来顶
                actionDownRectRight = mRect.right //按下右 = 原来右
                actionDownRectBottom = mRect.bottom //按下底 = 原来底
                return getPosition(
                    mActionMovePoint.x.toFloat(),
                    mActionMovePoint.y.toFloat()
                ) != -1 //如果焦点不在八个点上则不消耗事件，由DragScaleView来处理
            }

            MotionEvent.ACTION_MOVE -> {
                when (getPosition(mActionMovePoint.x.toFloat(), mActionMovePoint.y.toFloat())) {
                    0 -> {
                        mRect.left = mActionMovePoint.x.toFloat() //更新矩形框顶点
                        mRect.top = mActionMovePoint.y.toFloat() //更新矩形框顶点
                        //设置裁剪框可缩放到的最小区域大小
                        if (actionDownRectRight - mActionMovePoint.x < RECT_MIN_WIDTH) {  //如果右减左（矩形框的宽）小于最小宽度
                            mRect.left = actionDownRectRight - RECT_MIN_WIDTH //则矩形框宽度变为最小宽度
                        }
                        if (actionDownRectBottom - mActionMovePoint.y < RECT_MIN_HEIGHT) {    //如果下减上（矩形框的高）小于最小高度
                            mRect.top = actionDownRectBottom - RECT_MIN_HEIGHT //则矩形框高度变为最小高度
                        }
                        mPressPointIndex = 0 //因为在拖动，因此不改变编号
                    }

                    1 -> {
                        mRect.top = mActionMovePoint.y.toFloat()
                        if (actionDownRectBottom - mActionMovePoint.y < RECT_MIN_HEIGHT) {
                            mRect.top = actionDownRectBottom - RECT_MIN_HEIGHT
                        }
                        mPressPointIndex = 1
                    }

                    2 -> {
                        mRect.right = mActionMovePoint.x.toFloat()
                        mRect.top = mActionMovePoint.y.toFloat()
                        if (mActionMovePoint.x - actionDownRectLeft < RECT_MIN_WIDTH) {
                            mRect.right = actionDownRectLeft + RECT_MIN_WIDTH
                        }
                        if (actionDownRectBottom - mActionMovePoint.y < RECT_MIN_HEIGHT) {
                            mRect.top = actionDownRectBottom - RECT_MIN_HEIGHT
                        }
                        mPressPointIndex = 2
                    }

                    3 -> {
                        mRect.left = mActionMovePoint.x.toFloat()
                        if (actionDownRectRight - mActionMovePoint.x < RECT_MIN_WIDTH) {
                            mRect.left = actionDownRectRight - RECT_MIN_WIDTH
                        }
                        mPressPointIndex = 3
                    }

                    4 -> {
                        mRect.right = mActionMovePoint.x.toFloat()
                        if (mActionMovePoint.x - actionDownRectLeft < RECT_MIN_WIDTH) {
                            mRect.right = actionDownRectLeft + RECT_MIN_WIDTH
                        }
                        mPressPointIndex = 4
                    }

                    5 -> {
                        mRect.left = mActionMovePoint.x.toFloat()
                        mRect.bottom = mActionMovePoint.y.toFloat()
                        if (actionDownRectRight - mActionMovePoint.x < RECT_MIN_WIDTH) {
                            mRect.left = actionDownRectRight - RECT_MIN_WIDTH
                        }
                        if (mActionMovePoint.y - actionDownRectTop < RECT_MIN_HEIGHT) {
                            mRect.bottom = actionDownRectTop + RECT_MIN_HEIGHT
                        }
                        mPressPointIndex = 5
                    }

                    6 -> {
                        mRect.bottom = mActionMovePoint.y.toFloat()
                        if (mActionMovePoint.y - actionDownRectTop < RECT_MIN_HEIGHT) {
                            mRect.bottom = actionDownRectTop + RECT_MIN_HEIGHT
                        }
                        mPressPointIndex = 6
                    }

                    7 -> {
                        mRect.right = mActionMovePoint.x.toFloat()
                        mRect.bottom = mActionMovePoint.y.toFloat()
                        if (mActionMovePoint.x - actionDownRectLeft < RECT_MIN_WIDTH) {
                            mRect.right = actionDownRectLeft + RECT_MIN_WIDTH
                        }
                        if (mActionMovePoint.y - actionDownRectTop < RECT_MIN_HEIGHT) {
                            mRect.bottom = actionDownRectTop + RECT_MIN_HEIGHT
                        }
                        mPressPointIndex = 7
                    }

                    8 -> {
                        //x的更新策略
                        if ((mRect.left <= 5) && (mActionMovePoint.x - mOriPoint.x < 0)) {    //贴近左边缘
                            Log.d("Message", "close to left edge")
                        } else if ((mRect.right >= width - 5) && (mActionMovePoint.x - mOriPoint.x > 0)) { //贴近右边缘
                            Log.d("Message", "close to right edge")
                        } else {
                            mRect.left = mRect.left + (mActionMovePoint.x - mOriPoint.x)
                            mRect.right = mRect.right + (mActionMovePoint.x - mOriPoint.x)
                        }
                        //y的更新策略
                        if ((mRect.top <= 5) && (mActionMovePoint.y - mOriPoint.y) < 0) {    //贴近左边缘
                            Log.d("Message", "close to top edge")
                        } else if ((mRect.bottom >= height - 5) && (mActionMovePoint.y - mOriPoint.y > 0)) { //贴近右边缘
                            Log.d("Message", "close to bottom edge")
                        } else {
                            mRect.top = mRect.top + (mActionMovePoint.y - mOriPoint.y)
                            mRect.bottom = mRect.bottom + (mActionMovePoint.y - mOriPoint.y)
                        }
                        mOriPoint.x = event.x.toInt()
                        mOriPoint.y = event.y.toInt()
                        mPressPointIndex = 8
                    }
                }
                invalidate()
                return mPressPointIndex != -1
            }

            MotionEvent.ACTION_UP -> {
                //抬手检查
                if(mRect.left<5){
                    mRect.left = 5f
                }
                if(mRect.right>width-5){
                    mRect.right = width-5f
                }
                if(mRect.top < 5){
                    mRect.top = 5f
                }
                if(mRect.bottom > height-5){
                    mRect.bottom = height-5f
                }
                invalidate()
                mPressPointIndex = -1
            }
        }
        return true
    }

    private fun getPosition(x: Float, y: Float): Int {
        if (mPressPointIndex > -1 && mPressPointIndex < 9) {
            return mPressPointIndex
        }
        val POINT_RADIUS = 900 //触点可响应区域的半径平方
        if ((x - mRect.left) * (x - mRect.left) + (y - mRect.top) * (y - mRect.top) < POINT_RADIUS) {
            return 0
        } else if ((x - (mRect.left + mRect.right) / 2) * (x - (mRect.left + mRect.right) / 2) +
            (y - mRect.top) * (y - mRect.top) < POINT_RADIUS
        ) {
            return 1
        } else if ((x - mRect.right) * (x - mRect.right) + (y - mRect.top) * (y - mRect.top) < POINT_RADIUS) {
            return 2
        } else if ((x - mRect.left) * (x - mRect.left) + (y - (mRect.top + mRect.bottom) / 2) *
            (y - (mRect.top + mRect.bottom) / 2) < POINT_RADIUS
        ) {
            return 3
        } else if ((x - mRect.right) * (x - mRect.right) + (y - (mRect.top + mRect.bottom) / 2) *
            (y - (mRect.top + mRect.bottom) / 2) < POINT_RADIUS
        ) {
            return 4
        } else if ((x - mRect.left) * (x - mRect.left) + (y - mRect.bottom) * (y - mRect.bottom) < POINT_RADIUS) {
            return 5
        } else if ((x - (mRect.left + mRect.right) / 2) * (x - (mRect.left + mRect.right) / 2) +
            (y - mRect.bottom) * (y - mRect.bottom) < POINT_RADIUS
        ) {
            return 6
        } else if ((x - mRect.right) * (x - mRect.right) + (y - mRect.bottom) * (y - mRect.bottom) < POINT_RADIUS) {
            return 7
        } else if ((x > mRect.left) && (x < mRect.right) && (y > mRect.top) && (y < mRect.bottom)) {
            return 8
        }
        return -1
    }
}