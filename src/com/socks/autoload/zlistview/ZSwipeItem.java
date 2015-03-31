package com.socks.autoload.zlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.socks.autoload.R;
import com.socks.autoload.zlistview.enums.DragEdge;
import com.socks.autoload.zlistview.enums.ShowMode;
import com.socks.autoload.zlistview.listener.OnSwipeLayoutListener;
import com.socks.autoload.zlistview.listener.SwipeListener;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class ZSwipeItem extends FrameLayout {

	protected static final String TAG = "ZSwipeItem";

	private ViewDragHelper mDragHelper;
	private int mDragDistance = 0;
	private DragEdge mDragEdge;
	private ShowMode mShowMode;

	private float mHorizontalSwipeOffset;
	private float mVerticalSwipeOffset;

	private boolean mSwipeEnabled = true;

	private List<OnSwipeLayoutListener> mOnLayoutListeners;
	private List<SwipeListener> swipeListeners = new ArrayList<SwipeListener>();

	public ZSwipeItem(Context context) {
		this(context, null);
	}

	public ZSwipeItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZSwipeItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ZSwipeItem);
		// 默认是右边缘检测
		int ordinal = a.getInt(R.styleable.ZSwipeItem_drag_edge,
				DragEdge.Right.ordinal());
		mDragEdge = DragEdge.values()[ordinal];
		// 默认模式是拉出
		ordinal = a.getInt(R.styleable.ZSwipeItem_show_mode,
				ShowMode.PullOut.ordinal());
		mShowMode = ShowMode.values()[ordinal];

		mHorizontalSwipeOffset = a.getDimension(
				R.styleable.ZSwipeItem_horizontalSwipeOffset, 0);
		mVerticalSwipeOffset = a.getDimension(
				R.styleable.ZSwipeItem_verticalSwipeOffset, 0);

		a.recycle();
	}

	/**
	 * 进行拖拽的主要类
	 */
	private ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {

		/**
		 * 计算被横向拖动view的left
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == getSurfaceView()) {
				switch (mDragEdge) {
					case Top:
					case Bottom:
						return getPaddingLeft();
					case Left:
						if (left < getPaddingLeft())
							return getPaddingLeft();
						if (left > getPaddingLeft() + mDragDistance)
							return getPaddingLeft() + mDragDistance;
						break;
					case Right:
						if (left > getPaddingLeft())
							return getPaddingLeft();
						if (left < getPaddingLeft() - mDragDistance)
							return getPaddingLeft() - mDragDistance;
						break;
				}
			} else if (child == getBottomView()) {

				switch (mDragEdge) {
					case Top:
					case Bottom:
						return getPaddingLeft();
					case Left:
						if (mShowMode == ShowMode.PullOut) {
							if (left > getPaddingLeft())
								return getPaddingLeft();
						}
						break;
					case Right:
						if (mShowMode == ShowMode.PullOut) {
							if (left < getMeasuredWidth() - mDragDistance) {
								return getMeasuredWidth() - mDragDistance;
							}
						}
						break;
				}
			}
			return left;
		}

		/**
		 * 计算被纵向拖动的view的top
		 */
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			if (child == getSurfaceView()) {
				switch (mDragEdge) {
					case Left:
					case Right:
						return getPaddingTop();
					case Top:
						if (top < getPaddingTop())
							return getPaddingTop();
						if (top > getPaddingTop() + mDragDistance)
							return getPaddingTop() + mDragDistance;
						break;
					case Bottom:
						if (top < getPaddingTop() - mDragDistance) {
							return getPaddingTop() - mDragDistance;
						}
						if (top > getPaddingTop()) {
							return getPaddingTop();
						}
				}
			} else {
				switch (mDragEdge) {
					case Left:
					case Right:
						return getPaddingTop();
					case Top:
						if (mShowMode == ShowMode.PullOut) {
							if (top > getPaddingTop())
								return getPaddingTop();
						} else {
							if (getSurfaceView().getTop() + dy < getPaddingTop())
								return getPaddingTop();
							if (getSurfaceView().getTop() + dy > getPaddingTop()
									+ mDragDistance)
								return getPaddingTop() + mDragDistance;
						}
						break;
					case Bottom:
						if (mShowMode == ShowMode.PullOut) {
							if (top < getMeasuredHeight() - mDragDistance)
								return getMeasuredHeight() - mDragDistance;
						} else {
							if (getSurfaceView().getTop() + dy >= getPaddingTop())
								return getPaddingTop();
							if (getSurfaceView().getTop() + dy <= getPaddingTop()
									- mDragDistance)
								return getPaddingTop() - mDragDistance;
						}
				}
			}
			return top;
		}

		/**
		 * 确定要进行拖动的view
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == getSurfaceView() || child == getBottomView();
		}

		/**
		 * 确定横向拖动边界
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			return mDragDistance;
		}

		/**
		 * 确定纵向拖动边界
		 */
		@Override
		public int getViewVerticalDragRange(View child) {
			return mDragDistance;
		}

		/**
		 * 当子控件被释放的时候调用，可以获取加速度的数据，来判断用户意图
		 */
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);

			for (SwipeListener l : swipeListeners) {
				l.onHandRelease(ZSwipeItem.this, xvel, yvel);
			}

			if (releasedChild == getSurfaceView()) {
				processSurfaceRelease(xvel, yvel);
			} else if (releasedChild == getBottomView()) {
				if (getShowMode() == ShowMode.PullOut) {
					processBottomPullOutRelease(xvel, yvel);
				} else if (getShowMode() == ShowMode.LayDown) {
					processBottomLayDownMode(xvel, yvel);
				}
			}

			invalidate();
		}

		/**
		 * 当view的位置发生变化的时候调用，可以设置view的位置跟随手指移动
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
		                                  int dx, int dy) {

			int evLeft = getSurfaceView().getLeft();
			int evTop = getSurfaceView().getTop();

			if (changedView == getSurfaceView()) {

				if (mShowMode == ShowMode.PullOut) {
					if (mDragEdge == DragEdge.Left
							|| mDragEdge == DragEdge.Right) {
						getBottomView().offsetLeftAndRight(dx);
					} else {
						getBottomView().offsetTopAndBottom(dy);
					}
				}

			} else if (changedView == getBottomView()) {

				if (mShowMode == ShowMode.PullOut) {
					getSurfaceView().offsetLeftAndRight(dx);
					getSurfaceView().offsetTopAndBottom(dy);
				} else {
					Rect rect = computeBottomLayDown(mDragEdge);
					getBottomView().layout(rect.left, rect.top, rect.right,
							rect.bottom);

					int newLeft = getSurfaceView().getLeft() + dx;
					int newTop = getSurfaceView().getTop() + dy;

					if (mDragEdge == DragEdge.Left
							&& newLeft < getPaddingLeft())
						newLeft = getPaddingLeft();
					else if (mDragEdge == DragEdge.Right
							&& newLeft > getPaddingLeft())
						newLeft = getPaddingLeft();
					else if (mDragEdge == DragEdge.Top
							&& newTop < getPaddingTop())
						newTop = getPaddingTop();
					else if (mDragEdge == DragEdge.Bottom
							&& newTop > getPaddingTop())
						newTop = getPaddingTop();

					getSurfaceView().layout(newLeft, newTop,
							newLeft + getMeasuredWidth(),
							newTop + getMeasuredHeight());
				}
			}

			// 及时派发滑动事件
			dispatchSwipeEvent(evLeft, evTop, dx, dy);

			invalidate();
		}
	};

	/**
	 * swipe事件分发器
	 *
	 * @param surfaceLeft
	 * @param surfaceTop
	 * @param dx
	 * @param dy
	 */
	protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, int dx,
	                                  int dy) {
		DragEdge edge = getDragEdge();
		boolean open = true;
		if (edge == DragEdge.Left) {
			if (dx < 0)
				open = false;
		} else if (edge == DragEdge.Right) {
			if (dx > 0)
				open = false;
		} else if (edge == DragEdge.Top) {
			if (dy < 0)
				open = false;
		} else if (edge == DragEdge.Bottom) {
			if (dy > 0)
				open = false;
		}

		dispatchSwipeEvent(surfaceLeft, surfaceTop, open);
	}

	private int mEventCounter = 0;

	/**
	 * swipe事件分发器
	 *
	 * @param surfaceLeft
	 * @param surfaceTop
	 * @param open
	 */
	protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop,
	                                  boolean open) {

		safeBottomView();
		Status status = getOpenStatus();

		if (!swipeListeners.isEmpty()) {

			Log.d(TAG, "swipeListeners=" + swipeListeners.size());

			mEventCounter++;

			if (mEventCounter == 1) {
				if (open) {
					swipeListeners.get(0).onStartOpen(ZSwipeItem.this);
					swipeListeners.get(swipeListeners.size() - 1).onStartOpen(
							ZSwipeItem.this);
				} else {
					swipeListeners.get(0).onStartClose(ZSwipeItem.this);
					swipeListeners.get(swipeListeners.size() - 1).onStartClose(
							ZSwipeItem.this);
				}
			}

			for (SwipeListener l : swipeListeners) {
				l.onUpdate(ZSwipeItem.this, surfaceLeft - getPaddingLeft(),
						surfaceTop - getPaddingTop());
			}

			if (status == Status.Close) {
				swipeListeners.get(0).onClose(ZSwipeItem.this);
				swipeListeners.get(swipeListeners.size() - 1).onClose(
						ZSwipeItem.this);
				mEventCounter = 0;
			} else if (status == Status.Open) {
				getBottomView().setEnabled(true);
				swipeListeners.get(0).onOpen(ZSwipeItem.this);
				swipeListeners.get(swipeListeners.size() - 1).onOpen(
						ZSwipeItem.this);
				mEventCounter = 0;
			}
		}
	}

	/**
	 * 防止底布局获取到任何的触摸事件，特别是在LayDown模式
	 */
	private void safeBottomView() {
		Status status = getOpenStatus();
		ViewGroup bottom = getBottomView();

		if (status == Status.Close) {
			if (bottom.getVisibility() != INVISIBLE)
				bottom.setVisibility(INVISIBLE);
		} else {
			if (bottom.getVisibility() != VISIBLE)
				bottom.setVisibility(VISIBLE);
		}
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		// 让滚动一直进行下去
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/**
	 * 强制布局中必须嵌套两个ViewGroup布局,在新item出现的时候就会调用
	 */
	@SuppressLint("WrongCall")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		if (childCount != 2) {
			throw new IllegalStateException("You need 2  views in SwipeLayout");
		}
		if (!(getChildAt(0) instanceof ViewGroup)
				|| !(getChildAt(1) instanceof ViewGroup)) {
			throw new IllegalArgumentException(
					"The 2 children in SwipeLayout must be an instance of ViewGroup");
		}

		if (mShowMode == ShowMode.PullOut) {
			layoutPullOut();
		} else if (mShowMode == ShowMode.LayDown) {
			layoutLayDown();
		}

		safeBottomView();

		if (mOnLayoutListeners != null)
			for (int i = 0; i < mOnLayoutListeners.size(); i++) {
				mOnLayoutListeners.get(i).onLayout(this);
			}

	}

	private void layoutPullOut() {
		Rect rect = computeSurfaceLayoutArea(false);
		getSurfaceView().layout(rect.left, rect.top, rect.right, rect.bottom);
		rect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
		getBottomView().layout(rect.left, rect.top, rect.right, rect.bottom);
		bringChildToFront(getSurfaceView());
	}

	private void layoutLayDown() {
		Rect rect = computeSurfaceLayoutArea(false);
		getSurfaceView().layout(rect.left, rect.top, rect.right, rect.bottom);
		rect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, rect);
		getBottomView().layout(rect.left, rect.top, rect.right, rect.bottom);
		bringChildToFront(getSurfaceView());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 初始化移动距离
		if (mDragEdge == DragEdge.Left || mDragEdge == DragEdge.Right)
			mDragDistance = getBottomView().getMeasuredWidth()
					- dp2px(mHorizontalSwipeOffset);
		else {
			mDragDistance = getBottomView().getMeasuredHeight()
					- dp2px(mVerticalSwipeOffset);
		}
	}

	private boolean mTouchConsumedByChild = false;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (!isEnabled() || !isEnabledInAdapterView()) {
			return true;
		}

		if (!isSwipeEnabled()) {
			return false;
		}

		int action = ev.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				Status status = getOpenStatus();
				if (status == Status.Close) {
					mTouchConsumedByChild = childNeedHandleTouchEvent(
							getSurfaceView(), ev) != null;
				} else if (status == Status.Open) {
					mTouchConsumedByChild = childNeedHandleTouchEvent(
							getBottomView(), ev) != null;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mTouchConsumedByChild = false;
		}

		if (mTouchConsumedByChild)
			return false;
		return mDragHelper.shouldInterceptTouchEvent(ev);
	}

	/**
	 * ViewGroup的子View是否需要处理这个事件
	 *
	 * @param v
	 * @param event
	 * @return
	 */
	private View childNeedHandleTouchEvent(ViewGroup v, MotionEvent event) {
		if (v == null)
			return null;
		if (v.onTouchEvent(event))
			return v;

		int childCount = v.getChildCount();
		for (int i = childCount - 1; i >= 0; i--) {
			View child = v.getChildAt(i);
			if (child instanceof ViewGroup) {
				View grandChild = childNeedHandleTouchEvent((ViewGroup) child,
						event);
				if (grandChild != null)
					return grandChild;
			} else {
				if (childNeedHandleTouchEvent(v.getChildAt(i), event))
					return v.getChildAt(i);
			}
		}
		return null;
	}

	/**
	 * 判断View是否要去处理触摸事件
	 *
	 * @param v
	 * @param event
	 * @return
	 */
	private boolean childNeedHandleTouchEvent(View v, MotionEvent event) {
		if (v == null)
			return false;

		int[] loc = new int[2];
		v.getLocationOnScreen(loc);
		int left = loc[0];
		int top = loc[1];

		if (event.getRawX() > left && event.getRawX() < left + v.getWidth()
				&& event.getRawY() > top
				&& event.getRawY() < top + v.getHeight()) {
			return v.onTouchEvent(event);
		}

		return false;
	}

	private float sX = -1, sY = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabledInAdapterView() || !isEnabled())
			return true;

		if (!isSwipeEnabled())
			return super.onTouchEvent(event);

		int action = event.getActionMasked();
		ViewParent parent = getParent();

		gestureDetector.onTouchEvent(event);

		Status status = getOpenStatus();
		ViewGroup touching = null;
		if (status == Status.Close) {
			touching = getSurfaceView();
		} else if (status == Status.Open) {
			touching = getBottomView();
		}

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mDragHelper.processTouchEvent(event);
				parent.requestDisallowInterceptTouchEvent(true);

				sX = event.getRawX();
				sY = event.getRawY();

				if (touching != null)
					touching.setPressed(true);

				return true;
			case MotionEvent.ACTION_MOVE: {
				if (sX == -1 || sY == -1) {
					// Trick:
					// When in nested mode, we need to send a constructed
					// ACTION_DOWN MotionEvent to mDragHelper, to help
					// it initialize itself.
					event.setAction(MotionEvent.ACTION_DOWN);
					mDragHelper.processTouchEvent(event);
					parent.requestDisallowInterceptTouchEvent(true);
					sX = event.getRawX();
					sY = event.getRawY();
					return true;
				}

				float distanceX = event.getRawX() - sX;
				float distanceY = event.getRawY() - sY;
				float angle = Math.abs(distanceY / distanceX);
				angle = (float) Math.toDegrees(Math.atan(angle));

				boolean doNothing = false;
				// 根据触摸角度，判断是否执行用户操作
				if (mDragEdge == DragEdge.Right) {
					boolean suitable = (status == Status.Open && distanceX > 0)
							|| (status == Status.Close && distanceX < 0);
					suitable = suitable || (status == Status.Middle);

					if (angle > 30 || !suitable) {
						doNothing = true;
					}
				}

				if (mDragEdge == DragEdge.Left) {
					boolean suitable = (status == Status.Open && distanceX < 0)
							|| (status == Status.Close && distanceX > 0);
					suitable = suitable || status == Status.Middle;

					if (angle > 30 || !suitable) {
						doNothing = true;
					}
				}

				if (mDragEdge == DragEdge.Top) {
					boolean suitable = (status == Status.Open && distanceY < 0)
							|| (status == Status.Close && distanceY > 0);
					suitable = suitable || status == Status.Middle;

					if (angle < 60 || !suitable) {
						doNothing = true;
					}
				}

				if (mDragEdge == DragEdge.Bottom) {
					boolean suitable = (status == Status.Open && distanceY > 0)
							|| (status == Status.Close && distanceY < 0);
					suitable = suitable || status == Status.Middle;

					if (angle < 60 || !suitable) {
						doNothing = true;
					}
				}

				if (doNothing) {
					// 拦截触摸事件
					parent.requestDisallowInterceptTouchEvent(false);
					return false;
				} else {
					if (touching != null) {
						touching.setPressed(false);
					}
					parent.requestDisallowInterceptTouchEvent(true);
					mDragHelper.processTouchEvent(event);
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				sX = -1;
				sY = -1;
				if (touching != null) {
					touching.setPressed(false);
				}
			}
			default:
				parent.requestDisallowInterceptTouchEvent(true);
				mDragHelper.processTouchEvent(event);
		}

		return true;
	}

	/**
	 * if working in {@link android.widget.AdapterView}, we should response
	 * {@link android.widget.Adapter} isEnable(int position).
	 *
	 * @return true when item is enabled, else disabled.
	 */
	private boolean isEnabledInAdapterView() {
		@SuppressWarnings("rawtypes")
		AdapterView adapterView = getAdapterView();
		boolean enable = true;
		if (adapterView != null) {
			Adapter adapter = adapterView.getAdapter();
			if (adapter != null) {
				int p = adapterView.getPositionForView(ZSwipeItem.this);
				if (adapter instanceof BaseAdapter) {
					enable = ((BaseAdapter) adapter).isEnabled(p);
				} else if (adapter instanceof ListAdapter) {
					enable = ((ListAdapter) adapter).isEnabled(p);
				}
			}
		}
		return enable;
	}

	public void setSwipeEnabled(boolean enabled) {
		mSwipeEnabled = enabled;
	}

	public boolean isSwipeEnabled() {
		return mSwipeEnabled;
	}

	@SuppressWarnings("rawtypes")
	private AdapterView getAdapterView() {
		ViewParent t = getParent();
		while (t != null) {
			if (t instanceof AdapterView) {
				return (AdapterView) t;
			}
			t = t.getParent();
		}
		return null;
	}

	private void performAdapterViewItemClick(MotionEvent e) {
		ViewParent t = getParent();

		Log.d(TAG, "performAdapterViewItemClick()");

		while (t != null) {
			if (t instanceof AdapterView) {
				@SuppressWarnings("rawtypes")
				AdapterView view = (AdapterView) t;
				int p = view.getPositionForView(ZSwipeItem.this);
				if (p != AdapterView.INVALID_POSITION
						&& view.performItemClick(
						view.getChildAt(p
								- view.getFirstVisiblePosition()), p,
						view.getAdapter().getItemId(p)))
					return;
			} else {
				if (t instanceof View && ((View) t).performClick())
					return;
			}

			t = t.getParent();
		}
	}

	private GestureDetector gestureDetector = new GestureDetector(getContext(),
			new SwipeDetector());

	/**
	 * 手势监听器，通过调用performItemClick、performItemLongClick，来解决item的点击问题，
	 *
	 * @author zhaokaiqiang
	 * @class: com.socks.zlistview.SwipeDetector
	 * @date 2015-1-7 下午3:44:09
	 */
	private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// 当用户单击之后，手指抬起的时候调用，如果没有双击监听器，就直接调用
			performAdapterViewItemClick(e);
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// 这个方法只有在确认用户不会发生双击事件的时候调用
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// 长按事件
			performLongClick();
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return false;
		}
	}

	public void setDragEdge(DragEdge dragEdge) {
		mDragEdge = dragEdge;
		requestLayout();
	}

	public void setShowMode(ShowMode mode) {
		mShowMode = mode;
		requestLayout();
	}

	public DragEdge getDragEdge() {
		return mDragEdge;
	}

	public int getDragDistance() {
		return mDragDistance;
	}

	public ShowMode getShowMode() {
		return mShowMode;
	}

	public ViewGroup getSurfaceView() {
		return (ViewGroup) getChildAt(1);
	}

	public ViewGroup getBottomView() {
		return (ViewGroup) getChildAt(0);
	}

	public enum Status {
		Middle, Open, Close
	}

	/**
	 * 获取当前的开启状态
	 *
	 * @return
	 */
	public Status getOpenStatus() {
		int surfaceLeft = getSurfaceView().getLeft();
		int surfaceTop = getSurfaceView().getTop();
		if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop())
			return Status.Close;

		if (surfaceLeft == (getPaddingLeft() - mDragDistance)
				|| surfaceLeft == (getPaddingLeft() + mDragDistance)
				|| surfaceTop == (getPaddingTop() - mDragDistance)
				|| surfaceTop == (getPaddingTop() + mDragDistance))
			return Status.Open;

		return Status.Middle;
	}

	/**
	 * 执行前布局的释放过程
	 *
	 * @param xvel
	 * @param yvel
	 */
	private void processSurfaceRelease(float xvel, float yvel) {
		if (xvel == 0 && getOpenStatus() == Status.Middle)
			close();

		if (mDragEdge == DragEdge.Left || mDragEdge == DragEdge.Right) {
			if (xvel > 0) {
				if (mDragEdge == DragEdge.Left)
					open();
				else
					close();
			}
			if (xvel < 0) {
				if (mDragEdge == DragEdge.Left)
					close();
				else
					open();
			}
		} else {
			if (yvel > 0) {
				if (mDragEdge == DragEdge.Top)
					open();
				else
					close();
			}
			if (yvel < 0) {
				if (mDragEdge == DragEdge.Top)
					close();
				else
					open();
			}
		}
	}

	/**
	 * 执行PullOut模式下，底布局的释放过程
	 *
	 * @param xvel
	 * @param yvel
	 */
	private void processBottomPullOutRelease(float xvel, float yvel) {

		if (xvel == 0 && getOpenStatus() == Status.Middle)
			close();

		if (mDragEdge == DragEdge.Left || mDragEdge == DragEdge.Right) {
			if (xvel > 0) {
				if (mDragEdge == DragEdge.Left)
					open();
				else
					close();
			}
			if (xvel < 0) {
				if (mDragEdge == DragEdge.Left)
					close();
				else
					open();
			}
		} else {
			if (yvel > 0) {
				if (mDragEdge == DragEdge.Top)
					open();
				else
					close();
			}

			if (yvel < 0) {
				if (mDragEdge == DragEdge.Top)
					close();
				else
					open();
			}
		}
	}

	/**
	 * 执行LayDown模式下，底布局的释放过程
	 *
	 * @param xvel
	 * @param yvel
	 */
	private void processBottomLayDownMode(float xvel, float yvel) {

		if (xvel == 0 && getOpenStatus() == Status.Middle)
			close();

		int l = getPaddingLeft(), t = getPaddingTop();

		if (xvel < 0 && mDragEdge == DragEdge.Right)
			l -= mDragDistance;
		if (xvel > 0 && mDragEdge == DragEdge.Left)
			l += mDragDistance;

		if (yvel > 0 && mDragEdge == DragEdge.Top)
			t += mDragDistance;
		if (yvel < 0 && mDragEdge == DragEdge.Bottom)
			t -= mDragDistance;

		mDragHelper.smoothSlideViewTo(getSurfaceView(), l, t);
		invalidate();
	}

	public void open() {
		open(true, true);
	}

	public void open(boolean smooth) {
		open(smooth, true);
	}

	public void open(boolean smooth, boolean notify) {
		ViewGroup surface = getSurfaceView(), bottom = getBottomView();
		int dx, dy;
		Rect rect = computeSurfaceLayoutArea(true);
		if (smooth) {
			mDragHelper
					.smoothSlideViewTo(getSurfaceView(), rect.left, rect.top);
		} else {
			dx = rect.left - surface.getLeft();
			dy = rect.top - surface.getTop();
			surface.layout(rect.left, rect.top, rect.right, rect.bottom);
			if (getShowMode() == ShowMode.PullOut) {
				Rect bRect = computeBottomLayoutAreaViaSurface(
						ShowMode.PullOut, rect);
				bottom.layout(bRect.left, bRect.top, bRect.right, bRect.bottom);
			}
			if (notify) {
				dispatchSwipeEvent(rect.left, rect.top, dx, dy);
			} else {
				safeBottomView();
			}
		}
		invalidate();
	}

	public void close() {
		close(true, true);
	}

	public void close(boolean smooth) {
		close(smooth, true);
	}

	public void close(boolean smooth, boolean notify) {
		ViewGroup surface = getSurfaceView();
		int dx, dy;
		if (smooth)
			mDragHelper.smoothSlideViewTo(getSurfaceView(), getPaddingLeft(),
					getPaddingTop());
		else {
			Rect rect = computeSurfaceLayoutArea(false);
			dx = rect.left - surface.getLeft();
			dy = rect.top - surface.getTop();
			surface.layout(rect.left, rect.top, rect.right, rect.bottom);
			if (notify) {
				dispatchSwipeEvent(rect.left, rect.top, dx, dy);
			} else {
				safeBottomView();
			}
		}
		invalidate();
	}

	public void toggle() {
		toggle(true);
	}

	public void toggle(boolean smooth) {
		if (getOpenStatus() == Status.Open)
			close(smooth);
		else if (getOpenStatus() == Status.Close)
			open(smooth);
	}

	private Rect computeSurfaceLayoutArea(boolean open) {
		int l = getPaddingLeft(), t = getPaddingTop();
		if (open) {
			if (mDragEdge == DragEdge.Left)
				l = getPaddingLeft() + mDragDistance;
			else if (mDragEdge == DragEdge.Right)
				l = getPaddingLeft() - mDragDistance;
			else if (mDragEdge == DragEdge.Top)
				t = getPaddingTop() + mDragDistance;
			else
				t = getPaddingTop() - mDragDistance;
		}
		return new Rect(l, t, l + getMeasuredWidth(), t + getMeasuredHeight());
	}

	private Rect computeBottomLayoutAreaViaSurface(ShowMode mode,
	                                               Rect surfaceArea) {
		Rect rect = surfaceArea;

		int bl = rect.left, bt = rect.top, br = rect.right, bb = rect.bottom;
		if (mode == ShowMode.PullOut) {
			if (mDragEdge == DragEdge.Left)
				bl = rect.left - mDragDistance;
			else if (mDragEdge == DragEdge.Right)
				bl = rect.right;
			else if (mDragEdge == DragEdge.Top)
				bt = rect.top - mDragDistance;
			else
				bt = rect.bottom;

			if (mDragEdge == DragEdge.Left || mDragEdge == DragEdge.Right) {
				bb = rect.bottom;
				br = bl + getBottomView().getMeasuredWidth();
			} else {
				bb = bt + getBottomView().getMeasuredHeight();
				br = rect.right;
			}
		} else if (mode == ShowMode.LayDown) {
			if (mDragEdge == DragEdge.Left)
				br = bl + mDragDistance;
			else if (mDragEdge == DragEdge.Right)
				bl = br - mDragDistance;
			else if (mDragEdge == DragEdge.Top)
				bb = bt + mDragDistance;
			else
				bt = bb - mDragDistance;

		}
		return new Rect(bl, bt, br, bb);

	}

	private Rect computeBottomLayDown(DragEdge dragEdge) {
		int bl = getPaddingLeft(), bt = getPaddingTop();
		int br, bb;
		if (dragEdge == DragEdge.Right) {
			bl = getMeasuredWidth() - mDragDistance;
		} else if (dragEdge == DragEdge.Bottom) {
			bt = getMeasuredHeight() - mDragDistance;
		}
		if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
			br = bl + mDragDistance;
			bb = bt + getMeasuredHeight();
		} else {
			br = bl + getMeasuredWidth();
			bb = bt + mDragDistance;
		}
		return new Rect(bl, bt, br, bb);
	}

	public void addSwipeListener(SwipeListener l) {

		if (swipeListeners.size() == 2) {
			swipeListeners.remove(1);
		}

		swipeListeners.add(l);
	}

	public void removeSwipeListener(SwipeListener l) {
		swipeListeners.remove(l);
	}

	public void addOnLayoutListener(OnSwipeLayoutListener l) {
		if (mOnLayoutListeners == null)
			mOnLayoutListeners = new ArrayList<OnSwipeLayoutListener>();
		mOnLayoutListeners.add(l);
	}

	public void removeOnLayoutListener(OnSwipeLayoutListener l) {
		if (mOnLayoutListeners != null)
			mOnLayoutListeners.remove(l);
	}

	private int dp2px(float dp) {
		return (int) (dp
				* getContext().getResources().getDisplayMetrics().density + 0.5f);
	}
}
