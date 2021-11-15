package Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

import com.sdk.pocketmonisdk.Model.NotificationModel;
import com.sdk.pocketmonisdk.Model.RecyclerModel;
import com.sdk.pocketmonisdk.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class TransDB {
    private static final String DB_NAME = "MY_POS";
    private static final String DB_TABLE = "POS";
    private static final String DB_TABLE2 = "NOTIFY";
    private static final String DB_TIME = "TIME";
    private static final String DB_TYPE = "TYPE";
    private static final String DB_DATA = "DATA";
    private static final String DB_AMT = "AMT";
    private static final String DB_RESP = "RESP";
    private static final String DB_CARD_NO = "CARD_NO";
    private static final int DB_VERSION = 1;

    private DBData dData;
    private SQLiteDatabase sqDb;
    private Context c;

    public TransDB(Context context) {
        c = context;
    }

    public void open() {
        dData = new DBData(c);
        sqDb = dData.getWritableDatabase();
    }

    public void insert(String time, TransType option, String amt, String resp, String cardNo, String data) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DB_TIME, time);
            cv.put(DB_TYPE, option.toString());
            cv.put(DB_AMT, amt);
            cv.put(DB_RESP, resp);
            cv.put(DB_CARD_NO, cardNo);
            cv.put(DB_DATA, data);
            sqDb.insert(DB_TABLE, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDb(String time, TransType option, String respCode, String data) {
        ContentValues cv = new ContentValues();
        cv.put(DB_RESP, respCode);
        cv.put(DB_DATA, data);
        sqDb.update(DB_TABLE, cv, DB_TIME + " = '" + time + "' AND " + DB_TYPE + " = '" + option.toString() + "'", null);
    }

    //For notification
    public void insert(String time, String data) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DB_TIME, time);
            cv.put(DB_DATA, data);
            sqDb.insert(DB_TABLE2, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //For notification
    public void deleteData(String transTime) {
        sqDb.delete(DB_TABLE2, DB_TIME + " = '" + transTime + "'", null);
    }

    //For notification
    public void deleteAllRecords() {
        sqDb.delete(DB_TABLE2, null, null);
    }

    //For notification
    public List<NotificationModel> getNotificationRecords() {
        String[] columns = {DB_TIME,DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE2, columns,null, null, null, null, DB_TIME);
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iData = cursor.getColumnIndex(DB_DATA);
        List<NotificationModel> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String time = cursor.getString(iTime);
            String data = cursor.getString(iData);
            NotificationModel db = new NotificationModel(time, data);
            dbList.add(db);
        }
        return dbList;
    }

    public void deleteData(String transType, String transTime) {
        if (transType.isEmpty()) return;
        if (transType.equals("ALL") && !transTime.equals("ALL")) {
            sqDb.delete(DB_TABLE, DB_TIME + " LIKE '%" + transTime + "%'", null);
        } else if (transTime.equals("ALL") && !transType.equals("ALL")) {
            sqDb.delete(DB_TABLE, DB_TYPE + " = '" + transType + "'", null);
        } else if (transTime.equals("ALL") && transType.equals("ALL")) {
            sqDb.delete(DB_TABLE, null, null);
        } else {
            sqDb.delete(DB_TABLE, DB_TYPE + " = '" + transType + "' AND " + DB_TIME + " LIKE '%" + transTime + "%'", null);
        }
    }

    public List<RecyclerModel> getAllData(int limit) {
        String[] columns = {DB_TIME, DB_TYPE, DB_AMT, DB_RESP, DB_CARD_NO, DB_DATA};
        Cursor cursor = null;
        if(limit != 0){
            //Set a limit that will show
            cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC LIMIT " + limit);
        }else{
            cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC");
        }
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iType = cursor.getColumnIndex(DB_TYPE);
        int iAmt = cursor.getColumnIndex(DB_AMT);
        int iResp = cursor.getColumnIndex(DB_RESP);
        int iCardNo = cursor.getColumnIndex(DB_CARD_NO);
        int iData = cursor.getColumnIndex(DB_DATA);
        List<RecyclerModel> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String time = cursor.getString(iTime);
            String amt = cursor.getString(iAmt);
            String resp = cursor.getString(iResp);
            String card_no = cursor.getString(iCardNo);
            String trans_type = cursor.getString(iType);
            String data = cursor.getString(iData);
            RecyclerModel db;
            if (resp.equals("00")) {
                db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
            } else if (resp.isEmpty()) {
                db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }else{
                db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }
            dbList.add(db);
        }
        return dbList;
    }

    public String getEODFullData(String transType, String transDate) {
        String[] columns = {DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE, columns, DB_TIME + " = '" + transDate + "' AND " + DB_TYPE + " = '" + transType + "'", null, null, null, DB_TIME + " DESC");
        int iData = cursor.getColumnIndex(DB_DATA);
        String data = "";
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            data = cursor.getString(iData);
        }
        return data;
    }

    public List<RecyclerModel> geTransBetweenDate(String startDate, String endDate) {
        String[] columns = {DB_TIME, DB_TYPE, DB_AMT, DB_RESP, DB_CARD_NO, DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE, columns, DB_TIME + " BETWEEN '" +startDate+ " 00:00:00' AND '" + endDate + " 23:59:00'", null, null, null, DB_TIME + " DESC");
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iType = cursor.getColumnIndex(DB_TYPE);
        int iAmt = cursor.getColumnIndex(DB_AMT);
        int iResp = cursor.getColumnIndex(DB_RESP);
        int iCardNo = cursor.getColumnIndex(DB_CARD_NO);
        int iData = cursor.getColumnIndex(DB_DATA);
        List<RecyclerModel> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String trans_type = cursor.getString(iType);
            String time = cursor.getString(iTime);
            String amt = cursor.getString(iAmt);
            String resp = cursor.getString(iResp);
            String card_no = cursor.getString(iCardNo);
            String data = cursor.getString(iData);
            RecyclerModel db;
            if (resp.equals("00")) {
                db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
            } else if (resp.isEmpty()) {
                db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }else{
                db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }
            dbList.add(db);
        }
        return dbList;
    }

    public List<RecyclerModel> searchByDate(String date) {
        String[] columns = {DB_TIME, DB_TYPE, DB_AMT, DB_RESP, DB_CARD_NO, DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE, columns, DB_TIME+" LIKE '%" + date + "%'", null, null, null, DB_TIME + " DESC");
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iType = cursor.getColumnIndex(DB_TYPE);
        int iAmt = cursor.getColumnIndex(DB_AMT);
        int iResp = cursor.getColumnIndex(DB_RESP);
        int iCardNo = cursor.getColumnIndex(DB_CARD_NO);
        int iData = cursor.getColumnIndex(DB_DATA);
        List<RecyclerModel> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String trans_type = cursor.getString(iType);
            String time = cursor.getString(iTime);
            String amt = cursor.getString(iAmt);
            String resp = cursor.getString(iResp);
            String card_no = cursor.getString(iCardNo);
            String data = cursor.getString(iData);
            RecyclerModel db;
            if (resp.equals("00")) {
                db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
            } else if (resp.isEmpty()) {
                db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }else{
                db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
            }
            dbList.add(db);
        }
        return dbList;
    }

    public List<RecyclerModel> getData(String transType, String transDate) {
        String[] columns = {DB_TIME, DB_TYPE, DB_AMT, DB_RESP, DB_CARD_NO, DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC");
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iType = cursor.getColumnIndex(DB_TYPE);
        int iAmt = cursor.getColumnIndex(DB_AMT);
        int iResp = cursor.getColumnIndex(DB_RESP);
        int iCardNo = cursor.getColumnIndex(DB_CARD_NO);
        int iData = cursor.getColumnIndex(DB_DATA);
        List<RecyclerModel> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String trans_type = cursor.getString(iType);
            String time = cursor.getString(iTime);
            if ((transType.equals(trans_type)) && (transDate.equals(time.substring(0, 10)))) {
                String amt = cursor.getString(iAmt);
                String resp = cursor.getString(iResp);
                String card_no = cursor.getString(iCardNo);
                String data = cursor.getString(iData);
                RecyclerModel db;
                if (resp.equals("00")) {
                    db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
                } else if (resp.isEmpty()) {
                    db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }else{
                    db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }
                dbList.add(db);
            } else if ((transType.equals("ALL")) && (transDate.equals(time.substring(0, 10)))) {
                String amt = cursor.getString(iAmt);
                String resp = cursor.getString(iResp);
                String card_no = cursor.getString(iCardNo);
                String data = cursor.getString(iData);
                RecyclerModel db;
                if (resp.equals("00")) {
                    db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
                } else if (resp.isEmpty()) {
                    db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }else{
                    db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }
                dbList.add(db);
            } else if ((transType.equals(trans_type)) && (transDate.equals("ALL"))) {
                String amt = cursor.getString(iAmt);
                String resp = cursor.getString(iResp);
                String card_no = cursor.getString(iCardNo);
                String data = cursor.getString(iData);
                RecyclerModel db;
                if (resp.equals("00")) {
                    db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
                } else if (resp.isEmpty()) {
                    db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }else{
                    db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }
                dbList.add(db);
            } else if ((transType.equals("ALL")) && (transDate.equals("ALL"))) {
                String amt = cursor.getString(iAmt);
                String resp = cursor.getString(iResp);
                String card_no = cursor.getString(iCardNo);
                String data = cursor.getString(iData);
                RecyclerModel db;
                if (resp.equals("00")) {
                    db = new RecyclerModel(R.drawable.circle_pass,R.color.success_green, resp, card_no, amt, time, trans_type, data);
                } else if (resp.isEmpty()) {
                    db = new RecyclerModel(R.drawable.circle_pending, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }else{
                    db = new RecyclerModel(R.drawable.circle_fail, R.color.error_red, resp, card_no, amt, time, trans_type, data);
                }
                dbList.add(db);
            }
        }
        return dbList;
    }

    public RecyclerModel getSpecificDataOnly(String transType, String transDate) {
        String[] columns = {DB_TIME, DB_TYPE, DB_AMT, DB_RESP, DB_CARD_NO, DB_DATA};
        Cursor cursor = sqDb.query(DB_TABLE, columns,DB_TIME + " = '" + transDate + "' AND " + DB_TYPE + " = '" + transType + "'", null, null, null, DB_TIME + " DESC");
        int iTime = cursor.getColumnIndex(DB_TIME);
        int iType = cursor.getColumnIndex(DB_TYPE);
        int iAmt = cursor.getColumnIndex(DB_AMT);
        int iResp = cursor.getColumnIndex(DB_RESP);
        int iCardNo = cursor.getColumnIndex(DB_CARD_NO);
        int iData = cursor.getColumnIndex(DB_DATA);
        RecyclerModel recyclerModel = new RecyclerModel();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            recyclerModel.setTransType(cursor.getString(iType));
            recyclerModel.setTransTime(cursor.getString(iTime));
            recyclerModel.setTransAmt(cursor.getString(iAmt));
            recyclerModel.setRespCode(cursor.getString(iResp));
            recyclerModel.setCardNo(cursor.getString(iCardNo));
            recyclerModel.setData(cursor.getString(iData));
        }
        return recyclerModel;
    }

    public ArrayAdapter<String> getTransTypes() {
        String[] columns = {DB_TYPE};
        Cursor cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC");
        //Cursor cursor = sqDb.rawQuery("SELECT DISTINCT " + DB_TYPE + " FROM " + DB_TABLE + " ORDER BY " + DB_TYPE + " DESC",null);
        int iType = cursor.getColumnIndex(DB_TYPE);
        List<String> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String trans_type = cursor.getString(iType);
            dbList.add(trans_type);
        }
        if (dbList.size() > 0) dbList.add("ALL");
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(dbList);
        dbList.clear();
        dbList.addAll(linkedHashSet);
        return new ArrayAdapter<String>(c, android.R.layout.simple_spinner_dropdown_item, dbList);
    }

    public ArrayAdapter<String> getTransDates() {
        String[] columns = {DB_TIME};
        Cursor cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC");
        //Cursor cursor = sqDb.rawQuery("SELECT DISTINCT " + DB_TIME + " FROM " + DB_TABLE + " ORDER BY " + DB_TIME + " DESC",null);
        int iTime = cursor.getColumnIndex(DB_TIME);
        List<String> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String time = cursor.getString(iTime).substring(0, 10);
            dbList.add(time);
        }
        if (dbList.size() > 0) dbList.add("ALL");
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(dbList);
        dbList.clear();
        dbList.addAll(linkedHashSet);
        return new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, dbList);
    }

    public ArrayAdapter<String> getTransHistoryDate() {
        String[] columns = {DB_TIME};
        Cursor cursor = sqDb.query(DB_TABLE, columns, null, null, null, null, DB_TIME + " DESC");
        int iTime = cursor.getColumnIndex(DB_TIME);
        List<String> dbList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String time = cursor.getString(iTime).substring(0, 10);
            dbList.add(time);
        }
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(dbList);
        dbList.clear();
        dbList.addAll(linkedHashSet);
        return new ArrayAdapter<String>(c, R.layout.spinner_custom_list, dbList);
    }

    public void close() {
        sqDb.close();
    }

    private class DBData extends SQLiteOpenHelper {
        public DBData(@Nullable Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                String sql = "CREATE TABLE " + DB_TABLE + " (" +
                        DB_TIME + " TEXT PRIMARY KEY NOT NULL, " +
                        DB_TYPE + " TEXT NOT NULL, " +
                        DB_AMT + " TEXT NOT NULL, " +
                        DB_RESP + " TEXT NOT NULL, " +
                        DB_CARD_NO + " TEXT NOT NULL, " +
                        DB_DATA + " TEXT NOT NULL);";
                db.execSQL(sql);

                String sql2 = "CREATE TABLE " + DB_TABLE2 + " (" +
                        DB_TIME + " TEXT PRIMARY KEY NOT NULL, " +
                        DB_DATA + " TEXT NOT NULL);";
                db.execSQL(sql2);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "DROP TABLE IF EXISTS " + DB_TABLE;
            String sql2 = "DROP TABLE IF EXISTS " + DB_TABLE2;
            db.execSQL(sql);
            db.execSQL(sql2);
            onCreate(db);
        }
    }
}

