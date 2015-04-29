package com.lge.handwritingime.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lge.handwritingime.R;

public class PenTypePreference extends DialogPreference implements View.OnClickListener {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    
    private static final int DEFAULT_CURRENT_VALUE = R.string.HW_PEN_TYPE_BALLPEN;
    
    private Context mContext;
    private final int mDefaultValueId;
    private String mCurrentValue;
    
    private ImageView mPenTypeSample;
    private ImageButton mButtonBallpen;
    private ImageButton mButtonInkpen;
    private ImageButton mButtonBrush;
    private ImageButton mButtonPencil;
    
    private SparseIntArray mMapSample;
    private SparseArray<String> mMapButtonString;
    
    public PenTypePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDefaultValueId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
        
        mMapSample = new SparseIntArray();
        mMapSample.put(R.id.buttonBallpen, R.drawable.hw_popup_pen_type_ballpen);
        mMapSample.put(R.id.buttonInkpen, R.drawable.hw_popup_pen_type_inkpen);
        mMapSample.put(R.id.buttonBrush, R.drawable.hw_popup_pen_type_brush);
        mMapSample.put(R.id.buttonPencil, R.drawable.hw_popup_pen_type_pencil);
        
        mMapButtonString = new SparseArray<String>();
        mMapButtonString.put(R.id.buttonBallpen, mContext.getString(R.string.HW_PEN_TYPE_BALLPEN));
        mMapButtonString.put(R.id.buttonInkpen, mContext.getString(R.string.HW_PEN_TYPE_INKPEN));
        mMapButtonString.put(R.id.buttonBrush, mContext.getString(R.string.HW_PEN_TYPE_BRUSH));
        mMapButtonString.put(R.id.buttonPencil, mContext.getString(R.string.HW_PEN_TYPE_PENCIL));
    }

    
    @Override
    protected View onCreateDialogView() {
        mCurrentValue = getPersistedString(mContext.getString(mDefaultValueId));
        
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pen_type_dialog, null);
        
        mPenTypeSample = (ImageView) view.findViewById(R.id.penTypeSample);
        
        mButtonBallpen = (ImageButton) view.findViewById(R.id.buttonBallpen);
        mButtonInkpen = (ImageButton) view.findViewById(R.id.buttonInkpen);
        mButtonBrush = (ImageButton) view.findViewById(R.id.buttonBrush);
        mButtonPencil = (ImageButton) view.findViewById(R.id.buttonPencil);
        
        mButtonBallpen.setOnClickListener(this);
        mButtonInkpen.setOnClickListener(this);
        mButtonBrush.setOnClickListener(this);
        mButtonPencil.setOnClickListener(this);
        
        int index = 0;
        for (int i = 0; i < mMapButtonString.size(); i++) {
            if (mCurrentValue.equals(mMapButtonString.valueAt(i))) {
                index = i;
                break;
            }
        }
        View v = view.findViewById(mMapButtonString.keyAt(index));
        v.setSelected(true);
        mPenTypeSample.setImageResource(mMapSample.get(v.getId()));
        
        return view;
    }
    
    @Override
    public void onClick(View v) {
        unselectAllButtons();
        v.setSelected(true);
        mPenTypeSample.setImageResource(mMapSample.get(v.getId()));
        mCurrentValue = mMapButtonString.get(v.getId());
    }
    

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) {
            return;
        }
        if (shouldPersist()) {
            persistString(mCurrentValue);
        }
        notifyChanged();
    }

    
    private void unselectAllButtons() {
        mButtonBallpen.setSelected(false);
        mButtonInkpen.setSelected(false);
        mButtonBrush.setSelected(false);
        mButtonPencil.setSelected(false);
    }
}
