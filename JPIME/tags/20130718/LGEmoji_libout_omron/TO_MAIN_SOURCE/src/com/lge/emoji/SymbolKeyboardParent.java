package com.lge.emoji;


import jp.co.omronsoft.iwnnime.ml.InputMethodBase;
import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import android.inputmethodservice.InputMethodService;

public class SymbolKeyboardParent extends InputMethodBase {
    public SymbolKeyboardParent(InputMethodService ims) {
        super((InputMethodSwitcher)ims);
    }
    
    public SymbolKeyboardParent() {
        super(null);
    }
    
    public String getString(int resId) {
        return getResources().getString(resId);
    }
}
