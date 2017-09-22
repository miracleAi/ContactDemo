package com.example.zhulinping.contactdemo.contactdisplay.contactdata;

import android.content.Context;

/**
 * Created by sm on 17-2-28.
 */

public class ARFirstCharacterPImplI extends ILocalFirstCharacterPImpl {

    // the Definite Article
    private static final String AR_THE_DEFINITE_ARTICLE = "ال";
    // special char
    private static final String AR_SPECIAL_CHAR_ASCII_0622 = "آ";
    private static final String AR_SPECIAL_CHAR_ASCII_0625 = "إ";
    private static final String AR_SPECIAL_CHAR_ASCII_0627 = "ا";

    public ARFirstCharacterPImplI(Context mContext) {
        super(mContext);
    }

    @Override
    public int getBefInterval(String s) {
        if (s.startsWith(AR_THE_DEFINITE_ARTICLE)){
            return 2;
        }else {
            return super.getBefInterval(s);
        }
    }

    @Override
    public int getMidInterval(char c) {
        if (c == 0x200f){  // 阿拉伯语有一个从右到左的字符转换标志，去掉它防止归类时出现问题
            return 1;
        }else {
            return super.getMidInterval(c);
        }
    }

    @Override
    public char getTransCharacter(char c) {
        String c1 = String.valueOf(c);
        if (c1.equals(AR_SPECIAL_CHAR_ASCII_0622) || c1.equals(AR_SPECIAL_CHAR_ASCII_0625)
                || c1.equals(AR_SPECIAL_CHAR_ASCII_0627)){
            return 'أ';
        }else {
            return super.getTransCharacter(c);
        }

    }
}
