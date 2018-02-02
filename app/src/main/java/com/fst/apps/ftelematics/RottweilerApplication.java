package com.fst.apps.ftelematics;

import android.app.Application;

import com.fst.apps.ftelematics.utils.TtsProviderFactory;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.HashMap;

/**
 * Created by welcome on 1/12/2016.
 */
public class RottweilerApplication extends Application {

    private HashMap<String, String> keyValueStore=new HashMap<String, String>();
    private TtsProviderFactory tts;
    @Override
    public void onCreate() {
        super.onCreate();
        Iconify.with(new FontAwesomeModule());
        setCommandsMap();
        /*tts=TtsProviderFactory.getInstance();
        tts.init(this);*/
    }

    public void set(String key,String value){
        this.keyValueStore.put(key, value);
    }

    public String get(String key){
        return this.keyValueStore.get(key);
    }

    public HashMap<String, String> getKeyValueStore(){
        return this.keyValueStore;
    }

    public void setCommandsMap(){
        this.keyValueStore.put("1_ignition_start", "supplyelec123456");
        this.keyValueStore.put("1_ignition_stop", "stopelec123456");
        this.keyValueStore.put("2_ignition_start", "RELAY,0#");
        this.keyValueStore.put("2_ignition_stop", "RELAY,1#");
        this.keyValueStore.put("27_ignition_start", "ARM<6906>");
        this.keyValueStore.put("27_ignition_stop", "DISARM<6906>");
    }
}
