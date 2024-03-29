package Utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class TransRoute {

    public static boolean bypassRoute = false;
    private static int failedCounts = 1;
    private static int successCounts = 1;
//    public static void setRouteRespCode(Context c, String resp){
//        String respLocal = "00";
//        if (resp.equals("91")) {
//            if(successCounts > 9) {
//                Emv.environment = Emv.environment.equals("NIBSS") ? "TMS" : "NIBSS";
//                respLocal = "91";
//                successCounts = 1;
//                failedCounts--;
//            }else if (failedCounts == 3){
//                Emv.environment = Emv.environment.equals("NIBSS") ? "TMS" : "NIBSS";
//                bypassRoute = true;
//            }
//            else if (failedCounts == 6) Emv.environment = Emv.environment.equals("NIBSS") ? "TMS" : "NIBSS";
//            else if(failedCounts > 5) failedCounts = 1;
//            failedCounts++;
//        }
//        if (!resp.equals("91") && (failedCounts > 1)) {
//            if((successCounts > 10) && respLocal.equals("00")) {
//                failedCounts = 1;
//                successCounts = 0;
//                bypassRoute = false;
//            }else if(!bypassRoute){
//                failedCounts = 1;
//                successCounts = 0;
//            }
//            else if(successCounts > 9) Emv.environment = Emv.environment.equals("NIBSS") ? "TMS" : "NIBSS";
//            successCounts++;
//        }
//        SharedPref.set(c, "environment", Emv.environment);
//    }

    public static void setTransactionRoute(Context c, String amount){
        try{
            if(bypassRoute) return;

            amount = amount.replace(",", "");
            String resp = SharedPref.get(c,"routeResp", "");

            JSONObject jsonObject = new JSONObject(resp);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("routes");
            for(int i =0; i<jsonArray.length();i++){
                String processorVal = jsonArray.getJSONObject(i).getString("processor");
                String minVal = jsonArray.getJSONObject(i).getString("min");
                String maxVal = jsonArray.getJSONObject(i).getString("max");

                double min = Double.parseDouble(minVal);
                double max = Double.parseDouble(maxVal);
                double amt = Double.parseDouble(amount)/100;
                if(amt >= min && amt <= max){
                    Emv.environment = processorVal;
                    SharedPref.set(c, "environment", Emv.environment);
                    Log.d("Result", "Environment selected: " + Emv.environment);
                    return;
                }
            }
            Emv.environment = SharedPref.get(c, "environment", "NIBSS");
        }catch (Exception ex){
            ex.printStackTrace();
            Emv.environment = SharedPref.get(c, "environment", "NIBSS");
        }

    }

    public static void resetRoute() {
        failedCounts = 1;
        successCounts = 0;
        bypassRoute = false;
    }
}
