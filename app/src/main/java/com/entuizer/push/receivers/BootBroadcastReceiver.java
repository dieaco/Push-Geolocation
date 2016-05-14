package com.entuizer.push.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.entuizer.push.services.LocationService;

/**
 * Created by Diego Acosta on 10/05/2016.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    //static final String ACTION2 = "android.intent.action.PACKAGE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED” start Service
        Log.e("BROADCASTRECEIVER", "ACTION: "+intent.getAction());
        if (intent.getAction().equals(ACTION)) {
            //Service
            Intent serviceIntent = new Intent(context, LocationService.class);
            context.startService(serviceIntent);
        }
    }
}
