package com.andlot.newlqlibrary.adapter;

import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public abstract class NewLqExpanableAdapter<K, V> extends BaseExpandableListAdapter {

	protected List<V> parentData;
	protected Map<K, List<V>> childData;
	
	public NewLqExpanableAdapter(List<V> parentData, Map<K, List<V>> childData){
		this.childData = childData;
		this.parentData = parentData;
	}
	
	@Override
	public int getGroupCount() {
		return parentData == null ? 0 : parentData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childData == null ? 0 : 
			childData.get(groupPosition) == null ? 0 : childData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return parentData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupViewHolder<V> holder = null;
		if (convertView == null) {
			holder = getGroupHolder();
			convertView = View.inflate(parent.getContext(), getGroupViewRes(), null);
			holder.initWidgets(convertView);
			convertView.setTag(holder);
		}else{
			holder = (GroupViewHolder<V>) convertView.getTag();
		}
		holder.setData(parentData, groupPosition);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder<K, V> holder;
		if (convertView == null) {
			holder = getChildHolder();
			convertView = View.inflate(parent.getContext(), getChildViewRes(), null);
			holder.initWidgets(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ChildViewHolder<K, V>) convertView.getTag();
		}
		holder.setData(childData, groupPosition, childPosition);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	protected abstract int getGroupViewRes();
	protected abstract int getChildViewRes();
	
	protected abstract GroupViewHolder<V> getGroupHolder();
	protected abstract ChildViewHolder<K, V> getChildHolder();
	
}
