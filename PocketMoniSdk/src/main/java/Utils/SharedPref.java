package Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public synchronized static void set(Context context, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(Emv.PREFERENCEKEY, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public synchronized static String get(Context context, String key, String defVal){
        SharedPreferences sp = context.getSharedPreferences(Emv.PREFERENCEKEY, context.MODE_PRIVATE);
        return sp.getString(key, defVal);
    }
}
