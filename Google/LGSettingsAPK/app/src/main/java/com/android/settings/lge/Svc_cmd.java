package com.android.settings.lge;

import android.content.Context;
import android.util.Log;

import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.NvManager;

public class Svc_cmd {
    public static final String LOG_TAG = "aboutphone , src # Svc_cmd";

    private static final int CMD_SCR_VERSION = 5008;

    public static String LgSvcCmd_getCmdValue(int LgSvcCmdId, Context context) {
        Log.i(LOG_TAG, "LgSvcCmd_getCmdValue:LgSvcCmdId:" + LgSvcCmdId);
        String ret_value = "";

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        LGContext serviceContext = new LGContext(context);
        NvManager nvManager = (NvManager)serviceContext.getLGSystemService(NvManager.getServiceName());

        if (LgSvcCmdId == 1000) {
            ret_value = nvManager.getServiceCommand(CMD_SCR_VERSION);
            Log.i(LOG_TAG, "LgSvcCmd_getCmdValue:ret_value11:" + ret_value);
        } else {
            ret_value = nvManager.getServiceCommand(LgSvcCmdId);
        }

        Log.i(LOG_TAG, "LgSvcCmd_getCmdValue:ret_value:" + ret_value);

        return ret_value;
    }

    public static boolean getQcrilMsgTunnelServiceStatus(Context context) {

        Log.i(LOG_TAG, "getQcrilMsgTunnelServiceStatus");
        boolean ret_value = false;

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        LGContext serviceContext = new LGContext(context);
        NvManager nvManager = (NvManager)serviceContext.getLGSystemService(NvManager.getServiceName());

        ret_value = nvManager.getServiceStatus();

        return ret_value;
    }
}