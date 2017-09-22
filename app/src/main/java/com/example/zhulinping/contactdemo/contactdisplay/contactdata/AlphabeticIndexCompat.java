package com.example.zhulinping.contactdemo.contactdisplay.contactdata;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这是 Launcher3 对语言字母排序的适配，可以替代我们自己的适配【两个选择一个就好了】
 */
public class AlphabeticIndexCompat {
    private static final String TAG = "AlphabeticIndexCompat";

    private static final String MID_DOT = "#";
    private final BaseIndex mBaseIndex;
    private final String mDefaultMiscLabel;

    // special char
    private static final String AR_SPECIAL_CHAR_ASCII_0622 = "آ";
    private static final String AR_SPECIAL_CHAR_ASCII_0625 = "إ";
    private static final String AR_SPECIAL_CHAR_ASCII_0627 = "ا";

    private boolean createException = false;
    private Context mContext;

    public AlphabeticIndexCompat(Context context) {
        BaseIndex index = null;
        mContext = context;
        final Locale userPreferLang = getUserPreferLang(context);

        /*launcher 3 的实现在Android N 上有问题, Android N上当用户选择的语言列表中同时存在简体中文和繁体中文的时候，
        * 会出现一些中文首字母（非A-Z），这里对Android N ，用户首选语言为中文的，使用HanZiToPinyin来兼容，和桌面的策略保持一致*/
        /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && userPreferLang != null && Locale.CHINESE.getLanguage().equals(userPreferLang.getLanguage())) {
            index = new DefaultChinaIndex();
        }*/

