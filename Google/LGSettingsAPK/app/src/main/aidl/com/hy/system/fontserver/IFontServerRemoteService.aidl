/*
***************************************************************************
** IFontServerRemoteService.aidl
***************************************************************************
**
** 2012.07.17 Android JB
**
** Mobile Communication R&D Center, Hanyang
** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
**
** This code is a program that changes the font.
**
***************************************************************************
*/
package com.hy.system.fontserver;
interface IFontServerRemoteService {
    void updateFontServer();
    void selectDefaultTypeface(int fontIndex);
    int getDefaultTypefaceIndex();
    int getNumAllFonts();
    int getNumEmbeddedFonts();
    String getSummary();
    String[] getAllFontNames();
    String[] getAllFontWebFaceNames();
    String[] getAllFontFullPath();
    String getFontFullPath(int fontIndex);
    String getDownloadFontSrcPath();
    String getDownloadFontDstPath();
    int getSystemDefaultTypefaceIndex();
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
    String getDownloadFontAppName(int fontIndex);
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]
    int setDefault();
}