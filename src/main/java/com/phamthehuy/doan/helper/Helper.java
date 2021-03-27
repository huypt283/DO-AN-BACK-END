package com.phamthehuy.doan.helper;

public class Helper {
    public static String getHostUrl(String url, String substring){
        int index=url.indexOf(substring);
        String hostUrl=url.substring(0, index);
        return hostUrl;
    }
}
