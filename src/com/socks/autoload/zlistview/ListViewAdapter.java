/*
 * Copyright (c) 2014, 青岛司通科技有限公司 All rights reserved.
 * File Name：OtherAdapter.java
 * Version：V1.0
 * Author：zhaokaiqiang
 * Date：2015-1-4
 */

package com.socks.autoload.zlistview;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.socks.autoload.R;
import com.socks.autoload.zlistview.adapter.BaseSwipeAdapter;
import com.socks.autoload.zlistview.enums.DragEdge;
import com.socks.autoload.zlistview.enums.ShowMode;
import com.socks.autoload.zlistview.listener.SimpleSwipeListener;

public class ListViewAdapter extends BaseSwipeAdapter {

	protected static final String TAG = "ListViewAdapter";

	public int count = 20;

	private Activity context;

	public ListViewAdapter(Activity context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe_item;
	}

	@Override
	public View generateView(int position, ViewGroup parent) {
		return context.getLayoutInflater().inflate(R.layout.item_listview,
				parent, false);
	}

	@Override
	public void fillValues(final int position, View convertView) {
		final ZSwipeItem swipeItem = (ZSwipeItem) convertView
				.findViewById(R.id.swipe_item);
		LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.ll);

		TextView tv = (TextView) convertView.findViewById(R.id.tv);
		tv.setText(" I'm " + position);

		swipeItem.setShowMode(ShowMode.PullOut);
		swipeItem.setDragEdge(DragEdge.Right);

		swipeItem.addSwipeListener(new SimpleSwipeListener() {
			@Override
			public void onOpen(ZSwipeItem layout) {
				Log.d(TAG, "打开:" + position);
			}

			@Override
			public void onClose(ZSwipeItem layout) {
				Log.d(TAG, "关闭:" + position);
			}

			@Override
			public void onStartOpen(ZSwipeItem layout) {
				Log.d(TAG, "准备打开:" + position);
			}

			@Override
			public void onStartClose(ZSwipeItem layout) {
				Log.d(TAG, "准备关闭:" + position);
			}

			@Override
			public void onHandRelease(ZSwipeItem layout, float xvel, float yvel) {
				Log.d(TAG, "手势释放");
			}

			@Override
			public void onUpdate(ZSwipeItem layout, int leftOffset,
			                     int topOffset) {
				Log.d(TAG, "位置更新");
			}
		});

		ll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, "删除:" + position, Toast.LENGTH_SHORT)
						.show();
				swipeItem.close();
			}
		});
	}
}
