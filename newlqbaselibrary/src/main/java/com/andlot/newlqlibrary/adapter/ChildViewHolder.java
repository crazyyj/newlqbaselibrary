package com.andlot.newlqlibrary.adapter;

import java.util.List;
import java.util.Map;

import android.view.View;

/**
 *
 * @param <K> 固定Integer 类型 待指哪个条目
 * @param <V> 数据类型 Bean对象
 */
public abstract class ChildViewHolder<K, V> {
	
	public abstract void initWidgets(View childView);
	public abstract void setData(Map<K, List<V>> childData, int groupPosition, int childPosition);
}
