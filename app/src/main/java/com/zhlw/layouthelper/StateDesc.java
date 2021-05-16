package com.zhlw.layouthelper;

/**
 * author zlw  2021-05-16
 * 当前功能记录
 */
public class StateDesc {

    private StateDesc(){

    }

    public static final int STATE_DEFAULT = 0x0001;
    public static final int STATE_DYHELPEROPEN = 0x0010;
    public static final int STATE_DYAUTOVEDIO = 0x0100;
    public static final int STATE_SCREENHELP = 0x1000;

    public static int CUR_STATE = STATE_DEFAULT;

    public static boolean isDyHelperOpen(){
        return CUR_STATE == STATE_DYHELPEROPEN;
    }

    public static boolean isDyAutoVedioOpen(){
        return CUR_STATE == STATE_DYAUTOVEDIO;
    }

    public static boolean isScreenHelperOpen(){
        return CUR_STATE == STATE_SCREENHELP;
    }


}
