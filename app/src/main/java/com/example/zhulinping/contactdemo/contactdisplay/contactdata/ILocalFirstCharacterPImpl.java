package com.example.zhulinping.contactdemo.contactdisplay.contactdata;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by sm on 17-2-28.
 */

public abstract class ILocalFirstCharacterPImpl {

    public HashMap<Character, Character> leadingCharMap;
    public Context mContext;
    public final String TAG = "FirstCharIndex";
    private static final String MID_DOT = "•";
    public boolean noSupportAlpha = true;

    public ILocalFirstCharacterPImpl(Context mContext) {
        this.mContext = mContext;
        if (leadingCharMap == null){
            leadingCharMap = new HashMap<Character, Character>();
            //initLeadingChars();
        }
    }

   /* private void initLeadingChars() {
        String[] leadingChars = mContext.getResources().getStringArray(R.array.allapps_indexing_array);
        for (String leading : leadingChars) {
             final int LENGTH = leading.length();
             char value = leading.charAt(0);
             for (int j = 0; j < LENGTH; j++) {
                   char key = leading.charAt(j);
                   if (!leadingCharMap.containsKey(key)) {
                        leadingCharMap.put(key, value);
                   }
             }
        }

    }*/

    public Character getFirstCharacter(CharSequence s) {
        String noBlankStr = s.toString().trim();  // 去掉空格
        int index = getBefInterval(noBlankStr);
        char c = noBlankStr.charAt(index);
        int midInterval = getMidInterval(c);
        index+=midInterval;
        c = getTransCharacter(noBlankStr.charAt(index));
        Character leading = leadingCharMap.get(c);
        if (leading != null){
            return leading;
        }else {
            if (noSupportAlpha){
                return '#';
            }else {
                if (c >= 0x30 && c <= 0x39){  // ０－９ 返回# 其他返回 .
                    return '#';
                }else {
                    return MID_DOT.charAt(0);
                }
            }
        }
    }

    /**
     * 在执行for循环之前检查字符串，看是否有需要跳过的字符，比如阿拉伯语的定冠词（２个词）必须专门创建一个方法
     * @param s   待检测的字符串
     * @return     需要跳过的字符串数
     */
    public int getBefInterval(String s){
        return 0;
    }

    /**
     * 检查获取到的字符串是否为无意思的字符串，比如“　”　‘0xc2’ '0xa0' 只会针对一个无意义字符的情景
     * @param c 待检测字符
     * @return  为无意义字符是返回１，默认０
     */
    public int getMidInterval(char c){
        return 0;
    }

    /**
     * 对语言的特殊处理规则，比如HanziToPinyin
     * @param c
     * @return
     */
    public char getTransCharacter(char c){
        return c;
    }


}
