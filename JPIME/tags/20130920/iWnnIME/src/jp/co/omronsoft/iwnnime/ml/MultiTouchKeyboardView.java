/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.KeyEvent;

import jp.co.omronsoft.iwnnime.ml.Keyboard.Key;

import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The view that reders a virtual Keyboard. It can multi touch.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class MultiTouchKeyboardView extends KeyboardView {

    /** Multi touch enable point max */
    private static final int MULTI_TOUCH_MAX = 10;

    /** Touch stop range */
    private static final int MULTI_TOUCH_STOP_RANGE = 80;

    /** Temporarily id none. */
    private int TEMPORARILY_ID_NONE = -1;

    /** List of touch point. */
    private LinkedList<TouchPoint> mTouchPoints = new LinkedList<TouchPoint>();

    /** List of ignore up id. */
    private ArrayList<Integer> mIgnoreTouchId = new ArrayList<Integer>();

    /** True is Repeatkey touched. */
    private boolean mIsRepeatKeyTouch = false;

    /** Shift keyboard. */
    private Keyboard mShiftKeyboard = null;

    /** Normal keyboard. */
    private Keyboard mNormalKeyboard = null;

    /** Temporarily id. */
    private int mTemporarilyId = TEMPORARILY_ID_NONE;

    /** Temporarily inputed. */
    private boolean mIsTemporarilyInputed = false;

    /** Temporarily keycode. */
    private int mTemporarilyKeycode = KeyEvent.KEYCODE_UNKNOWN;

    /** Temporarily keycode for Previopus Keyboard. */
    private int mTempPreviousKeyboard;

    /** Temporarily down touch point. */
    private TouchPoint mDownTouchPoint = null;

    /** Shift status. */
    private boolean mShifted = false;

    /** Caps status. */
    private boolean mCaps = false;

    /** Keyboard listener for flick action */
    private OnFlickKeyboardActionListener mFlickKeyboardActionListener;

    /**
     * Touch point.
     */
    static class TouchPoint {
        /* Id */
        public int mId;
        /* Point x */
        public float mX;
        /* Point y */
        public float mY;
        /* Meta state */
        public int mMetaState;

        /** Constructor */
        public TouchPoint(int id, float x, float y, int metaState) {
            mId = id;
            mX = x;
            mY = y;
            mMetaState = metaState;
        }

        /** Constructor */
        public TouchPoint(int index, MotionEvent event) {
            mId = event.getPointerId(index);
            mX = event.getX(index);
            mY = event.getY(index);
            mMetaState = event.getMetaState();
        }
    }

    /**
     * Constructor
     */
    public MultiTouchKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     */
    public MultiTouchKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** @see android.inputmethodservice.KeyboardView#onTouchEvent */
    @Override public boolean onTouchEvent(MotionEvent ev) {
        OpenWnn wnn = OpenWnn.getCurrentIme();
        if (wnn == null) {
            return true;
        }
        int action = ev.getActionMasked();    //for froyo
//        int action = ev.getAction() & MotionEvent.ACTION_MASK;  //for eclair
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                wnn.cancelToast();
                return onTouchEventDown(ev);

            case MotionEvent.ACTION_MOVE:
                return onTouchEventMove(ev);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return onTouchEventUp(ev);

            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_OUTSIDE:
                break;

            default:
                break;

        }

        return super.onTouchEvent(ev);
    }

    /**
     * Touch event Down.
     *
     * @param event MotionEvent.
     * @return boolean result.
     */
    private boolean onTouchEventDown(MotionEvent event) {
        long time = event.getEventTime();
        int index = event.getActionIndex();    //for froyo
//        int index = ((event.getAction() & 0xff00) >> 8);  //for eclair
        int id = event.getPointerId(index);

        if (mMiniKeyboardOnScreen) {
            dismissPopupKeyboard();
            return true;
        }

        if (index >= MULTI_TOUCH_MAX) {
            return true;
        }

        mIgnoreTouchId.remove(Integer.valueOf(id));
        TouchPoint point = new TouchPoint(index, event);
        Keyboard.Key key = getKey((int)point.mX, (int)point.mY);
        if ((key != null)
            && !(key.isSplitKey() && !key.isInsideSplitKey((int)point.mX, (int)point.mY))) {
            if (mFlickKeyboardActionListener != null) {
                mFlickKeyboardActionListener.onPress(key.codes[0]);
            }
            if (key.repeatable) {
                mIsRepeatKeyTouch = true;
                executeQueueTouchEvent(event, -1);
            // shift temporarily input
            } else if (!mShifted && key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                if (mTemporarilyId != TEMPORARILY_ID_NONE) {
                    setIgnoreTouchId(mTemporarilyId);
                    mTemporarilyId = TEMPORARILY_ID_NONE;
                }
                executeQueueTouchEvent(event, -1);
                if (!mShifted) {
                    mTouchPoints.add(point);
                    executeQueueTouchEvent(event, -1);
                    mIgnoreTouchId.remove(Integer.valueOf(id));
                }

                mTemporarilyId = id;
                mIsTemporarilyInputed = false;
                mDownTouchPoint = point;
                mTemporarilyKeycode = key.codes[0];
                return executeTouchAction(time, MotionEvent.ACTION_DOWN, point);
            // num mode key temporarily input
            } else if ((key.codes[0] == DefaultSoftKeyboard.KEYCODE_SWITCH_HALF_NUMBER_TEMP_INPUT) ||
                        (key.codes[0] == DefaultSoftKeyboard.KEYCODE_SWITCH_FULL_NUMBER_TEMP_INPUT) ||
                        (key.codes[0] == DefaultSoftKeyboard.KEYCODE_SWITCH_RU_NUMBER_TEMP_INPUT) ||
                        (key.codes[0] == DefaultSoftKeyboard.KEYCODE_SWITCH_CH_NUMBER_TEMP_INPUT) ||
                        (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE_TEMP_INPUT)) {
                if (mTemporarilyId != TEMPORARILY_ID_NONE) {
                    setIgnoreTouchId(mTemporarilyId);
                    mTemporarilyId = TEMPORARILY_ID_NONE;
                }
                mTouchPoints.add(point);
                executeQueueTouchEvent(event, -1);
                mIgnoreTouchId.remove(Integer.valueOf(id));

                mTemporarilyId = id;
                mIsTemporarilyInputed = false;
                mDownTouchPoint = point;
                mTemporarilyKeycode = key.codes[0];
                return executeTouchAction(time, MotionEvent.ACTION_DOWN, point);
            }
        } else {
            setIgnoreTouchId(id);
            return true;
        }
        mTouchPoints.add(point);
        if (mIsRepeatKeyTouch && !key.repeatable) {
            return true;
        }
        return executeTouchAction(time, MotionEvent.ACTION_DOWN, point);
    }

    /**
     * Touch event Move.
     *
     * @param event MotionEvent.
     * @return boolean result.
     */
    private boolean onTouchEventMove(MotionEvent event) {
        long time = event.getEventTime();
        int lastIndex = event.getPointerCount() - 1;
        if (lastIndex >= MULTI_TOUCH_MAX ) {
            lastIndex = MULTI_TOUCH_MAX - 1;
        }
        TouchPoint point = new TouchPoint(lastIndex, event);
        int lastId = event.getPointerId(lastIndex);
        if (lastIndex == 0 && lastId == mTemporarilyId) {
            float y = event.getY(lastIndex);
            if (y >= 0) {
                return executeTouchAction(time, MotionEvent.ACTION_MOVE, point);
            }
        } else {
            if (mPopupKeyboard.isShowing()) {
                return executeTouchAction(time, MotionEvent.ACTION_MOVE, point);
            }
            int size = mTouchPoints.size();
            if (size > 0) {
                lastId = mTouchPoints.get(size - 1).mId;
                lastIndex = event.findPointerIndex(lastId);
                for (int i = 0; i < size; i++) {
                    TouchPoint tp = mTouchPoints.get(i);
                    int pointerIndex = event.findPointerIndex(tp.mId);
                    if (pointerIndex >= 0 && pointerIndex != lastIndex) {
                        if (Math.abs(event.getX(pointerIndex) - tp.mX) > MULTI_TOUCH_STOP_RANGE ||
                                Math.abs(event.getY(pointerIndex) - tp.mY) > MULTI_TOUCH_STOP_RANGE) {
                            setIgnoreTouchId(tp.mId);
                            mTouchPoints.remove(i);
                            size = mTouchPoints.size();
                        }
                    }
                }
            }
            if (!mIgnoreTouchId.contains(Integer.valueOf(lastId)) && size != 0) {
                if (lastIndex >= 0) {
                    float y = event.getY(lastIndex);
                    if (y >= 0) {
                        mTouchPoints.removeLast();
                        mTouchPoints.add(point);
                        return executeTouchAction(time, MotionEvent.ACTION_MOVE, point);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Touch event Up.
     *
     * @param event MotionEvent.
     * @return boolean result.
     */
    private boolean onTouchEventUp(MotionEvent event) {
        long time = event.getEventTime();
        int index = event.getActionIndex();    //for froyo
//        int index = ((event.getAction() & 0xff00) >> 8);  //for eclair
        int id = event.getPointerId(index);
        TouchPoint point = new TouchPoint(index, event);

        if (index >= MULTI_TOUCH_MAX) {
            return true;
        }

        boolean result = true;
        if (id == mTemporarilyId) {
            mTemporarilyId = TEMPORARILY_ID_NONE;
            executeQueueTouchEvent(event, -1);
            result = executeTouchAction(time, MotionEvent.ACTION_CANCEL, point);

            // After another key touch up or, Not touch up on key
            if (mIsTemporarilyInputed) {
                switch (mTemporarilyKeycode) {
                case Keyboard.KEYCODE_SHIFT:
                    setShifted(false);
                    break;

                case DefaultSoftKeyboard.KEYCODE_SWITCH_RU_NUMBER_TEMP_INPUT:
                case DefaultSoftKeyboard.KEYCODE_SWITCH_CH_NUMBER_TEMP_INPUT:
                    mFlickKeyboardActionListener.onKey(mTempPreviousKeyboard, null);
                    mFlickKeyboardActionListener.onRelease(mTempPreviousKeyboard);
                    break;


                case DefaultSoftKeyboard.KEYCODE_SWITCH_HALF_NUMBER_TEMP_INPUT:
                case DefaultSoftKeyboard.KEYCODE_SWITCH_FULL_NUMBER_TEMP_INPUT:
                case DefaultSoftKeyboard.KEYCODE_QWERTY_TOGGLE_MODE_TEMP_INPUT:
                    executeTouchAction(time, MotionEvent.ACTION_DOWN, mDownTouchPoint);
                    result = executeTouchAction(time, MotionEvent.ACTION_UP, mDownTouchPoint);
                    break;

                default:
                    break;

                }
            }
        } else {
            if (mPopupKeyboard.isShowing()) {
                result = executeTouchAction(time, MotionEvent.ACTION_UP, point);
            } else if (!mIgnoreTouchId.contains(Integer.valueOf(id))) {
                result = executeQueueTouchEvent(event, id);
            }
        }
        return result;
    }

    /**
     * Get key from position.
     *
     * @param positionX  x position.
     * @param positionY  y position.
     */
    public Keyboard.Key getKey(int positionX, int positionY) {
        Keyboard.Key positionKey = null;
        Keyboard keyboard = getKeyboard();
        int keyboardX = positionX - getPaddingLeft();
        int keyboardY = positionY - getPaddingTop();
        int [] keyIndices = keyboard.getNearestKeys(keyboardX, keyboardY);
        final int keyCount = keyIndices.length;

        if (keyCount > 0) {
            List<Keyboard.Key> keys = keyboard.getKeys();
            int i;
            for (i = 0; i < keyCount; i++) {
                final Keyboard.Key key = keys.get(keyIndices[i]);
                if (key.isInside(keyboardX, keyboardY)) {
                    positionKey = key;
                }
            }
        }
        return positionKey;
    }

    /**
     * Execute touch event in queue.
     *
     * @param event MotionEvent.
     * @param stopId stop id, if stopId is -1 all touch event execute.
     * @return boolean execute result.
     */
    private boolean executeQueueTouchEvent(MotionEvent event, int stopId) {
        long time = event.getEventTime();
        boolean result = true;

        TouchPoint point = mTouchPoints.poll();
        while (point != null) {
            Keyboard.Key pointKey = getKey((int)point.mX, (int)point.mY);

            if (pointKey != null && pointKey.repeatable) {
                mIsRepeatKeyTouch = false;
            } else {
                executeTouchAction(time, MotionEvent.ACTION_DOWN, point);
            }
            result = executeTouchAction(time, MotionEvent.ACTION_UP, point);
            if (stopId == point.mId) {
                break;
            }
            setIgnoreTouchId(point.mId);
            point = mTouchPoints.poll();
        }
        if (mTemporarilyId != TEMPORARILY_ID_NONE) {
            mIsTemporarilyInputed = true;
        }
        return result;
    }

    /**
     * Execute touch action.
     *
     * @param time touch time.
     * @param action action kind.
     * @param point touch point.
     * @return boolean execute result.
     */
    private boolean executeTouchAction(long time, int action, TouchPoint point) {
        boolean result = false;

        MotionEvent motionEvent = MotionEvent.obtain(time, time, action, point.mX, point.mY,
                                                      point.mMetaState);
        result = super.onTouchEvent(motionEvent);
        motionEvent.recycle();
        return result;
    }

    /** @see android.inputmethodservice.KeyboardView#onLongPress */
    @Override protected boolean onLongPress(Key popupKey) {
        boolean result = false;

        if (mFlickKeyboardActionListener != null && mFlickKeyboardActionListener.onLongPress(popupKey)) {
            result = true;
        } else {
            result = super.onLongPress(popupKey);
        }

        if (result) {
            mTouchPoints.clear();
        }
        return result;
    }

    /** @see android.inputmethodservice.KeyboardView#setKeyboard */
    @Override public void setKeyboard(Keyboard keyboard) {
        if (keyboard != null) {
            super.setKeyboard(keyboard);
            if (mCaps) {
                setCapsLock(true);
            } else {
                setCapsLock(false);
            }
        }
    }

    /**
     * Set normal keyboard and shift keyboard.
     *
     * @param normalKeyboard normal keyboard.
     * @param shiftKeyboard shift keyboard.
     * @param enableTempShift if true enable temporarily shift.
     */
    public void setKeyboard(Keyboard normalKeyboard, Keyboard shiftKeyboard) {
        if (mNormalKeyboard != normalKeyboard && mShiftKeyboard != shiftKeyboard) {
            cancelTouchEvent();
        }
        mNormalKeyboard = normalKeyboard;
        mShiftKeyboard = shiftKeyboard;
        setShifted(mShifted);
    }

    /**
     * Set capslock.
     *
     * @param capslock capslock status ,if true set capslock.
     */
    public void setCapsLock(boolean capslock) {
        setCapsLockMode(capslock);
        if (mShiftKeyboard != null) {
            List<Key> keys = mShiftKeyboard.getModifierKeys();
            int size = keys.size();

            for (int i = 0; i < size; i++) {
                Key key = keys.get(i);
                if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                    int index = mShiftKeyboard.getKeys().indexOf(key);
                    key.on  = capslock;
                    if (getKeyboard() == mShiftKeyboard) {
                        invalidateKey(index);
                    }
                }
            }
        }
    }

    /**
     * Set capslock mode.
     *
     * @param capslock capslock status ,if true set capslock.
     */
    public void setCapsLockMode(boolean capslock) {
        mCaps = capslock;
    }

    /** @see android.inputmethodservice.KeyboardView#setShifted */
    @Override public boolean setShifted(boolean shift) {
        if (mTemporarilyId == TEMPORARILY_ID_NONE) {
            mShifted = shift;
            if (shift) {
                if (mShiftKeyboard != null) {
                    mShiftKeyboard.setShifted(true);
                }
                setKeyboard(mShiftKeyboard);
            } else {
                setKeyboard(mNormalKeyboard);
            }
            return true;
        }
        return false;
    }

    /** @see android.inputmethodservice.KeyboardView#setShifted */
    @Override public boolean isShifted() {
        return mShifted;
    }

    /**
     * Is capslock keyboard.
     *
     * @return capslock status ,if true capslock.
     */
    public boolean isCapsLock() {
        return mCaps;
    }

    /**
     * Cancel all touch event.
     */
    public void cancelTouchEvent() {
        mTemporarilyId = TEMPORARILY_ID_NONE;
        mIsRepeatKeyTouch = false;
        if (mTouchPoints.size() > 0) {
            TouchPoint lastPoint = mTouchPoints.getLast();
            if (lastPoint != null) {
                Keyboard.Key tmpkey = getKey((int)lastPoint.mX, (int)lastPoint.mY);
                if (tmpkey == null || tmpkey.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE) {
                    return;
                }
                executeTouchAction(System.currentTimeMillis(), MotionEvent.ACTION_CANCEL, lastPoint);
            }
        }
      TouchPoint point = mTouchPoints.poll();
      while (point != null) {
          setIgnoreTouchId(point.mId);
          point = mTouchPoints.poll();
      }
    }

    /**
     * Copy the enterkey state.
     */
    public void copyEnterKeyState() {
        if (mNormalKeyboard != null && mShiftKeyboard != null) {
            int normalKeyboardIndex = getEnterKeyIndex(mNormalKeyboard);
            int shiftKeyboardIndex = getEnterKeyIndex(mShiftKeyboard);

            if (normalKeyboardIndex != -1 && shiftKeyboardIndex != -1) {
                Key normalkey = mNormalKeyboard.getKeys().get(normalKeyboardIndex);
                Key shiftkey = mShiftKeyboard.getKeys().get(shiftKeyboardIndex);
                if (mShifted) {
                    normalkey.label = shiftkey.label;
                    normalkey.icon = shiftkey.icon;
                    normalkey.iconPreview = shiftkey.iconPreview;
                } else {
                    shiftkey.label = normalkey.label;
                    shiftkey.icon = normalkey.icon;
                    shiftkey.iconPreview = normalkey.iconPreview;
                }
            }
        }
    }

    /**
     * keycode to keyindex.
     *
     * @param keyboard    disp keyboard.
     * @param keycode     keycode.
     *
     * @return keyindex.
     */
    public static int getKeyIndex(Keyboard keyboard, int keycode) {
        int retindex = -1;
        List<Keyboard.Key> keys = keyboard.getKeys();
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            if (keys.get(i).codes[0] == keycode) {
                retindex = i;
                break;
            }
        }
        return retindex;
    }

    /**
     * get Enter Key Index .
     *
     * @param keyboard    disp keyboard.
     * @return Enter keyindex.
     */
    public static int getEnterKeyIndex(Keyboard keyboard) {
        int retKeycode = getKeyIndex(keyboard, DefaultSoftKeyboard.KEYCODE_QWERTY_ENTER);
        if (retKeycode == -1) {
            retKeycode = getKeyIndex(keyboard, DefaultSoftKeyboard.KEYCODE_JP12_ENTER);
        }
        return retKeycode;
    }

    /**
     * Called when sets the keyboard listener.
     *
     * @param listener OnFlickKeyboardActionListener.
     */
    public void setOnKeyboardActionListener(OnFlickKeyboardActionListener listener) {
        super.setOnKeyboardActionListener((OnKeyboardActionListener)listener);
        mFlickKeyboardActionListener = listener;
    }

    /**
     * Set ignore touch id.
     *
     * @param id ignore id.
     */
    private void setIgnoreTouchId(int id) {
        Integer setId = Integer.valueOf(id);
        if (!mIgnoreTouchId.contains(setId)) {
            mIgnoreTouchId.add(setId);
        }
    }

    /**
     * setTempPreviousKeyboard.
     *
     * @param code keycode.
     */
    public void setTempPreviousKeyboard(int code) {
        mTempPreviousKeyboard = code;
    }
}
