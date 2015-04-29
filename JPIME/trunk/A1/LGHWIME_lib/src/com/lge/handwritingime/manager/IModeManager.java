package com.lge.handwritingime.manager;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.widget.FrameLayout;

import com.visionobjects.im.Result;

public interface IModeManager {
    public void onCreateStrokeLayout(FrameLayout strokeLayout);    
    public void deleteStroke();
    public void deleteOne();
    public void deleteText();
    public void destroy();
    public void clear();
    
    public ArrayList<String> getSuggestions();
    public void pickSuggestionManually(int index);
    public void pickSuggestionManually(String string);
    public void setResult(Result result);
    public void setValuesByPrefs(SharedPreferences sharedPref);
    public boolean dismissCharButtonPopups();
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd);
    
    public void dPadBackward();
    public void dPadForward();
}
