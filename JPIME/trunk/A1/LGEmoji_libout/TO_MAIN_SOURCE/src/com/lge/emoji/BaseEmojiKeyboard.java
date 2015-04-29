package com.lge.emoji;

import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import android.inputmethodservice.InputMethodService;

public class BaseEmojiKeyboard extends EmojiKeyboard {

    public BaseEmojiKeyboard(InputMethodService ims) {
        super(ims);
    }
}
