package com.acer.paroyalty;

public class Constant {
//    static final String DEV_HOST =        "https://paroyalty-dtp-dev.acervcon.com/paroyaltyApi/";
//    static final String PRODUCTION_HOST = "https://paroyalty-dtp.acervcon.com/paroyaltyApi/";
    static final String DEV_HOST = "http://ec2-18-141-235-83.ap-southeast-1.compute.amazonaws.com/paroyaltyApi/";
    static final String PRODUCTION_HOST = "http://ec2-18-141-235-83.ap-southeast-1.compute.amazonaws.com/paroyaltyApi/";
    static final String PATH_VERIFY_LICENSE = "api/V1/license/verify";
    static final String PATH_GET_ADDATA = "api/V1/ad/getaddata";
    static final String PATH_TRACK_LOG = "api/V1/ad/tracklog";
    static final String SERVER_USERNAME = "aiSage2020";
    static final String SERVER_PASSWORD = "aiSage2020";
    static final String INTENT_EXTRA_KEY_APPID = "appid";
    static final String INTENT_EXTRA_KEY_ISDEVE = "isDev";
    public static final String INTENT_EXTRA_KEY_AD_DATA = "adData";
    public static final String INTENT_ACTION_NAME = "com.ptv.searchlight.addata";
}
