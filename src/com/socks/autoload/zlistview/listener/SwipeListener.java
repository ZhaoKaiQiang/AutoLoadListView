/*
 * Copyright (c) 2014, 青岛司通科技有限公司 All rights reserved.
 * File Name：SwipeListener.java
 * Version：V1.0
 * Author：zhaokaiqiang
 * Date：2015-1-6
 */

package com.socks.autoload.zlistview.listener;


import com.socks.autoload.zlistview.ZSwipeItem;

/**
 * 滑动监听器
 * 
 * @class: com.socks.zlistview.bean.SwipeListener
 * @author zhaokaiqiang
 * @date 2015-1-6 下午5:49:10
 * 
 */
public interface SwipeListener {

	public void onStartOpen(ZSwipeItem layout);

	public void onOpen(ZSwipeItem layout);

	public void onStartClose(ZSwipeItem layout);

	public void onClose(ZSwipeItem layout);

	public void onUpdate(ZSwipeItem layout, int leftOffset, int topOffset);

	public void onHandRelease(ZSwipeItem layout, float xvel, float yvel);

}
