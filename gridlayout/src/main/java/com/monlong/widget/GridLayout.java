package com.monlong.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * @Descirption: 不具有滑动特性的宫格视图布局（根据指定列数依次排列）
 * @Author: monlong
 * @Email: 826309156@qq.com
 * @Date: 2016-07-17 22:25
 * @Version: 1.0.0
 */
public class GridLayout extends ViewGroup {

    private static final String TAG = "GridLayout";
    private static final boolean DEBUG = true;

    /**
     * 每行中所排列childView数量
     */
    private int mNumColumns;

    /**
     * 单个childView所占宽度
     */
    private int mColumnWidth;

    /**
     * 水平方向childView间隔距离
     */
    private int mHorizontalSpace;

    /**
     * 垂直方向childView间隔距离
     */
    private int mVerticalSpace;

    /**
     * 水平方向首尾childView是否与父布局带有间隔
     */
    private boolean mHorizontalStartEndSpaceEnabled;

    /**
     * 垂直方向首尾childView是否与父布局带有间隔
     */
    private boolean mVerticalStartEndSpaceEnabled;

    /**
     * 垂直方向间隔区域填充颜色
     */
    private int mVerticalSpaceColor;

    /**
     * 水平方向间隔区域填充颜色
     */
    private int mHorizontalSpaceColor;

    /**
     * 最大宽度
     */
    private int mHorizontalMaxWidth;

    /**
     * 垂直方向绘制颜色画笔
     */
    private Paint mVerticalPaint;

    /**
     * 水平方向绘制颜色画笔
     */
    private Paint mHorizontalPaint;

    /**
     * childView适配器
     */
    private ListAdapter mAdapter;

