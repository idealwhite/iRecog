package com.wecombo.ml.irecog.util;

public class StringUtil {
    public static String[] processOcrResult(String rawResult) {
        String[] rawSplit = rawResult.split("\n");

        return  rawSplit;
    }
}
