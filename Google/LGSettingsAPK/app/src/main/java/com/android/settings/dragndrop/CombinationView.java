package com.android.settings.dragndrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemProperties;
import com.android.settings.R;

public class CombinationView extends ViewGroup implements OnTouchListener/*, OnLongClickListener*/ {

    private static final String TAG = "CombinationView";
    private static int ANIMATION_DURATION = 250;
    private static int GRID_VIEW_PADDING = 50;

    private static int MAX_ROW_COUNT = 2;

    private CombinationViewAdapter mAdapter;
    private OnClickListener mOnClickListener = null;

    private SparseIntArray newPositions = new SparseIntArray();

    private int mDraggingItemIndex = -1;
    private String mDraggingItemKey = "";
    private int mFirstTouchX;
    private int mFirstTouchY;

    
    private int[] mColumnWidthSize = new int[] { 0, 0 };
    private int[] mRowHeightSize = new int[] { 0, 0 };

    private int lastTarget = -1;
    private boolean mSetLongClickFlag = false;
    private TextView mViewlabel;
    
    private View mView;
    public Runnable mPendingCheckForLongPress;
    public boolean mHasPerformedLongPress;
    public static final int MAX_NUM = 5;
    public ButtonItemManager mButtonItemManager;

    
    public CombinationView(Context context) {
    	this(context, null);
    }
    
