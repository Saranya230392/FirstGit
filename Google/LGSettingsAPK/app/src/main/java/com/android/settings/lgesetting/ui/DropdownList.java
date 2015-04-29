package com.android.settings.lgesetting.ui;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;

public class DropdownList extends ListActivity implements DropdownListIntent, AdapterView.OnItemClickListener, View.OnClickListener {
    //private static final String TAG = DropdownList.class.getSimpleName();

    private static final int LISTITEM_VIEW = R.layout.ui_dropdown_list_item;

    private String[] mEntries;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Button mHeaderButton;
    private int mIndex;
    private int mSelector;

    private AnimationSet mAnimationSet = null;
    private Animation mAnimation = null;
    private LayoutAnimationController mAnimationController = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_dropdown_list);

        findViews();
        View contentView = findViewById(R.id.contentLayout);
        contentView.setOnClickListener(this);

        Intent intent = getIntent();
        boolean useAnimation = intent.getBooleanExtra(INTENT_EXTRA_ANIMATION, false);
        int requestedEntryIndex = intent.getIntExtra(DropdownListIntent.INTENT_EXTRA_INDEX, 0);

        if(useAnimation){
            setAnimation();
        }

        ArrayList<String> newEntry = new ArrayList<String>();
        int entryIndex = -1;
        mSelector = intent.getIntExtra(INTENT_EXTRA_SELECTOR, 0);
        int entryResourceId = intent.getIntExtra(DropdownListIntent.INTENT_EXTRA_ENTRY, 0);
        String[] entries = getResources().getStringArray(entryResourceId);
        for(int offset=0; offset < 32; offset++){
            if((mSelector & (0x00000001 << offset)) != 0){
                entryIndex++;
                if(requestedEntryIndex == offset){
                    mIndex = entryIndex;
                }
                newEntry.add(entries[offset]);
            }
        }

        mEntries = new String[entryIndex+1];
        newEntry.toArray(mEntries);

        mHeaderButton.setText(mEntries[mIndex]);
        mHeaderButton.setOnClickListener(this);

        mAdapter = new ListViewAdapter(this, LISTITEM_VIEW, mEntries);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        if(0 < mListView.getCount()){
            mListView.setVisibility(View.VISIBLE);
        }else{
            mListView.setVisibility(View.GONE);
        }
    }

    public void onItemClick(AdapterView<?> aParent, View aView, int aPosition, long aId) {
        if(aPosition != mIndex){
            // It makes fliking
            //mHeaderButton.setText(mEntries[aPosition]);

            int callerSideIndex = -1;
            int entryIndex = -1;
            for(int offset=0; offset < 32; offset++){
                if((mSelector & (0x00000001 << offset)) != 0){
                    entryIndex++;
                    if(entryIndex == aPosition){
                        callerSideIndex = offset;
                        break;
                    }
                }
            }

            Intent intent = new Intent();
            intent.putExtra(DropdownListIntent.INTENT_EXTRA_INDEX, callerSideIndex);

            setResult(RESULT_OK, intent);
        }

        finish();
    }

    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
        //return super.onTouchEvent(event);
    }

    public void onClick(View aView) {
        finish();
    }

    private void findViews(){
        mHeaderButton = (Button) findViewById(R.id.dropdownButton);
        mListView = getListView();

    }

    private void setAnimation() {
        mAnimationSet = new AnimationSet(true);
        mAnimation = new AlphaAnimation(0.0f, 1.0f);
        mAnimation.setDuration(50);
        mAnimationSet.addAnimation(mAnimation);
        mAnimation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        mAnimation.setDuration(200);
        mAnimationSet.addAnimation(mAnimation);
        mAnimationController = new LayoutAnimationController(mAnimationSet, 0.5f);

        mListView.setLayoutAnimation(mAnimationController);
    }

    private class ListViewAdapter extends AdapterHelper<String>{
        private class ViewHolder extends BaseViewHolder{
            TextView mTextView;
        }

        private LayoutInflater mLayoutInflater;

        public ListViewAdapter(Context aContext, int aResourceId, String[] aArrayList){
            super(aContext, aResourceId, aArrayList);
            mLayoutInflater = (LayoutInflater)aContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        protected View newView(int aPosition, ViewGroup aParent){
            return mLayoutInflater.inflate(R.layout.ui_dropdown_list_item, null);
        }

        protected BaseViewHolder newViewHolder(int aPosition, View aConvertView, ViewGroup aParent){
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) aConvertView.findViewById(R.id.textView);
            return viewHolder;
        }

        protected void bindView(int aPosition, View aConvertView, BaseViewHolder aViewHolder){
            ViewHolder viewHolder = (ViewHolder)aViewHolder;
            viewHolder.mTextView.setText((String)getItem(aPosition));
        }
    }
}
