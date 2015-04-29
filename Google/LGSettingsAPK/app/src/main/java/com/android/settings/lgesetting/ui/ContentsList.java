package com.android.settings.lgesetting.ui;

import android.app.ListActivity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;

public abstract class ContentsList extends ListActivity
                                    implements View.OnClickListener
                                                ,OptionsMenuHelper.ActionHandler {
//    private static final String TAG = ContentsList.class.getSimpleName();

    protected static final int OPTIONS_MENU_NONE = 0x00000000;
    protected static final int OPTIONS_MENU_VISIBLE_REMOVE = 0x00000001;
    protected static final int OPTIONS_MENU_VISIBLE_REMOVE_ALL = 0x00000002;
    protected static final int OPTIONS_MENU_VISIBLE_MARK_ALL = 0x00000004;
    protected static final int OPTIONS_MENU_VISIBLE_UNMARK_ALL = 0x00000008;

    protected static final int OPTIONS_MENU_ENABLE_REMOVE = 0x00010000;
    protected static final int OPTIONS_MENU_ENABLE_REMOVE_ALL = 0x00020000;
    protected static final int OPTIONS_MENU_ENABLE_MARK_ALL = 0x00040000;
    protected static final int OPTIONS_MENU_ENABLE_UNMARK_ALL = 0x00080000;

//    private static final int OPTIONS_MENU_ID_REMOVE = R.id.remove;
//    private static final int OPTIONS_MENU_ID_REMOVE_ALL = R.id.removeAll;
//    private static final int OPTIONS_MENU_ID_MARK_ALL = R.id.markAll;
//    private static final int OPTIONS_MENU_ID_UNMARK_ALL = R.id.unmarkAll;

    private View mButtonPanel;
    private Button mAddButton;
    private Button mRemoveButton;
//    private ListAdapter mAdapter;
    private ListView mListView;
    private TextView mNoListMessage;
    private boolean mShowNoListMessage = true;

    ////////////////////////////////////////////////////////////////////////////
    //    override
    public void onOptionsMenuRemoveConfirmed(){
    }
    public void onOptionsMenuRemoveAllConfirmed(){
    }
    public void onOptionsMenuMarkAllItemSelected(){
    }
    public void onOPtionsMenuUnmarkAllItemSelected(){
    }
    protected void onBeforeSetContentView(Bundle savedInstanceState, int aContentViewResourceID){
    }
    protected void onAddButtonClick(View aView){
    }
    protected void onRemoveButtonClick(View aView){
        //showRemoveConfirmDialog(mRemoveConfirmClickListener);
    }
//    protected void onRemoveConfirmDialogConfirmed(){
//    }
    protected int getPrepareOptionsMenuSelector(Menu aMenu){
        return OptionsMenuHelper.NO_MENU;
    }
    protected int getContentViewResourceId(){
        return R.layout.ui_contents_list;
    }
    protected void onDataSetChanged(){
    }
    //    override
    ////////////////////////////////////////////////////////////////////////////

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d(TAG, "onCreate(): BEGIN");
        int contentView = getContentViewResourceId();
        onBeforeSetContentView(savedInstanceState, contentView);
        setContentView(contentView);

        findViews();

        mAddButton.setOnClickListener(this);
        mRemoveButton.setOnClickListener(this);

//        Log.d(TAG, "onCreate(): END");
    }

    public boolean onCreateOptionsMenu(Menu aMenu) {
        boolean result = OptionsMenuHelper.onCreate(getMenuInflater(), aMenu);
        if(result == true)
            return true;

        return super.onCreateOptionsMenu(aMenu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        int selector = getPrepareOptionsMenuSelector(menu);

        return OptionsMenuHelper.onPrepare(selector, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = OptionsMenuHelper.onItemSelected(this, item, this);
        if(result == true)
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View aView) {
        if(aView == mAddButton){
            onAddButtonClick(aView);
        }else if(aView == mRemoveButton){
            onRemoveButtonClick(aView);
        }
    }

    protected void setAdapter(ListAdapter aAdapter){
        mListView.setAdapter(aAdapter);

        aAdapter.registerDataSetObserver(new DataSetObserver(){
                public void onChanged() {
                    super.onChanged();
                    doDataSetChanged(); //false);
                }
            }
        );

        doDataSetChanged(); //true);
    }

    private void doDataSetChanged(){ //boolean aFirst){
        int count = mListView.getCount();
        if(0 < count){
            mNoListMessage.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }else{
            mListView.setVisibility(View.GONE);
            if(mShowNoListMessage == true){
                mNoListMessage.setVisibility(View.VISIBLE);
            }
        }

        onDataSetChanged();
    }

    public void showButtonPanel(int aVisibility){
        mButtonPanel.setVisibility(aVisibility);
    }

    protected void enableShowNoListMessage(boolean aEnabled){
        mShowNoListMessage = aEnabled;
    }

    protected View getButtonPanel(){
        return mButtonPanel;
    }

    public Button getAddButton(){
        return mAddButton;
    }

    public Button getRemoveButton(){
        return mRemoveButton;
    }

    protected ListAdapter getAdapter(){
        return mListView.getAdapter();
    }

    private void findViews(){
        mListView = getListView();

        LinearLayout contentsLayout = (LinearLayout)findViewById(R.id.contentsLayout);
        mNoListMessage = (TextView)contentsLayout.findViewById(R.id.noList);

        // bottom
        mButtonPanel = (LinearLayout)findViewById(R.id.buttonPanel);
        mAddButton = (Button)mButtonPanel.findViewById(R.id.addButton);
        mRemoveButton = (Button)mButtonPanel.findViewById(R.id.removeButton);
    }
}
