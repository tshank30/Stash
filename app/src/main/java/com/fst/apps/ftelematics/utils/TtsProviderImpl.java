package com.fst.apps.ftelematics.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TtsProviderImpl extends TtsProviderFactory implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    public void init(Context context) {
        if (tts == null) {
            tts = new TextToSpeech(context, this);
            tts.setSpeechRate(0.7f);
        }
    }

    @Override
    public void say(String sayThis) {
        tts.speak(sayThis, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {
        if (tts!=null && tts.isLanguageAvailable(Locale.ENGLISH) >= TextToSpeech.LANG_AVAILABLE) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }

    public void shutdown() {
        tts.shutdown();
    }
}
