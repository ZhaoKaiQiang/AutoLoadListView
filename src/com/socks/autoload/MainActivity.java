package com.socks.autoload;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.socks.autoload.AutoLoadListView.OnLoadNextListener;
import com.socks.autoload.LoadingFooter.State;

public class MainActivity extends Activity implements
		SwipeRefreshLayout.OnRefreshListener {

	// 加载更多
	public static final int MSG_LOAD_MORE = 0;
	// 刷新
	public static final int MSG_REFRESH = 1;

	private SwipeRefreshLayout swipeLayout;
	private AutoLoadListView listView;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_LOAD_MORE:

				if (adapter.count < 60) {
					adapter.count += 20;
					adapter.notifyDataSetChanged();
					listView.setState(State.Idle);
				} else {
					listView.setState(State.TheEnd);
				}

				break;
			case MSG_REFRESH:
				swipeLayout.setRefreshing(false);
				adapter.count = 20;
				listView.smoothScrollToPosition(0);
				adapter.notifyDataSetChanged();
				listView.setState(State.Idle);
				break;
			default:
				break;
			}
		};
	};
	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		swipeLayout = (SwipeRefreshLayout) this
				.findViewById(R.id.swipe_refresh);
		// 顶部刷新的样式
		swipeLayout.setColorSchemeResources(android.R.color.holo_red_light,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);

		swipeLayout.setOnRefreshListener(this);

		listView = (AutoLoadListView) this.findViewById(R.id.listview);

		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnLoadNextListener(new OnLoadNextListener() {

			@Override
			public void onLoadNext() {
				handler.sendEmptyMessageDelayed(MSG_LOAD_MORE, 3000);
			}
		});

	}

	public void onRefresh() {
		handler.sendEmptyMessageDelayed(MSG_REFRESH, 3000);
	}

	private class MyAdapter extends BaseAdapter {

		public int count = 20;

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
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item,
						parent, false);
				viewHolder = new ViewHolder();
				viewHolder.tv = (TextView) convertView.findViewById(R.id.tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.tv.setText("I'm " + position);
			return convertView;
		}

	}

	private static class ViewHolder {
		TextView tv;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_reshresh) {

			swipeLayout.setRefreshing(true);
			handler.sendEmptyMessageDelayed(MSG_REFRESH, 3000);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