    private AdapterDataSetObserver mDataSetObserver;
    private OnItemClickListener mOnItemClickListener;

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(context, attrs);
    }

    /**
     * 获取xml中设定的属性样式（并加以默认值）
     *
     * @param context
     * @param attrs
     */
    private void obtainStyledAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GridLayout);
        mNumColumns = a.getInteger(R.styleable.GridLayout_numColumns, 1);
        mColumnWidth = (int) a.getDimension(R.styleable.GridLayout_columnWidth, 0);
        mHorizontalSpace = (int) a.getDimension(R.styleable.GridLayout_horizontalSpace, 0);
        mVerticalSpace = (int) a.getDimension(R.styleable.GridLayout_verticalSpace, 0);
        mHorizontalSpaceColor = a.getColor(R.styleable.GridLayout_horizontalSpaceColor, 0);
        mVerticalSpaceColor = a.getColor(R.styleable.GridLayout_verticalSpaceColor, 0);
        mHorizontalStartEndSpaceEnabled = a.getBoolean(R.styleable
                .GridLayout_horizontalStartEndSpace, false);
        mVerticalStartEndSpaceEnabled = a.getBoolean(R.styleable
                .GridLayout_verticalStartEndSpace, false);
        a.recycle();

        mVerticalPaint = new Paint();
        mVerticalPaint.setAntiAlias(true);
        mVerticalPaint.setDither(true);
        mVerticalPaint.setStyle(Paint.Style.FILL);
        mVerticalPaint.setColor(mVerticalSpaceColor);

        mHorizontalPaint = new Paint(mVerticalPaint);
        mHorizontalPaint.setColor(mHorizontalSpaceColor);

        /**
         * 背景为空时容器不调用onDraw方法，需要手动设置透明色
         */
        if (getBackground() == null) {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int childCount = getChildCount();

        if (DEBUG) {
            Log.e(TAG, "gridlayout child count " + childCount);
        }

        /**
         * 当不存在子视图时，直接设置控件宽高为0
         */
        if (childCount <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        int horizontalTotalSpacing = horizontalTotalSpacing();

        /**
         * UNSPECIFIED模式下需要自己计算
         * 此时需要参考设置的columnWidth属性，如果未设置会导致控件无法正确显示
         */
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            if (mColumnWidth > 0) {
                widthSize = mColumnWidth * mNumColumns + horizontalTotalSpacing;
            }
            widthSize += getPaddingLeft() + getPaddingRight();
        }

        // 去除padding大小
        widthSize = widthSize - getPaddingLeft() - getPaddingRight();
        int childHeight = 0;
        // 计算单个元素宽度
        int childWidth = (widthSize - horizontalTotalSpacing) / mNumColumns;

        if (DEBUG) {
            Log.e(TAG, "gridlayout single child width " + childWidth);
        }

        // 子元素为空或隐藏数量统计，用于计算高度
        int childGoneCount = 0;

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                childGoneCount++;
                continue;
            }

            LayoutParams p = child.getLayoutParams();
            if (p == null) {
                p = generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }

            /**
             * 构建子元素测量规格，因为宽度需要手动指定，构建时传递mode时指定为EXACTLY，高度无所谓
             */
            int childWidthSpec = getChildMeasureSpec(
                    MeasureSpec
                            .makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    0, p.width);

            int childHeightSpec = getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0,
                    p.height);

            child.measure(childWidthSpec, childHeightSpec);
            childHeight = child.getMeasuredHeight();
        }

        /**
         * 根据当前控件高度模式、子视图排列行数和子视图高度计算控件高度 测量模式：
         * EXACTLY，表示指定确定的值，不需要计算
         * AT_MOST，需要统计子元素叠加起来后的高度是否要大于指定的最大值
         * UNSPECIFIED，此情况出现在比如控件外层包含Scrollview，这里高度由子元素个数得出
         */
        if (heightMode == MeasureSpec.AT_MOST
                || heightMode == MeasureSpec.UNSPECIFIED) {
            int ourSize = getPaddingTop() + getPaddingBottom();
            if (mVerticalStartEndSpaceEnabled) {
                ourSize += mVerticalSpace;
            }

            final int numColumns = mNumColumns;
            final int count = childCount - childGoneCount;

            for (int i = 0; i < count; i += numColumns) {
                ourSize += childHeight;
                if (count - i <= numColumns) {
                    if (mVerticalStartEndSpaceEnabled) {
                        ourSize += mVerticalSpace;
                    }
                } else {
                    ourSize += mVerticalSpace;
                }

                if (heightMode == MeasureSpec.AT_MOST && ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
            }
            heightSize = ourSize;
        }

        if (DEBUG) {
            Log.e(TAG, "gridlayout width : " + widthSize + "  -----  " + "gridlayout height : " +
                    heightSize);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 水平方向总间隔
     *
     * @return
     */
    private int horizontalTotalSpacing() {
        int spacingNum;
        if (mHorizontalStartEndSpaceEnabled) {
            spacingNum = mNumColumns + 1;
        } else {
            spacingNum = mNumColumns - 1;
        }
        return spacingNum * mHorizontalSpace;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int leftPadding = getPaddingLeft();
        final int topPadding = getPaddingTop();

        int childIndex = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            /**
             * 计算当前视图所处第几行第几列
             * 行 ：index / 列数
             * 列 ：index % 列数
             */
            int col = childIndex % mNumColumns;
            int row = childIndex / mNumColumns;
            int left = col * childWidth + horizontalLeftSpacing(col) + leftPadding;
            int top = row * childHeight + verticalTopSpacing(row) + topPadding;
            int right = left + childWidth;
            int bottom = top + childHeight;

            child.layout(left, top, right, bottom);
            childIndex++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mHorizontalSpace > 0) {
            drawHorizontalSpaceColor(canvas);
        }

        if (mVerticalSpace > 0) {
            drawVerticalSpaceColor(canvas);
        }
    }

    /**
     * 绘制水平方向间隔区域颜色
     *
     * @param canvas
     */
    private void drawHorizontalSpaceColor(Canvas canvas) {
        int childIndex = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int col = childIndex % mNumColumns;
                int row = childIndex / mNumColumns;
                int cleft = col * childWidth + horizontalLeftSpacing(col);
                int ctop = row * childHeight + verticalTopSpacing(row);
                int cright = cleft + childWidth;
                int cbottom = ctop + childHeight;

                // 为第一行子元素需判断是否绘制垂直顶部的间隔区域
                boolean headerSpaceEnabled;
                if (col == 0) {
                    headerSpaceEnabled = mHorizontalStartEndSpaceEnabled;
                } else {
                    headerSpaceEnabled = true;
                }

                // 判断是否绘制元素左边填充颜色
                if (headerSpaceEnabled) {
                    canvas.drawRect(cleft - mHorizontalSpace, ctop, cleft, cbottom,
                            mHorizontalPaint);
                }

                // 有两种情况需要考虑是否绘制元素右边填充颜色
                // 1、当元素处于每一行最后一个元素
                // 2、当元素为布局最后一个元素

                boolean isLastColumn = col == mNumColumns - 1;
                boolean isLastChild = i == count - 1;

                if (!isLastColumn && !isLastChild) {
                    childIndex++;
                    continue;
                }

                boolean horizontalSpaceEnabled = false;

                // 为最后子元素时表示需要绘制右边间隔区域
                if (isLastChild) {
                    horizontalSpaceEnabled = true;
                }

                // 同时为一行中最后一列时则需要判断是否绘制尾部间隔区域
                if (isLastColumn) {
                    horizontalSpaceEnabled = mHorizontalStartEndSpaceEnabled;
                }

                // 绘制
                if (horizontalSpaceEnabled) {
                    canvas.drawRect(cright, ctop, cright + mHorizontalSpace, cbottom,
                            mHorizontalPaint);
                }

                // 当前横向宽度最大值
                mHorizontalMaxWidth = Math.max(mHorizontalMaxWidth, cright);
                childIndex++;
            }
        }
    }

    /**
     * 绘制垂直方向间隔区域颜色
     *
     * @param canvas
     */
    private void drawVerticalSpaceColor(Canvas canvas) {
        int childIndex = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int col = childIndex % mNumColumns;
                int row = childIndex / mNumColumns;
                int cleft = col * childWidth + horizontalLeftSpacing(col);
                int ctop = row * childHeight + verticalTopSpacing(row);
                int cright = cleft + childWidth;
                int cbottom = ctop + childHeight;

                // 有两种情况需要考虑是否绘制元素顶部和底部填充颜色
                // 1、当元素处于每一行最后一个元素
                // 2、当元素为布局最后一个元素

                boolean isLastColumn = col == mNumColumns - 1;
                boolean isLastChild = i == count - 1;

                if (!isLastColumn && !isLastChild) {
                    childIndex++;
                    continue;
                }

                int horizontalSpace = 0;
                if (mHorizontalStartEndSpaceEnabled) {
                    horizontalSpace = mHorizontalSpace;
                }

                // 最大宽度
                int right = mHorizontalMaxWidth;
                if (isLastColumn) {
                    right = cright;
                }

                // 为第一行子元素需判断是否绘制垂直顶部的间隔区域
                boolean headerSpaceEnabled;
                if (row == 0) {
                    headerSpaceEnabled = mVerticalStartEndSpaceEnabled;
                } else {
                    headerSpaceEnabled = true;
                }

                if (headerSpaceEnabled) {
                    canvas.drawRect(0, ctop - mVerticalSpace, right + horizontalSpace, ctop,
                            mVerticalPaint);
                }

                // 为最后子元素时需判断是否绘制垂直底部的间隔区域
                if (isLastChild) {
                    if (mVerticalStartEndSpaceEnabled) {
                        canvas.drawRect(0, cbottom, cright + horizontalSpace, cbottom +
                                mVerticalSpace, mVerticalPaint);
                    }
                }
                childIndex++;
            }
        }
    }

    /**
     * 当前视图距离左边间距距离（只算间距）
     *
     * @param col
     * @return
     */
    private int horizontalLeftSpacing(int col) {
        int leftSpacing = col * mHorizontalSpace;
        if (mHorizontalStartEndSpaceEnabled) {
            leftSpacing += mHorizontalSpace;
        }
        return leftSpacing;
    }

    /**
     * 当前视图距离顶部间距距离（只算间距）
     *
     * @param row
     * @return
     */
    private int verticalTopSpacing(int row) {
        int topSpacing = row * mVerticalSpace;
        if (mVerticalStartEndSpaceEnabled) {
            topSpacing += mVerticalSpace;
        }
        return topSpacing;
    }

    public void setNumColumns(int numColumns) {
        if (mNumColumns != numColumns) {
            mNumColumns = numColumns;
            requestLayoutIfNecessary();
        }
    }

    public void setHorizontalSpace(int horizontalSpace) {
        if (mHorizontalSpace != horizontalSpace) {
            mHorizontalSpace = horizontalSpace;
            requestLayoutIfNecessary();
        }
    }

    public void setVerticalSpace(int verticalSpace) {
        if (mVerticalSpace != verticalSpace) {
            mVerticalSpace = verticalSpace;
            requestLayoutIfNecessary();
        }
    }

    public void setHorizontalStartEndSpaceEnabled(boolean horizontalStartEndSpaceEnabled) {
        if (mHorizontalStartEndSpaceEnabled != horizontalStartEndSpaceEnabled) {
            mHorizontalStartEndSpaceEnabled = horizontalStartEndSpaceEnabled;
            requestLayoutIfNecessary();
        }
    }

    public void setVerticalStartEndSpaceEnabled(boolean verticalStartEndSpaceEnabled) {
        if (mVerticalStartEndSpaceEnabled != verticalStartEndSpaceEnabled) {
            mVerticalStartEndSpaceEnabled = verticalStartEndSpaceEnabled;
            requestLayoutIfNecessary();
        }
    }

    public void setVerticalSpaceColor(int verticalSpaceColor) {
        if (mVerticalSpaceColor != verticalSpaceColor) {
            mVerticalSpaceColor = verticalSpaceColor;
            requestLayoutIfNecessary();
        }
    }

    public void setHorizontalSpaceColor(int horizontalSpaceColor) {
        if (horizontalSpaceColor != horizontalSpaceColor) {
            mHorizontalSpaceColor = horizontalSpaceColor;
            requestLayoutIfNecessary();
        }
    }

    /**
     * 是否是必须重新布局
     */
    private void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            requestLayout();
            invalidate();
        }
    }

    public int getVerticalSpace() {
        return mVerticalSpace;
    }

    public int getHorizontalSpace() {
        return mHorizontalSpace;
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        this.mAdapter = adapter;
        mDataSetObserver = new AdapterDataSetObserver();
        mAdapter.registerDataSetObserver(mDataSetObserver);
        notifyDataChanged();
    }

    private void notifyDataChanged() {
        this.removeAllViews();

        final int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View childView = mAdapter.getView(i, null, this);
            final int position = i;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, position);
                    }
                }
            });
            addView(childView);
        }

        requestLayoutIfNecessary();
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataChanged();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAdapter != null && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LayoutParams(p);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * @Descirption: 宫格视图点击事件监听器
     * @Author: monlong
     * @Email: 826309156@qq.com
     * @Date: 2016-05-04 14:17
     * @Version: 1.0.0
     */
    public interface OnItemClickListener {

        /**
         * 点击事件回调
         *
         * @param v     被点击的元素
         * @param index 被点击元素的位置
         */
        void onItemClick(View v, int index);
    }
}
