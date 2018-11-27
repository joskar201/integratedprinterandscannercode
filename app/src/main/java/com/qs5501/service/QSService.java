package com.qs5501.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by Administrator on 2018-04-20.
 */

public class QSService extends Service {


    private Binder iQSpda=new IQSPDAImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return iQSpda;
    }

}
