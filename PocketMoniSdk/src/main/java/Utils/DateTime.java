package Utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {
    public static Date CustomDate(int yyyy, int MM, int dd, int HH, int mm, int ss){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date dt = sdf.parse(yyyy +"-"+ MM +"-"+ dd +"-"+ HH +"-"+mm +"-"+ ss);
            return dt;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static Date CustomDate(int yyyy, int MM, int dd){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dt = sdf.parse(String.valueOf(yyyy)+"-"+ String.valueOf(MM)+"-"+String.valueOf(dd));
            return dt;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static int Compare(Date dt1, Date dt2){
        return dt1.compareTo(dt2);
    }

    public static class Now {

        public static int Day(){
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd");
            return Integer.parseInt(sdf.format(date));
        }

        public static int Month(){
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            return Integer.parseInt(sdf.format(date));
        }

        public static int Year(){
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            return Integer.parseInt(sdf.format(date));
        }

        public static String ToString(String DateFormat){
            if(DateFormat.equals("yMMdd")){
                return String.valueOf(Year()).substring(2) + Keys.padLeft(String.valueOf(Month()), 2,'0') + Keys.padLeft(String.valueOf(Day()), 2, '0');
            }
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
            return sdf.format(date);
        }

        public static String getTimeStamp(){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            return String.valueOf(timestamp.getTime());
        }
    }
}
