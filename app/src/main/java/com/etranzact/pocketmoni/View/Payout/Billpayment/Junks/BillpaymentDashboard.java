package com.etranzact.pocketmoni.View.Payout.Billpayment.Junks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.etranzact.pocketmoni.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Utils.Keys;

public class BillpaymentDashboard extends AppCompatActivity {

    GridLayout itemGridLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billpayment_dashboard);
        itemGridLayout = findViewById(R.id.gridviw_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        itemGridLayout.removeAllViews();
        for(String names : getCategories()){
            String catName = names.split("\\|")[0];
            String id = names.split("\\|")[1];
            if(names.length() > 11) catName = catName.substring(0, 11) + "..";
            createCard(catName, id, R.drawable.ic_champagne);
        }
    }

    private void createCard(String name, String id, int image){
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.LayoutParams.WRAP_CONTENT),
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.LayoutParams.WRAP_CONTENT)
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        }
        int paddingVal = (int) Keys.pixelToDpi(this,13f);
        params.setMargins(paddingVal,paddingVal,paddingVal,paddingVal);

        CardView cardView = new CardView(this);
        cardView.setLayoutParams(params);
        cardView.setCardElevation(Keys.pixelToDpi(this,3f));
        cardView.setRadius(Keys.pixelToDpi(this,10f));
        cardView.setId(Integer.parseInt(id));
        cardView.setOnClickListener(onCardClicked);

        LinearLayout linearLayout = new LinearLayout(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setClickable(false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        paddingVal = (int)Keys.pixelToDpi(this,20f);
        linearLayout.setPadding(0,paddingVal,0,paddingVal);

        ImageView imageView = new ImageView(this);
        ViewGroup.LayoutParams imageParam = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        imageView.setLayoutParams(imageParam);
        imageView.setClickable(false);
        //Set the image
        imageView.setImageResource(image);

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paddingVal = (int)Keys.pixelToDpi(this,20f);
        textParam.setMargins(0,paddingVal,0,0);
        textView.setLayoutParams(textParam);
        textView.setGravity(Gravity.CENTER);
        //Set the text
        textView.setText(name);

        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        cardView.addView(linearLayout);
        itemGridLayout.addView(cardView);
    }

    View.OnClickListener onCardClicked = (v)->{
        CardView cv = (CardView)v;
        TextView tv = (TextView) ((LinearLayout)cv.getChildAt(0)).getChildAt(1);
        switch (cv.getId()){
            case 2:
                cv.setCardBackgroundColor(getResources().getColor(R.color.pocketmoni_white_smoke));
                StartBillpaymentActivity.toFragment = new ElectricityFragment();
                startActivity(new Intent(BillpaymentDashboard.this, StartBillpaymentActivity.class));
                break;
            default:
                Toast.makeText(BillpaymentDashboard.this, "Payment Coming Soon", Toast.LENGTH_SHORT).show();
                break;
        }

    };

    public static List<String> getCategories(){
        //String data = SharedPref.get(context, "routeResp", "");
        String data = "{\"responseCode\":\"00\",\"responseMessage\":\"Successful\",\"data\":{\"routes\":[{\"min\":0,\"max\":3000,\"processor\":\"TMS\"},{\"min\":3001,\"max\":10000000,\"processor\":\"NIBSS\"}],\"version\":{\"latest_version\":\"0.0.6\",\"upgrade\":\"TRUE\",\"forceUpgrade\":\"TRUE\"},\"forceConfig\":true,\"active\":true,\"minimum_amount\":36.0,\"terminalConfiguration\":{\"agentLocation\":\"NO 15 ADO BALE EBUTE STREET, AJAH, LAGOS\",\"currencyCode\":\"566\",\"countryCode\":\"0566\",\"logo_url\":\"https://demo.etranzact.com/tms-service/logo\",\"terminalId\":\"2214I13M\",\"agentId\":\"1\",\"tmsBaseUrl\":\"https://demo.etranzact.com\",\"nibbsIp\":\"196.6.103.73\",\"nibbsPort\":\"5043\",\"nibbsEnv\":\"EPMS\",\"nibbsKey\":\"A050F63AFF366A4B0588D818D23C6C77\",\"posDataCode\":\"510101511344101\",\"terminalCapability\":\"E0F8C8\",\"contactlessCvmLimit\":999.0,\"contactlessTransLimit\":100000.0,\"nibbsMerchantId\":\"2214LA565107542\",\"processorMerchantLocation\":\"ETRANZACT INTERNATIO LA LANG\",\"agentName\":\"JOY NWANKWO\",\"minimumAmount\":36.0},\"billersCategories\":{\"status\":true,\"message\":\"Biller category retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"categoryName\":\"Cable TV\",\"description\":\"This is cable TV service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"billerName\":\"GOTV\",\"billerCode\":\"gotv\",\"description\":\"This is gotv biller\"},{\"id\":\"2\",\"billerName\":\"DSTV\",\"billerCode\":\"dstv\",\"description\":\"This is dstv biller\"},{\"id\":\"3\",\"billerName\":\"Startimes\",\"billerCode\":\"startimes\",\"description\":\"This is startimes biller\"}]}},{\"id\":\"2\",\"categoryName\":\"Electricity\",\"description\":\"This is electricity biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"12\",\"billerName\":\"PHCN Enugu Prepaid\",\"billerCode\":\"phcnenu\",\"description\":\"This is phcn enugu prepaid biller\"},{\"id\":\"13\",\"billerName\":\"PHCN Enugu Postpaid\",\"billerCode\":\"phcnppenu\",\"description\":\"This is phcn enugu postpaid biller\"},{\"id\":\"15\",\"billerName\":\"PHCN Eko Prepaid\",\"billerCode\":\"phcneko\",\"description\":\"This is phcn eko prepaid biller\"},{\"id\":\"16\",\"billerName\":\"PHCN Eko Postpaid\",\"billerCode\":\"phcnppeko\",\"description\":\"This is phcn eko postpaid biller\"},{\"id\":\"19\",\"billerName\":\"PHCN Jos Prepaid\",\"billerCode\":\"phcnjos\",\"description\":\"This is phcn jos prepaid biller\"},{\"id\":\"20\",\"billerName\":\"PHCN Jos Postpaid\",\"billerCode\":\"phcnppjos\",\"description\":\"This is phcn jos postpaid biller\"},{\"id\":\"21\",\"billerName\":\"PHCN Kano Prepaid\",\"billerCode\":\"phcnkan\",\"description\":\"This is phcn kano prepaid biller\"},{\"id\":\"22\",\"billerName\":\"PHCN Kaduna Postpaid\",\"billerCode\":\"phcnppkad\",\"description\":\"This is phcn kaduna postpaid biller\"},{\"id\":\"23\",\"billerName\":\"PHCN Abuja Prepaid\",\"billerCode\":\"phcnabj\",\"description\":\"This is phcn abuja prepaid biller\"},{\"id\":\"24\",\"billerName\":\"PHCN Abuja Postpaid\",\"billerCode\":\"phcnppabj\",\"description\":\"This is phcn abuja postpaid biller\"},{\"id\":\"25\",\"billerName\":\"PHCN Ibadan Prepaid\",\"billerCode\":\"phcnibd\",\"description\":\"This is phcn ibadan prepaid biller\"},{\"id\":\"26\",\"billerName\":\"PHCN Kano PostPaid\",\"billerCode\":\"phcnppkan\",\"description\":\"This is phcn kano postpaid biller\"},{\"id\":\"27\",\"billerName\":\"PHCN Kaduna PrePaid\",\"billerCode\":\"phcnkad\",\"description\":\"This is phcn kaduna prepaid biller\"},{\"id\":\"28\",\"billerName\":\"PHCN PortHarcourt Prepaid\",\"billerCode\":\"phcnphe\",\"description\":\"This is phcn portharcourt prepaid biller\"},{\"id\":\"29\",\"billerName\":\"PHCN PortHarcourt Postpaid\",\"billerCode\":\"phcnppphe\",\"description\":\"This is phcn portharcourt postpaid biller\"},{\"id\":\"30\",\"billerName\":\"PHCN Ibadan Postpaid\",\"billerCode\":\"phcnppibd\",\"description\":\"This is phcn ibadan postpaid biller\"}]}},{\"id\":\"3\",\"categoryName\":\"Govt Tax\",\"description\":\"This is Government tax biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"7\",\"billerName\":\"Rev Pay\",\"billerCode\":\"revpay\",\"description\":\"This is rev pay biller\"}]}},{\"id\":\"4\",\"categoryName\":\"Internet Services\",\"description\":\"This is internet services biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"5\",\"billerName\":\"Smile\",\"billerCode\":\"smile\",\"description\":\"This is smile biller\"},{\"id\":\"6\",\"billerName\":\"Swift\",\"billerCode\":\"swift\",\"description\":\"This is swift biller\"}]}},{\"id\":\"5\",\"categoryName\":\"Toll Service\",\"description\":\"This is toll service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"4\",\"billerName\":\"LCC\",\"billerCode\":\"lcc\",\"description\":\"This is lcc biller\"}]}}]},\"banks\":[{\"code\":\"070\",\"name\":\"Fidelity\"},{\"code\":\"076\",\"name\":\"Polaris\"}]},\"timeStamp\":\"2021-03-26T09:47:50.910+00:00\"}";
        List<String> categories = new ArrayList<>();
        try{
            JSONObject obj = new JSONObject(data);
            JSONArray categoryArray = obj.getJSONObject("data").getJSONObject("billersCategories").getJSONArray("result");
            for (int i = 0; i < categoryArray.length(); i++) {
                String id = categoryArray.getJSONObject(i).getString("id");
                String categoryName = categoryArray.getJSONObject(i).getString("categoryName");
                categories.add(categoryName.toUpperCase() + "|" + id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return categories;
    }
}