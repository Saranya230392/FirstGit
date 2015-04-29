package com.android.settings.remote;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RemoteLayoutInflater extends LayoutInflater {
    private Resources mResources;

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit."
    };

    public RemoteLayoutInflater(Context context) {
        super(context);
    }

    public RemoteLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new RemoteLayoutInflater(this, newContext);
    }

    @Override
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        //Log.d("kimyow", "INFLATING from mResources: " + mResources);
        XmlResourceParser parser = mResources.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    /** Override onCreateView to instantiate names that correspond to the
    widgets known to the Widget factory. If we don't find a match,
    call through to our super class.
    */
    @Override
    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : sClassPrefixList) {
            try {
                View view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
            	Log.d("kimyow", "ClassNotFoundException");
            }
        }

        return super.onCreateView(name, attrs);
    }

    public void setResources(Resources resources) {
        mResources = resources;
    }
}