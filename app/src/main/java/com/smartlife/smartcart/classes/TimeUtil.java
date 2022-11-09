package com.smartlife.smartcart.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lzy on 2017/4/19 0019.
 */

public class TimeUtil {

    public static final SimpleDateFormat DEFAULT_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //2015-01-28

    public static final SimpleDateFormat EEMC_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    public static final SimpleDateFormat SERVER_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static String getDateEEMC() {
        Date date = new Date();
        return EEMC_FORMAT.format(date);
    }

    public static String getDateEEMC(String date) {
        try {
            return EEMC_FORMAT.format(SERVER_FORMAT.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    public static String currentTime() {
        Date date = new Date();
        return DEFAULT_FORMAT.format(date);
    }
}
