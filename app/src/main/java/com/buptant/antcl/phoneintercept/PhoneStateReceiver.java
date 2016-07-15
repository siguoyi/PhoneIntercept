package com.buptant.antcl.phoneintercept;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.content.SharedPreferences.Editor;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by antcl on 2016/4/18.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = PhoneStateReceiver.class.getSimpleName();
    private static final String[] PHONES_PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
    private static final int PHONES_NUMBER_INDEX = 1;
    private TelephonyManager telephonyManager;
    private HashSet<String> set;
    private ContentResolver resolver;
    private Cursor mCursor;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive");
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        switch (telephonyManager.getCallState()){
            case TelephonyManager.CALL_STATE_RINGING:

                String telNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String date = sdf.format(new Date());
                Log.d(TAG,"tel: " + telNumber + "  date: " + date);
                set = getContacts(context);
                if(!set.contains(telNumber)){
                    SharedPreferences sharedPreferences = context.getSharedPreferences("black_list",Context.MODE_PRIVATE);
                    Map map = sharedPreferences.getAll();
                    if(map.containsKey(telNumber) || map.containsValue(date)){
                        return;
                    }else{
                        if(telNumber != null){
                            Editor editor = sharedPreferences.edit();
                            editor.putString(telNumber, date);
                            editor.commit();
                            endCall();
                            Intent i = new Intent(MainActivity.ACTION_UPDATE);
                            context.sendBroadcast(i);
                        }
                    }
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                break;
        }
    }

    private void endCall(){
        Class<TelephonyManager> c = TelephonyManager.class;
        try
        {
            Method getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony iTelephony = null;
            Log.e(TAG, "End call.");
            iTelephony = (ITelephony) getITelephonyMethod.invoke(telephonyManager, (Object[]) null);
            iTelephony.endCall();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Fail to answer ring call.", e);
        }
    }

    private HashSet<String> getContacts(Context context){
        HashSet<String> tempSet = new HashSet<>();
        resolver = context.getContentResolver();
        mCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION,null,null,null);
        if(mCursor != null){
            while(mCursor.moveToNext()){
                String phoneNumber = mCursor.getString(PHONES_NUMBER_INDEX);
                if(TextUtils.isEmpty(phoneNumber)){
                    continue;
                }
                if(!tempSet.contains(phoneNumber)){
                    tempSet.add(phoneNumber);
                }
            }
            mCursor.close();
        }
        return tempSet;
    };
}
