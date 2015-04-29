/*
***************************************************************************
** FontServerConnection.java
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
/****************************************************************************
** User Define : Package name, please change the name of your package.
****************************************************************************/
package com.android.settings;

/****************************************************************************
** Warning : Do not change.
****************************************************************************/
import com.hy.system.fontserver.IFontServerRemoteService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public class FontServerConnection implements ServiceConnection {
    private static final String TAG = "HyFontServerConnection";
    private static final String FONT_SERVER_CLASS = ".FontServer";
    private static final String FONT_SERVER_PACKAGE = "com.hy.system.fontserver";
    private static final String SYSTEM_FONTSERVER_BIND_ACTION = "android.intent.action.SYSTEM_FONTSERVER_BIND_ACTION";
    private IFontServerRemoteService service = null;
    public static final int FONT_SERVER_CONNECTED = 1010;

    /****************************************************************************
    ** User Define : Activity class name, please change the name of your Activity class.
    ****************************************************************************/
    private final Context parent;
    private final Handler handler;
    public FontServerConnection( Context parent, Handler handler ) {
        this.parent = parent;
        this.handler = handler;
    }

    /****************************************************************************
    ** Connect & Disconnect.
    ****************************************************************************/
    @Override
    public void onServiceConnected( ComponentName name, IBinder service ) {
        this.service = IFontServerRemoteService.Stub.asInterface( service );
        if( handler != null ) {
            handler.sendEmptyMessage( FONT_SERVER_CONNECTED ); // Send connected message.
        }
        Log.d( TAG, "FontServer Connected." );
    }

    @Override
    public void onServiceDisconnected( ComponentName name ) {
        service = null;
    }

    public void disconnectFontServerService() {
        if( service != null ) {
            parent.unbindService( this );
            service = null;
        }
    }

    public void connectFontServerService() {
        if( service == null ) {
            Intent bindIntent = new Intent( SYSTEM_FONTSERVER_BIND_ACTION );
            bindIntent.setClassName( FONT_SERVER_PACKAGE, FONT_SERVER_PACKAGE + FONT_SERVER_CLASS );

            // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
            // KK Support Update ( include Multi-User )
            if (FontSettings.isAdmin(parent) == true) {
                parent.startService(bindIntent);
            }

            // if( parent.bindService( bindIntent, this, Context.BIND_AUTO_CREATE ) == true ) {
            if (parent.bindServiceAsUser(bindIntent, this, Context.BIND_AUTO_CREATE, android.os.UserHandle.OWNER) == true) {
                 Log.d(TAG, "FontServer Connecting..");
            }
            // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]
        }
    }

    /****************************************************************************
    ** Link - Font Configuration APIs - System app only.
    ****************************************************************************/
    public void updateFontServer() {
        if( service == null ) { return; }
        try {
            service.updateFontServer();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
    }

    public void selectDefaultTypeface( int fontIndex ) {
        if( service == null ) { return; }
        try {
            service.selectDefaultTypeface( fontIndex );
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
    }

    /****************************************************************************
    ** Link - Font Configuration APIs - Settings updateSettingsDB() only
    ****************************************************************************/
    public int setDefault() {
        if( service == null ) { return 0; }
        try {
            return service.setDefault();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return 0;
    }

    /****************************************************************************
    ** Link - Font Util APIs.
    ****************************************************************************/
    public int getDefaultTypefaceIndex() {
        if( service == null ) { return 0; }
        try {
            return service.getDefaultTypefaceIndex();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return 0;
    }

    public int getNumAllFonts() {
        if( service == null ) { return 0; }
        try {
            return service.getNumAllFonts();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return 0;
    }

    public int getNumEmbeddedFonts() {
        if( service == null ) { return 0; }
        try {
            return service.getNumEmbeddedFonts();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return 0;
    }

    public String getSummary() {
        if( service == null ) { return null; }
        try {
            return service.getSummary();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String[] getAllFontNames() {
        if( service == null ) { return null; }
        try {
            return service.getAllFontNames();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String[] getAllFontWebFaceNames() {
        if( service == null ) { return null; }
        try {
            return service.getAllFontWebFaceNames();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String[] getAllFontFullPath() {
        if( service == null ) { return null; }
        try {
            return service.getAllFontFullPath();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String getFontFullPath( int fontIndex ) {
        if( service == null ) { return null; }
        try {
            return service.getFontFullPath( fontIndex );
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String getDownloadFontSrcPath() {
        if( service == null ) { return null; }
        try {
            return service.getDownloadFontSrcPath();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public String getDownloadFontDstPath() {
        if( service == null ) { return null; }
        try {
            return service.getDownloadFontDstPath();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return null;
    }

    public int getSystemDefaultTypefaceIndex() {
        if( service == null ) { return 0; }
        try {
            return service.getSystemDefaultTypefaceIndex();
        } catch( RemoteException e ) {
            Log.e( TAG, "An error occured during the call." );
        }
        return 0;
    }

    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
    public String getDownloadFontAppName(int fontIndex) {
        if (service == null) {
            return null;
        }
        try {
            return service.getDownloadFontAppName(fontIndex);
        } catch (RemoteException e) {
            Log.e(TAG, "An error occured during the call.");
        }
        return null;
    }
    // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]
}