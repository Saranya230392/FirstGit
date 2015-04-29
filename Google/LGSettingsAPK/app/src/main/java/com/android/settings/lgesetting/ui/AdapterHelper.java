package com.android.settings.lgesetting.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class AdapterHelper<ITEM> extends ArrayAdapter<ITEM>{
    protected abstract class BaseViewHolder {
    }

    protected abstract View newView(int aPosition, ViewGroup aParent);
    protected abstract BaseViewHolder newViewHolder(int aPosition, View aConvertView, ViewGroup aParent);
    protected abstract void bindView(int aPosition, View aConvertView, BaseViewHolder aViewHolder);

    private LayoutInflater mLayoutInflater;

    public AdapterHelper(Context aContext, int aResourceId){
        super(aContext, aResourceId);

        mLayoutInflater = (LayoutInflater)aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AdapterHelper(Context aContext, int aResourceId, ITEM[] aArrayList){
        super(aContext, aResourceId, aArrayList);
        mLayoutInflater = (LayoutInflater)aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public AdapterHelper(Context aContext, int aResourceId, ArrayList<ITEM> aArrayList){
        super(aContext, aResourceId, aArrayList);
        mLayoutInflater = (LayoutInflater)aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHolder viewHolder = null;

        if(convertView == null){
            convertView = newView(position, parent);
            viewHolder = newViewHolder(position, convertView, parent);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (BaseViewHolder)convertView.getTag();
        }

        bindView(position, convertView, viewHolder);

        return convertView;
    }

    protected LayoutInflater getLayoutInflater(){
        return mLayoutInflater;
    }
}
