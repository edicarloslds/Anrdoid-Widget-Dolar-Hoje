package br.com.power.dolarhoje;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsUtils {

    public static void setInteger(Context context, String chave, int valor){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(chave,valor);
        editor.apply();
    }

    public static int getInteger(Context context, String chave){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getInt(chave,0);
    }

    public static void setString(Context context, String chave, String valor){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(chave, valor);
        editor.apply();
    }

    public static String getString(Context context, String chave){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getString(chave, "");
    }

}