    public CombinationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CombinationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mButtonItemManager = new ButtonItemManager(context);
        init();
    }


    private void init() {
        setOnTouchListener(this);
    }

    public void setAdapter(CombinationViewAdapter adapter) {
        mAdapter = adapter;
        addChildViews();
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    private void addChildViews() {
        for (int index = 0; index < mButtonItemManager.getItemCount(); index++) {
            View view = getView(index);
            if (view != null) {
                addView(view);
            }
        }
    }
    
    public View getView(int index) {

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        ImageView icon = new ImageView(getContext());

        ButtonItem item = mButtonItemManager.getItem(index);

        if (item == null) {
            return null;
        }

        icon.setImageResource(item.getDrawableId());
        //		icon.setPaddingRelative(15, 15, 15, 15);
        icon.setTag(item.getName());

        layout.addView(icon);

        TextView label = new TextView(getContext());
        label.setTag("text");
        label.setHorizontalFadingEdgeEnabled(true);
        label.setEllipsize(TruncateAt.MARQUEE);
        label.setSingleLine();
        label.setTextColor(getResources().getColor(R.color.normal_text_color));
        label.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        if (item.getDrawableId() == R.drawable.setting_button_combination_icon_qmemo) {
            item.setName(mAdapter.checkQmemoName());
            label.setText(mAdapter.checkQmemoName());
        } else {
            label.setText(item.getName());
        }
        layout.setContentDescription(item.getName());

        int mScreenSize = Integer.parseInt(SystemProperties.get("ro.sf.lcd_density"));
        if (mScreenSize < DisplayMetrics.DENSITY_XHIGH) {
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        } else {
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }

        int size = icon.getDrawable().getIntrinsicWidth();
        if (mButtonItemManager.getDownTrayItemCount() == MAX_NUM) {
            label.setMaxWidth((int)(size * 1.4));
            if (mScreenSize < DisplayMetrics.DENSITY_XHIGH) {
                label.setMaxWidth((int)(size * 1.3));
            }
        } else {
    	    label.setMaxWidth((int)(size * 1.7));
        }

        label.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

//        setViewBackground(layout);
        layout.setClickable(true);

        layout.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int action = event.getAction();
                mView = v;
                switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (mPendingCheckForLongPress == null) {
                    	mPendingCheckForLongPress = new Runnable() {
                            public void run() {
                                if (startLongClick(mView)) {
                                	mHasPerformedLongPress = true;
                                }
                            }
                        };
                    }
                    mHasPerformedLongPress = false;
                    v.postDelayed(mPendingCheckForLongPress, 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mHasPerformedLongPress) {
                        if (mPendingCheckForLongPress != null) {
                            v.removeCallbacks(mPendingCheckForLongPress);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mPendingCheckForLongPress != null) {
                        v.removeCallbacks(mPendingCheckForLongPress);
                        v.setPressed(false);
                        mHasPerformedLongPress = false;
                    }
                    break;
                default:
                    break;
                }
                return true;
            }
        });

        layout.addView(label);
        layout.setTag(item.getKey());
        return layout;
    }

    private void animateMoveAllItems() {
        Animation rotateAnimation = createFastRotateAnimation();

        for (int i = 0; i < getItemViewCount(); i++) {
            View child = getChildAt(i);
            child.startAnimation(rotateAnimation);
        }
    }

    private void cancelAnimations() {
        for (int i = 0; i < getItemViewCount(); i++) {
            View child = getChildAt(i);
            child.clearAnimation();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return onTouch(null, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            touchDown(event);
            break;
        case MotionEvent.ACTION_MOVE:
            if (v != null) {
                if (mHasPerformedLongPress) {
                    if (mPendingCheckForLongPress != null) {
                        v.removeCallbacks(mPendingCheckForLongPress);
                    }
                }
            }
            touchMove(event);
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (v != null) {
                if (mHasPerformedLongPress) {
                    if (mPendingCheckForLongPress != null) {
                        v.removeCallbacks(mPendingCheckForLongPress);
                        v.setPressed(false);
                        mHasPerformedLongPress = false;
                    }
                }
            }
            touchUp(event);
            break;
        default:
        	break;
        }
        if (aViewIsDragged()) {
            return true;
        }
        return false;
    }

    private void touchUp(MotionEvent event) {
        if (!aViewIsDragged()) {
            if (mOnClickListener != null) {
                View clickedView = getChildAt(getTargetAtCoor((int)event.getX(), (int)event.getY()));
                if (clickedView != null) {
                    mOnClickListener.onClick(clickedView);
                }
            }
        } else {

            int lastTouchX = (int)event.getX();
            int lastTouchY = (int)event.getY();

            lastTouchX = rangeXCheck(lastTouchX);
            lastTouchY = rangeYCheck(lastTouchY);

            cancelAnimations();
            reorderChildren(lastTouchX, lastTouchY);
            mDraggingItemIndex = -1;
            lastTarget = -1;
            mButtonItemManager.builderitemlist(mButtonItemManager.getUpTrayItems(), mButtonItemManager.getUpTrayItems());
        }
    }

    private void touchDown(MotionEvent event) {
        mFirstTouchX = (int)event.getRawX();
        mFirstTouchY = (int)event.getRawY();
    }

    private void touchMove(MotionEvent event) {
        if (aViewIsDragged()) {
            int lastTouchX = (int)event.getX();
            int lastTouchY = (int)event.getY();

            lastTouchX = rangeXCheck(lastTouchX);
            lastTouchY = rangeYCheck(lastTouchY);

            moveDraggedView(lastTouchX, lastTouchY);
            manageSwapPosition(lastTouchX, lastTouchY);
        }
    }

    public void pauseTouch() {
        Log.d(TAG, "pauseTouch()");
        cancelAnimations();
        mDraggingItemIndex = -1;
        lastTarget = -1;
        updateView();
    }

    private void moveDraggedView(int x, int y) {
        View childAt = getDraggedView();
        int width = childAt.getMeasuredWidth();
        int height = childAt.getMeasuredHeight();

        int l = x - (1 * width / 2);
        int t = y - (1 * height / 2);

        childAt.layout(l, t, l + width, t + height);
    }

    private int rangeYCheck(int y) {
        View childAt = getDraggedView();

        int height = childAt.getMeasuredHeight();
        int parentHeight = getMeasuredHeight();
        if (y - height / 2 < 0) {
            y = height / 2;
        } else if (y + height / 2 > parentHeight) {
            y = parentHeight - height / 2;
        }
        return y;
    }

    private int rangeXCheck(int x) {
        View childAt = getDraggedView();

        int width = childAt.getMeasuredWidth();
        int parentWidth = getMeasuredWidth();
        if (x - width / 2 < 0) {
            x = width / 2;
        } else if (x + width / 2 > parentWidth) {
            x = parentWidth - width / 2;
        }
        return x;
    }

    private void manageSwapPosition(int x, int y) {
        //Log.d("chan", "manageSwapPosition");
        int target = getTargetAtCoor(x, y);
        int row = getRowOfCoordinate(y);
        if (row > 0) {
            return;
        }

        ButtonItem selectedItem = mButtonItemManager.getItem(mDraggingItemKey);
        ButtonItem targetItem = mButtonItemManager.getItem(target);

        if (selectedItem != null && targetItem != null) {
            Log.d(TAG, "manageSwapPosition:selectedItem = " + selectedItem.getKey());
            Log.d(TAG, "manageSwapPosition:targetItem = " + targetItem.getKey());

            boolean movable = mButtonItemManager.needAnimation(selectedItem, targetItem);

            if (target != lastTarget && movable) {
                if (animateSwapOrMove(target)) {
                    lastTarget = target;
                } else {
                    lastTarget = -1;
                }
            }
        }
    }

    private void removeItemChildren(List<View> children) {
        for (View child : children) {
            removeView(child);
        }
    }

    private boolean animateSwapOrMove(int targetIndex) {
        int indexOfViewToBeMoved = getIndexOfViewToBeMoved(targetIndex);

        if (indexOfViewToBeMoved == mDraggingItemIndex) {
            return false;
        }

        if (mDraggingItemKey == null) {
            return false;
        }

        if (mButtonItemManager.getItem(mDraggingItemKey) == null) {
            return false;
        }

        View targetView = getChildView(indexOfViewToBeMoved);

        Point fromXYOfViewToBeMoved = getCoorForIndex(indexOfViewToBeMoved);
        Point toXYOfViewToBeMoved = getCoorForIndex(newPositions.get(mDraggingItemIndex,
                mDraggingItemIndex));
        boolean needLeftRightSwap = true;

        if (mButtonItemManager.isInOriginDownTray((String)targetView.getTag())
                && mButtonItemManager.findItemAtUpTray(mButtonItemManager.getItem(mDraggingItemKey).getKey()) == false
                &&
                mButtonItemManager.isInOriginDownTray(mButtonItemManager.getItem(mDraggingItemKey).getKey())) {
            toXYOfViewToBeMoved = getCoorForView(targetView);
            needLeftRightSwap = false;
        }

        if (fromXYOfViewToBeMoved == null || toXYOfViewToBeMoved == null) {
            return false;
        }

        Point oldOffset = computeTranslationStartDeltaRelativeToRealViewPosition(targetIndex,
                indexOfViewToBeMoved, fromXYOfViewToBeMoved);
        Point newOffset = computeTranslationEndDeltaRelativeToRealViewPosition(
                fromXYOfViewToBeMoved, toXYOfViewToBeMoved);

        if (oldOffset != null) {
            animateMoveToNewPosition(targetView, oldOffset, newOffset); 
        }
        if (needLeftRightSwap == true) {
            saveNewPositions(targetIndex, indexOfViewToBeMoved);			
        } else {
            ButtonItem draggedItem = mButtonItemManager.getItem(mDraggingItemKey);
            ButtonItem movedItem = mButtonItemManager.getItem(indexOfViewToBeMoved);
            if (draggedItem != null && movedItem != null) {
                Log.d(TAG,
                        "draggedItem = " + draggedItem.getKey() + ", movedItem = "
                                + movedItem.getKey());
                mButtonItemManager.moveToUpTray(draggedItem.getKey(), indexOfViewToBeMoved);
                mButtonItemManager.moveToDownTray(movedItem.getKey());
                newPositions.put(mDraggingItemIndex, targetIndex); 
                mButtonItemManager.printItems();
            }
        }
        return true;
    }

    private Point computeTranslationEndDeltaRelativeToRealViewPosition(Point oldXY, Point newXY) {
        return new Point(newXY.x - oldXY.x, newXY.y - oldXY.y);
    }

    private Point computeTranslationStartDeltaRelativeToRealViewPosition(int targetLocation,
            int viewAtPosition, Point oldXY) {
        Point oldOffset = null;
        if (viewWasAlreadyMoved(targetLocation, viewAtPosition)) {
            Point targetLocationPoint = getCoorForIndex(targetLocation);
            if (targetLocationPoint != null) {
                oldOffset = computeTranslationEndDeltaRelativeToRealViewPosition(oldXY,
                        targetLocationPoint);
            }
        } else {
            oldOffset = new Point(0, 0);
        }
        return oldOffset;
    }

    private void saveNewPositions(int targetLocation, int indexOfViewToBeMoved) {
        newPositions.put(indexOfViewToBeMoved,
                newPositions.get(mDraggingItemIndex, mDraggingItemIndex));
        newPositions.put(mDraggingItemIndex, targetLocation);
        mButtonItemManager.swapItems(newPositions.get(mDraggingItemIndex, mDraggingItemIndex),
                newPositions.get(indexOfViewToBeMoved, indexOfViewToBeMoved));
    }

    private boolean viewWasAlreadyMoved(int targetLocation, int viewAtPosition) {
        return viewAtPosition != targetLocation;
    }

    private void animateMoveToNewPosition(View targetView, Point oldOffset, Point newOffset) {
        AnimationSet set = new AnimationSet(true);

        Animation rotate = createFastRotateAnimation();
        Animation translate = createTranslateAnimation(oldOffset, newOffset);

        set.addAnimation(rotate);
        set.addAnimation(translate);

        targetView.clearAnimation();
        targetView.startAnimation(set);
    }

    /*Moving animation*/
    private TranslateAnimation createTranslateAnimation(Point oldOffset, Point newOffset) {
        TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, oldOffset.x,
                Animation.ABSOLUTE, newOffset.x,
                Animation.ABSOLUTE, oldOffset.y,
                Animation.ABSOLUTE, newOffset.y);
        translate.setDuration(ANIMATION_DURATION);
        translate.setFillEnabled(true);
        translate.setFillAfter(true);
        translate.setInterpolator(new AccelerateDecelerateInterpolator());
        return translate;
    }

    private Animation createFastRotateAnimation() {
        Animation rotate = new RotateAnimation(-1f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        rotate.setRepeatMode(Animation.REVERSE);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(100);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

        return rotate;
    }

    private int getIndexOfViewToBeMoved(int targetLocation) {
        int viewAtPosition = targetLocation;
        for (int i = 0; i < newPositions.size(); i++) {
            int value = newPositions.valueAt(i);
            if (value == targetLocation) {
                viewAtPosition = newPositions.keyAt(i);
                break;
            }
        }
        return viewAtPosition;
    }

    private Point getCoorForView(View view) {
        String key = (String)view.getTag();
        int col = ButtonConfiguration.getColumnOfDownTray(key);
        if (col < 0) {
            return null;
        }
        int columnWidthSize = mColumnWidthSize[1];
        int rowHeightSize = mRowHeightSize[1];

        int x = columnWidthSize * col;
        int y = rowHeightSize;

        return new Point(x, y);
    }

    private Point getCoorForIndex(int index) {
        if (index < 0 /*|| mAdapter.getItemCount() <= index*/) {
            return null;
        }
        Log.d(TAG, "getCoorForIndex:index=" + index);
        int row = index < mButtonItemManager.getColumnCount()[0] ? 0 : 1;
        int col = index - (row * mButtonItemManager.getColumnCount()[0]);

        int columnWidthSize = mColumnWidthSize[row];
        int rowHeightSize = mRowHeightSize[row];

        int x = columnWidthSize * col;
        int y = rowHeightSize * row;

        return new Point(x, y);
    }

    private int getTargetAtCoor(int x, int y) {
        int row = getRowOfCoordinate(y);

        int col = getColumnOfCoordinate(x, y, mButtonItemManager.getColumnCount()[row], mColumnWidthSize[row]);

        int positionIndex = col + (row * mButtonItemManager.getColumnCount()[0]);

        return positionIndex;
    }

    private int getColumnOfCoordinate(int x, int y, int columnCount, int columnWith) {
        int col = 0;

        int row = getRowOfCoordinate(y);
        row = (row > 1) ? 1 : row;
        for (int i = 1; i <= columnCount; i++) {
            int colRightBorder = (i * columnWith);
            if (x < colRightBorder) {
                break;
            }
            col++;
        }
        return col;
    }

    private int getRowOfCoordinate(int y) {
        int row = 0;
        for (int i = 1; i < MAX_ROW_COUNT; i++) {
            if (y < i * mRowHeightSize[0]) {
                break;
            }
            row++;
        }
        return row;
    }

    private int getTargetAtCoorOfUpTray(int x) {
        int widthSize = mColumnWidthSize[0] * mButtonItemManager.getColumnCount()[0];
        mColumnWidthSize[0] = widthSize / mButtonItemManager.getColumnCount()[0];
        int col = getColumnOfCoordinate(x, 0, mButtonItemManager.getColumnCount()[0] + 1, widthSize
                / (mButtonItemManager.getColumnCount()[0] + 1));
        return col;
    }



    private boolean calculateItems(int x, int y) {
        /* get target row : 0 or 1 ?*/
        int row = getRowOfCoordinate(y);
        ButtonItem draggingItem = mButtonItemManager.getItem(mDraggingItemKey);
        Log.d(TAG, "calculateItems: row=" + row);

        if (draggingItem == null) {
            return false;
        }

        if (row == 0
                && mButtonItemManager.getUpTrayItemCount() < ButtonConfiguration.CONFIG_MAX_UP_TRAY_KEY_NUM) {
            int column = getTargetAtCoorOfUpTray(x);
            mButtonItemManager.moveToUpTray(draggingItem.getKey(), column);
            updateView();
            return true;
        } else if (row == 1
                && mButtonItemManager.getUpTrayItemCount() > ButtonConfiguration.CONFIG_INITIAL_UP_TRAY_KEY_NUM) {
            if (draggingItem.getType() == ButtonItem.KEYTYPE_UP_TRAY) {
                return false;
            }
            mButtonItemManager.moveToDownTray(draggingItem.getKey());
            updateView();
            return true;
        }
        return false;
    }

    private void updateView() {
        List<View> children = cleanUnorderedChildren();
        for (View view : children) {
            Log.d(TAG, "updateView = " + view.getTag());
        }
        View[] views = new View[children.size()];
        int index = 0;
        for (int j = 0; j < mButtonItemManager.getItemCount(); j++) {
            ButtonItem item = mButtonItemManager.getItem(j);
            if (item == null) {
                continue;
            }

            for (int i = 0; i < children.size(); i++) {
                String key = (String)children.get(i).getTag();
                if (key.equals(item.getKey())) {
                    views[index++] = children.get(i);
                }
            }
        }

        ArrayList<View> reorderedViews = new ArrayList<View>(Arrays.asList(views));
        newPositions.clear();
        for (View view : reorderedViews) {
            if (view != null) {
                ViewGroup vg = (ViewGroup)view.getParent();
                if (vg != null) {
                    vg.removeView(view);
                }
                addView(view);
            }
        }
        requestLayout();
    }

    private void reorderChildren(int x, int y) {
        if (calculateItems(x, y)) {
            return;
        }
        updateView();
    }

    private List<View> cleanUnorderedChildren() {
        List<View> children = saveChildren();
        removeItemChildren(children);
        return children;
    }

    private List<View> saveChildren() {
        List<View> children = new ArrayList<View>();
        for (int i = 0; i < getItemViewCount(); i++) {
            View child;
            if (i == mDraggingItemIndex) {
                child = getDraggedView();
            } else {
                child = getChildView(i);
            }

            child.clearAnimation();
            children.add(child);
        }
        return children;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();

        widthSize = acknowledgeWidthSize(widthMode, widthSize, display);
        heightSize = acknowledgeHeightSize(heightMode, heightSize, display);

        adaptChildrenMeasuresToViewSize(widthSize, heightSize);

        computeGridMatrixSize(widthSize, heightSize);
        computeColumnsAndRowsSizes(widthSize, heightSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    private float getPixelFromDip(int size) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                r.getDisplayMetrics());
        return px;
    }

    public void computeColumnsAndRowsSizes(int widthSize, int heightSize) {
        mColumnWidthSize[0] = widthSize / mButtonItemManager.getColumnCount()[0];
        mColumnWidthSize[1] = widthSize / ButtonConfiguration.CONFIG_FIXED_DOWN_TRAY_KEY_NUM;
        mRowHeightSize[0] = mRowHeightSize[1] = heightSize / 2;
    }

    public void computeGridMatrixSize(int widthSize, int heightSize) {
    	int[] ColumnCount = { 0, 0 };
    	ColumnCount[0] = mButtonItemManager.getUpTrayItemCount();
    	ColumnCount[1] = ButtonConfiguration.CONFIG_FIXED_DOWN_TRAY_KEY_NUM;
    	mButtonItemManager.setColumnCount(ColumnCount);
    }

    private int getItemViewCount() {
        return getChildCount();
    }

    private void adaptChildrenMeasuresToViewSize(int widthSize, int heightSize) {
        measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    private int acknowledgeHeightSize(int heightMode, int heightSize, Display display) {
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = display.getHeight();
        }

        int height = 0;
        int count = getChildCount();
        View view = getChildView(0);
        height = view.getMeasuredHeight();
        view = getChildView(count - 1);
        height += view.getMeasuredHeight();
        height += GRID_VIEW_PADDING;

        return height; //heightSize;
        //		return (int)getPixelFromDip(176);
    }

    private int acknowledgeWidthSize(int widthMode, int widthSize, Display display) {
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = display.getWidth();
        }
        return widthSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mSetLongClickFlag == true) {
            mSetLongClickFlag = false;
            return;
        }

        layout();

        if (aViewIsDragged()) {
            bringDraggedToFront();
        }
    }

    private void layout() {
        int index = 0;
        for (index = 0; index < mButtonItemManager.getUpTrayItemCount(); index++) {
            layoutAChild(index, 0, index, false);
        }

        index = mButtonItemManager.getUpTrayItemCount();
        for (int col = 0; col < ButtonConfiguration.CONFIG_FIXED_DOWN_TRAY_KEY_NUM; col++) {
            //if (col == mAdapter.getEmptyColumnAtDownTray())	continue;
            if (mButtonItemManager.getItemAtDownTray(col) == null) {
                continue;
            }
            layoutAChild(col, 1, index, true);
            index++;
        }
    }

    private void layoutAChild(int col, int row, int index, boolean withText) {
        View child = getChildAt(index);
        if (child == null) {
            Log.d(TAG, "layoutAChild: child is null index=" + index);
            return;
        }

        TextView tv = (TextView)child.findViewWithTag("text");
        if (tv != null) {
            int visibility = withText == true ? View.VISIBLE : View.INVISIBLE;
            tv.setVisibility(visibility);
        }

        int left = 0;
        int top = 0;
        left = (col * mColumnWidthSize[row])
                + ((mColumnWidthSize[row] - child.getMeasuredWidth()) / 2);
        top = (row * mRowHeightSize[row]) + ((mRowHeightSize[row] - child.getMeasuredHeight()) / 2);
        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }

    public boolean startLongClick(View v) {
        if (positionForView(v) != -1) {
            mDraggingItemIndex = positionForView(v);
            mDraggingItemKey = (String)v.getTag();
            Log.d(TAG, "startLongClick view=" + v.getTag() + ", mDraggingItemIndex="
                    + mDraggingItemIndex);
            v.setPressed(true);
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            bringDraggedToFront();
            animateMoveAllItems();
            animateDragged();
            mViewlabel = (TextView)v.findViewWithTag("text");
            mViewlabel.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    private void printViewOrder() {
        for (int i = 0; i < getItemViewCount(); i++) {
            View view = getChildAt(i);
            if (view != null) {
                Log.d(TAG, "printViewOrder_" + i + ": " + (String)view.getTag());
            }
        }
    }

    private void bringDraggedToFront() {
        mSetLongClickFlag = true;
        View draggedView = getChildAt(mDraggingItemIndex);
        draggedView.bringToFront();
    }

    private View getDraggedView() {
        return getChildAt(getChildCount() - 1);
    }

    private void animateDragged() {

        ScaleAnimation scale = new ScaleAnimation(1f, 1f, 1f, 1f, 0, 0);
        scale.setDuration(200);
        scale.setFillAfter(true);
        scale.setFillEnabled(true);

        if (aViewIsDragged()) {
            View draggedView = getDraggedView();
            draggedView.clearAnimation();
            draggedView.startAnimation(scale);
        }
    }

    public boolean aViewIsDragged() {
        return (mDraggingItemIndex != -1);
    }

    private int positionForView(View v) {
        for (int index = 0; index < getItemViewCount(); index++) {
            View child = getChildView(index);
            if (isPointInsideView(mFirstTouchX, mFirstTouchY, child)) {
                return index;
            }
        }
        return -1;
    }

    private View getChildView(int index) {
        if (aViewIsDragged()) {
            if (index >= mDraggingItemIndex) {
                return getChildAt(index - 1 < 0 ? 0 : index - 1);
            }
        }
        return getChildAt(index);
    }

    private boolean isPointInsideView(float x, float y, View view) {
        if (view == null) {
            return false;
        }

        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        if (pointIsInsideViewBounds(x, y, view, viewX, viewY)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean pointIsInsideViewBounds(float x, float y, View view, int viewX, int viewY) {
        return (x > viewX && x < (viewX + view.getWidth()))
                && (y > viewY && y < (viewY + view.getHeight()));
    }
}
