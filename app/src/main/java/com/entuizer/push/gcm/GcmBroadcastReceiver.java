package com.entuizer.push.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Diego Acosta on 12/04/2016.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        ComponentName compName = new ComponentName(context.getPackageName(),
                GcmMessageHandler.class.getName());
        startWakefulService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }

}
