package com.example.mapArduino;

import android.app.Activity;

public class themer {
    public static int theme = 0;

    //listing all themes to use with the appropriate map theme
    public static Integer[] themes = new Integer[]{R.style.AppTheme, R.style.SilverAppTheme, R.style.AppTheme, R.style.DarkAppTheme, R.style.DarkAppTheme, R.style.DarkAppTheme};
    public static Integer[] mapThemes = new Integer[]{R.string.style_json_clean, R.string.style_json_silver, R.string.style_json_retro, R.string.style_json_aubergine, R.string.style_json_night, R.string.style_json_dark};
    public static int themeRange = themes.length;
    public static int spooky = 0;

    public static void changeTheme(Activity activity) {
        activity.recreate(); //reload the activity to allow for the theme changes to occur completely. will lose all data stored in it.
    }

    public static void changeToTheme(Activity activity, int theme) {
        themer.theme = theme;
        activity.recreate();
    }
    public static int getTheme() {
        return themes[theme];
    }

    public static int getMapTheme() {
        return mapThemes[theme];
    }

    public static void nextTheme(Activity activity) {
        theme = (theme + 1) % themeRange;
        changeTheme(activity);
    }

    public static void setTheme(int i) {
        if (i >= themeRange) throw new IndexOutOfBoundsException();
        theme = i;
    }
}