        if (index == null) {
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    index = new AlphabeticIndexVN(context);
                }
            } catch (Exception e) {
                createException = true;
            }
        }

        if (index == null) {
            try {
                index = new AlphabeticIndexV16(context);
            } catch (Exception e) {
                createException = true;
            }
        }

        /*if (index == null) {
            if (userPreferLang != null && Locale.CHINESE.getLanguage().equals(userPreferLang.getLanguage())) {
                index = new DefaultChinaIndex();
            }
        }*/

        // mBaseIndex = index == null ? new BaseIndex() : index;
        // 使用我们默认的Index，替换系统的
        mBaseIndex = index == null ? new DefaultIndex() : index;

        if (context.getResources().getConfiguration().locale
                .getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            // Japanese character 他 ("misc")
            mDefaultMiscLabel = "\u4ed6";
            // TODO(winsonc, omakoto): We need to handle Japanese sections better, especially the kanji
        } else {
            // Dot
            mDefaultMiscLabel = MID_DOT;
        }
    }

    public boolean checkException(){
        return createException;
    }

    private static Locale getUserPreferLang(Context context) {
        final Configuration config = context.getResources().getConfiguration();
        if (config == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//            final LocaleList locales = config.getLocales();
//            return locales.isEmpty() ? null : locales.get(0);
            return config.locale;
        } else {
            return config.locale;
        }
    }

    private static final Pattern sTrimPattern =
            Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

    /**
     * Computes the section name for an given string {@param s}.
     */
    public String computeSectionName(CharSequence cs) {
        String s = trim(cs);
        int bucketIndex = mBaseIndex.getBucketIndex(s);
        String sectionName = mBaseIndex.getBucketLabel(bucketIndex);
        //String language = LocaleUtils.getSavedLocale().getLanguage();
        String language = mContext.getResources().getConfiguration().locale.getLanguage();
        if (language.equals("ar") && sectionName.trim().isEmpty() && s.length() > 0){ // 阿拉伯语
            char c = s.charAt(0);
            if (c == 0x200f){  // 阿拉伯语有一个从右到左的字符转换标志，去掉它防止归类时出现问题
                c = s.charAt(1);
            }
            String firstC = String.valueOf(c);
            if (firstC.equals(AR_SPECIAL_CHAR_ASCII_0622) || firstC.equals(AR_SPECIAL_CHAR_ASCII_0625)
                    || firstC.equals(AR_SPECIAL_CHAR_ASCII_0627)){
                return "أ";
            }
        }

        if (trim(sectionName).isEmpty() && s.length() > 0) {
            int c = s.codePointAt(0);
            boolean startsWithDigit = Character.isDigit(c);
            if (startsWithDigit) {
                // Digit section
                return "#";
            } else {
                boolean startsWithLetter = Character.isLetter(c);
                if (startsWithLetter) {
                    return mDefaultMiscLabel;
                } else {
                    // In languages where these differ, this ensures that we differentiate
                    // between the misc section in the native language and a misc section
                    // for everything else.
                    return MID_DOT;
                }
            }
        }
        if (sectionName.equals("…")){
            return MID_DOT;
        }
        return sectionName;
    }

    public int getBucketIndex(String str){
        return mBaseIndex.getBucketIndex(str);
    }

    /**
     * Base class to support Alphabetic indexing if not supported by the framework.
     * TODO(winsonc): disable for non-english locales
     */
    private static class BaseIndex {

        private static final String BUCKETS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"\u2219";
        private static final int UNKNOWN_BUCKET_INDEX = BUCKETS.length() - 1;

        /**
         * Returns the index of the bucket in which the given string should appear.
         */
        protected int getBucketIndex(String s) {
            if (s.isEmpty()) {
                return UNKNOWN_BUCKET_INDEX;
            }
            int index = BUCKETS.indexOf(s.substring(0, 1).toUpperCase());
            if (index != -1) {
                return index;
            }
            return UNKNOWN_BUCKET_INDEX;
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            return BUCKETS.substring(index, index + 1);
        }
    }

    /**
     * Reflected libcore.icu.AlphabeticIndex implementation, falls back to the base
     * alphabetic index.
     */
    private static class AlphabeticIndexV16 extends BaseIndex {

        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;
        private Method mGetBucketLabelMethod;

        public AlphabeticIndexV16(Context context) throws Exception {
            // 当用户选择了语言之后AllApps索引可以及时替换
            //Locale curLocale = LocaleUtils.getSavedLocale();
            Locale curLocale = context.getResources().getConfiguration().locale;
            Class clazz = Class.forName("libcore.icu.AlphabeticIndex");
            mGetBucketIndexMethod = clazz.getDeclaredMethod("getBucketIndex", String.class);
            mGetBucketLabelMethod = clazz.getDeclaredMethod("getBucketLabel", int.class);
            mAlphabeticIndex = clazz.getConstructor(Locale.class).newInstance(curLocale);

            if (!curLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                clazz.getDeclaredMethod("addLabels", Locale.class)
                        .invoke(mAlphabeticIndex, Locale.ENGLISH);
            }


        }

        /**
         * Returns the index of the bucket in which {@param s} should appear.
         * Function is synchronized because underlying routine walks an iterator
         * whose state is maintained inside the index object.
         */
        protected int getBucketIndex(String s) {
            try {
                return (Integer) mGetBucketIndexMethod.invoke(mAlphabeticIndex, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketIndex(s);
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            try {
                return (String) mGetBucketLabelMethod.invoke(mAlphabeticIndex, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketLabel(index);
        }
    }

    /**
     * Reflected android.icu.text.AlphabeticIndex implementation, falls back to the base
     * alphabetic index.
     */
    private static class AlphabeticIndexVN extends BaseIndex {

        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;

        private Method mGetBucketMethod;
        private Method mGetLabelMethod;

        public AlphabeticIndexVN(Context context) throws Exception {
            // TODO: Replace this with locale list once available.
            Object locales = Configuration.class.getDeclaredMethod("getLocales").invoke(
                    context.getResources().getConfiguration());
            int localeCount = (Integer) locales.getClass().getDeclaredMethod("size").invoke(locales);
            Method localeGetter = locales.getClass().getDeclaredMethod("get", int.class);
            Locale primaryLocale = localeCount == 0 ? Locale.ENGLISH :
                    (Locale) localeGetter.invoke(locales, 0);
            Class clazz = Class.forName("android.icu.text.AlphabeticIndex");
            mAlphabeticIndex = clazz.getConstructor(Locale.class).newInstance(primaryLocale);

            Method addLocales = clazz.getDeclaredMethod("addLabels", Locale[].class);
            //将所有系统支持语言添加到索引中
            for (int i = 1; i < localeCount; i++) {
                Locale l = (Locale) localeGetter.invoke(locales, i);
                addLocales.invoke(mAlphabeticIndex, new Object[]{ new Locale[] {l}});
            }
            addLocales.invoke(mAlphabeticIndex, new Object[]{new Locale[]{Locale.ENGLISH}});

            mAlphabeticIndex = mAlphabeticIndex.getClass()
                    .getDeclaredMethod("buildImmutableIndex")
                    .invoke(mAlphabeticIndex);

            mGetBucketIndexMethod = mAlphabeticIndex.getClass().getDeclaredMethod(
                    "getBucketIndex", CharSequence.class);
            mGetBucketMethod = mAlphabeticIndex.getClass().getDeclaredMethod("getBucket", int.class);
            mGetLabelMethod = mGetBucketMethod.getReturnType().getDeclaredMethod("getLabel");
        }

        /**
         * Returns the index of the bucket in which {@param s} should appear.
         */
        protected int getBucketIndex(String s) {
            try {
                return (Integer) mGetBucketIndexMethod.invoke(mAlphabeticIndex, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketIndex(s);
        }

        /**
         * Returns the label for the bucket at the given index
         */
        protected String getBucketLabel(int index) {
            try {
                return (String) mGetLabelMethod.invoke(
                        mGetBucketMethod.invoke(mAlphabeticIndex, index));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketLabel(index);
        }
    }

    /**
     * 默认的字母表，用于系统内置不支持多语言字母索引的情况
     * 只有当用户的首选语言是中国的时候才使用这个
     */
    private static final class DefaultChinaIndex extends BaseIndex {

        private static final String BUCKETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"\u2219";
        private static final int UNKNOWN_BUCKET_INDEX = BUCKETS.length() - 1;

        /**
         * Returns the index of the bucket in which the given string should appear.
         */
        protected int getBucketIndex(String s) {
            if (s.isEmpty()) {
                return UNKNOWN_BUCKET_INDEX;
            }
            int index = BUCKETS.indexOf(s.substring(0, 1).toUpperCase());
            if (index != -1) {
                return index;
            }

           /* final ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(s);
            if (tokens != null && !tokens.isEmpty()) {
                final String pinyin = tokens.get(0).target;

                if (!TextUtils.isEmpty(pinyin)) {

                    index = BUCKETS.indexOf(pinyin.substring(0, 1).toUpperCase());
                    if (index != -1) {
                        return index;
                    }
                }
            }*/
           String pinyin = ContactDataUtils.getFirstLetter(s);
            if (!TextUtils.isEmpty(pinyin)) {

                index = BUCKETS.indexOf(pinyin.substring(0, 1).toUpperCase());
                if (index != -1) {
                    return index;
                }
            }
            return UNKNOWN_BUCKET_INDEX;
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            return BUCKETS.substring(index, index + 1);
        }
    }

    /**
     * 和Launcher3 唯一的不同是，我们不支持数字形式的字母索引
     */
    private static final class DefaultIndex extends BaseIndex {
        private static final String BUCKETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"\u2219";
        private static final int UNKNOWN_BUCKET_INDEX = BUCKETS.length() - 1;

        /**
         * Returns the index of the bucket in which the given string should appear.
         */
        protected int getBucketIndex(String s) {
            if (s.isEmpty()) {
                return UNKNOWN_BUCKET_INDEX;
            }
            int index = BUCKETS.indexOf(s.substring(0, 1).toUpperCase());
            if (index != -1) {
                return index;
            }
            return UNKNOWN_BUCKET_INDEX;
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            return BUCKETS.substring(index, index + 1);
        }
    }
}
