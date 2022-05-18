package com.etranzact.pocketmoni.View.Payout.Billpayment.Junks;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.etranzact.pocketmoni.Dialogs.LoadingProgressDialog;
import com.etranzact.pocketmoni.R;

import java.util.List;

import Utils.AidClass;
import Utils.CardInfo;
import Utils.PosHandler;

public class ElectricityFragment extends Fragment implements PosHandler {

    private static final String HOME_KEY = "KEY";

    private String data;

    public ElectricityFragment() {
    }

    public static ElectricityFragment newInstance(String value) {
        ElectricityFragment fragment = new ElectricityFragment();
        Bundle args = new Bundle();
        args.putString(HOME_KEY, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = getArguments().getString(HOME_KEY);
        }
    }

    private static FragmentActivity activity;
    private static Context context;
    private static List<AidClass> aidList;
    public static ProgressBar progressBar;

    private static LoadingProgressDialog loadingDialog;
    EditText amountText, decoder_no_input;
    ImageView backBtn;
    TextView changeAcct, itemName, customerName, acctAddress, customerId;
    Button btnProceed, btnVerifyId;
    Spinner billerBox;
    LinearLayout details_entry_layout;
    CardView acct_verify_layout;
    //InsertCardFragment insertCardFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_billpayment_electricity, container, false);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                CardInfo.Disconnect();
                return false;
            }
        });

//        activity = getActivity();
//        context = getContext();
//        loadingDialog = new LoadingProgressDialog(getActivity());
//
//        itemName = v.findViewById(R.id.bill_item_id);
//        customerName = v.findViewById(R.id.bill_name_id);
//        decoder_no_input = v.findViewById(R.id.decoder_no_id);
//        changeAcct = v.findViewById(R.id.change_id);
//        btnVerifyId = v.findViewById(R.id.btn_verify_id);
//        billerBox = v.findViewById(R.id.biller_id);
//        customerId = v.findViewById(R.id.bill_customer_id);
//        acctAddress = v.findViewById(R.id.bill_address_id);
//        progressBar = v.findViewById(R.id.progress_bar);
//        btnProceed = v.findViewById(R.id.continue_id);
//        amountText = v.findViewById(R.id.amt_label);
//        details_entry_layout = v.findViewById(R.id.details_layout_id);
//        acct_verify_layout = v.findViewById(R.id.account_display_id);
//        amountText.addTextChangedListener(OnAmountTextChanged);
//        btnProceed.setOnClickListener(onContinueButtonClicked);
//        btnVerifyId.setOnClickListener(onVerifyBtnClicked);
//        backBtn = v.findViewById(R.id.back_btn_id);
//        backBtn.setOnClickListener((view)->{
//            getActivity().onBackPressed();
//        });
//
//        acct_verify_layout.setVisibility(View.GONE);
//        changeAcct.setOnClickListener((view)->{
//            acct_verify_layout.setVisibility(View.GONE);
//            details_entry_layout.setVisibility(View.VISIBLE);
//        });
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.dropdown_item, getBillerListArray());
//        billerBox.setAdapter(adapter);
//
//        Emv.initializeEmv(activity);
//
//        //Checks if there is any restriction for the agent
//        if(Keys.isAgentRestricted(getActivity())){
//            Disconnect();
//            return null;
//        }
        return v;
    }

//    private static String[] getBillerListArray(){
//        //get and set the banks list
//        List<String> billersList = new ArrayList<>();
//        for(BillpaymentModel bm : getBillers()){
//            billersList.add(bm.billerName);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            billersList.sort(String::compareTo);
//        }
//        billersList.add(0, "Select Provider");
//        String[] billersListArray = new String[billersList.size()];
//        for(int i=0; i<billersList.size(); i++){
//            billersListArray[i] = billersList.get(i);
//        }
//        return billersListArray;
//    }
//
//    TextWatcher OnAmountTextChanged = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//        }
//        private String current = "";
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            if(!s.toString().equals(current)){
//                amountText.removeTextChangedListener(this);
//                String cleanString = s.toString().replaceAll("[#,.]", "");
//                String formatted = formatAmount(cleanString);
//                current = formatted;
//                amountText.setText(formatted);
//                amountText.setSelection(formatted.length());
//                amountText.addTextChangedListener(this);
//            }
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//
//        }
//    };
//
//    public static BillpaymentModel getBillerDetails(String billerName){
//        for(BillpaymentModel bp : billers){
//            if(bp.billerName.equals(billerName)){
//                return bp;
//            }
//        }
//        return null;
//    }
//
//    View.OnClickListener onVerifyBtnClicked = (v)->{
//        String decoderNo = decoder_no_input.getText().toString();
//        String billName = billerBox.getSelectedItem().toString();
//        String billerId = getBillerDetails(billName).billerId;
//
//        if(billName.equals("Select Provider") || decoderNo.equals("")){
//            Toast.makeText(context, "Please fill all the necessary fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        loadingDialog.show();
//        loadingDialog.setProgressLabel("Verifying account", true);
//
//        Handler handler = new Handler(Looper.getMainLooper(), (msg)->{
//            String response = msg.getData().getString("msg");
//            String resp = Keys.parseJson(response, "responseCode");
//            if(resp.isEmpty()){
//                Toast.makeText(context, "Network error, Try again later", Toast.LENGTH_SHORT).show();
//            }
//
//            if(resp.equals("00")){
//                details_entry_layout.setVisibility(View.GONE);
//                acct_verify_layout.setVisibility(View.VISIBLE);
//
//                StartBillpaymentActivity.clientRef = Keys.parseJson(response, "clientRef");
//                StartBillpaymentActivity.paymentRef = Keys.parseJson(response, "paymentRef");
//                StartBillpaymentActivity.customerId = Keys.parseJson(response, "customerId");
//                StartBillpaymentActivity.billerId = Keys.parseJson(response, "billId");
//                StartBillpaymentActivity.mobileNo = Keys.parseJson(response, "mobile");
//                StartBillpaymentActivity.billerName = Keys.parseJson(response, "billName");
//                //"customerName":"NAME:AKAWU MADAKI|DISTRICT:KAD|ADDRESS:18 1B IRRI STR DIRKANIA  KADUNA KADUNA|MERCHANT:73QABZ-TG5|ARREARS:0.0"
//                String[] parameters  = Keys.parseJson(response, "customerName").split("\\|");
//
//                StartBillpaymentActivity.customerName = parameters[0];
//                itemName.setText(billName);
//                acctAddress.setText(parameters[2]);
//                customerName.setText(StartBillpaymentActivity.customerName);
//                customerId.setText(StartBillpaymentActivity.customerId);
//
//                billerBox.setSelection(0);
//                decoder_no_input.setText("");
//            }else {
//                Toast.makeText(activity, "Unable to validate the meter number at this time.", Toast.LENGTH_SHORT).show();
//            }
//
//            loadingDialog.dismiss();
//            return true;
//        });
//        new Thread(()->{
//            String resp = Middleware.billpaymentValidation(Emv.serialNumber,billerId,decoderNo,Emv.terminalId);
//            Message msg = new Message();
//            Bundle bundle = new Bundle();
//            bundle.putString("msg", resp);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
//        }).start();
//
//    };
//
//    View.OnClickListener onContinueButtonClicked = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String newAmt = amountText.getText().toString();
//            if(newAmt.equals("") || newAmt.equals("0.00")){
//                Toast.makeText(activity, "Invalid Amount", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if(StartBillpaymentActivity.billerId.equals("")){
//                Toast.makeText(context, "Click on verify to verify the meter number", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if(Keys.isMinimumAmount(getActivity(), newAmt)){
//                Toast.makeText(activity, "Amount cannot be less than minimum amount", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String amtF = String.valueOf(Double.parseDouble(newAmt.replace(",", "")) * 100);
//            DecimalFormat df = new DecimalFormat("###.#");
//            String amt = "9F02|" + Keys.padLeft(df.format(Double.parseDouble(amtF)),12,'0');
//            Emv.AmountAuthorized = amt;
//
//            //Set the transaction route to use for transaction
//            TransRoute.setTransactionRoute(getContext(), amt.split("\\|")[1]);
//
//            //Enable account selection
//            //new AccountSelectionDialog(getActivity()).show();
//
//            DoInsertCardLogic();
//            return;
//        }
//    };
//
//
//    private String formatAmount(String amtString){
//        Long amt = Long.parseLong(amtString);
//        double amtD = (double)amt/100;
//        return String.format("%,.2f",amtD);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        CardInfo.CancelCardSearch();
//        super.onPause();
//    }
//
//    @Override
//    public void onDetach() {
//        Log.d("Result", "amount fragment removed");
//        super.onDetach();
//        CardInfo.StopTransaction(activity);
//    }
//
//    private void DoInsertCardLogic(){
//        //insertCardLabel.setVisibility(View.VISIBLE);
//        //linearLayout.setVisibility(View.GONE);
//        loadingDialog.show();
//        loadingDialog.setProgressLabel("Waiting for card", false);
//        CardInfo.initialize(getActivity());
//    }
//
//    public static void runPSECommand(CardReadMode cardInfoEntity){
//        //Check to see if the card is contact or contactless
//        if(cardInfoEntity == CardReadMode.CONTACT){
//            aidList = CardInfo.PseSupport(context);
//        }else if(cardInfoEntity == CardReadMode.CONTACTLESS){
//            Contactless.initialize(activity);
//            return;
//        }else if(cardInfoEntity == CardReadMode.FALLBACK_SWIPE){
//            Disconnect();
//            return;
//        }
//
//        //If you get here, it means you have successfully done application selection
//        if((aidList == null) || (CardInfo.isCardRemoved == true)){
//            Disconnect();
//            return;
//        }else if(aidList.size() == 0){
//            Disconnect();
//            return;
//        }
//
//        if (aidList.get(0).Aid.equals("")) {
//            Emv.startCVMProcessing(activity);
//        } else if(aidList.size() > 0) {
//            Collections.sort(aidList, AidClass.SortByAid);
//            //AppSelectionFragment appSelectionFragment = AppSelectionFragment.newInstance(aidList);
//            //activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_layout,appSelectionFragment).addToBackStack(null).commit();
//        }
//        loadingDialog.dismiss();
//    }
//
//    private static List<BillpaymentModel> billers = new ArrayList<>();
//    public static List<BillpaymentModel> getBillers(){
//        String data = SharedPref.get(context, "routeResp", "");
//        //String data = "{\"responseCode\":\"00\",\"responseMessage\":\"Successful\",\"data\":{\"routes\":[{\"min\":0,\"max\":3000,\"processor\":\"TMS\"},{\"min\":3001,\"max\":10000000,\"processor\":\"NIBSS\"}],\"version\":{\"latest_version\":\"0.0.6\",\"upgrade\":\"TRUE\",\"forceUpgrade\":\"TRUE\"},\"forceConfig\":true,\"active\":true,\"minimum_amount\":36.0,\"terminalConfiguration\":{\"agentLocation\":\"NO 15 ADO BALE EBUTE STREET, AJAH, LAGOS\",\"currencyCode\":\"566\",\"countryCode\":\"0566\",\"logo_url\":\"https://demo.etranzact.com/tms-service/logo\",\"terminalId\":\"2214I13M\",\"agentId\":\"1\",\"tmsBaseUrl\":\"https://demo.etranzact.com\",\"nibbsIp\":\"196.6.103.73\",\"nibbsPort\":\"5043\",\"nibbsEnv\":\"EPMS\",\"nibbsKey\":\"A050F63AFF366A4B0588D818D23C6C77\",\"posDataCode\":\"510101511344101\",\"terminalCapability\":\"E0F8C8\",\"contactlessCvmLimit\":999.0,\"contactlessTransLimit\":100000.0,\"nibbsMerchantId\":\"2214LA565107542\",\"processorMerchantLocation\":\"ETRANZACT INTERNATIO LA LANG\",\"agentName\":\"JOY NWANKWO\",\"minimumAmount\":36.0},\"billersCategories\":{\"status\":true,\"message\":\"Biller category retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"categoryName\":\"Cable TV\",\"description\":\"This is cable TV service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"1\",\"billerName\":\"GOTV\",\"billerCode\":\"gotv\",\"description\":\"This is gotv biller\"},{\"id\":\"2\",\"billerName\":\"DSTV\",\"billerCode\":\"dstv\",\"description\":\"This is dstv biller\"},{\"id\":\"3\",\"billerName\":\"Startimes\",\"billerCode\":\"startimes\",\"description\":\"This is startimes biller\"}]}},{\"id\":\"2\",\"categoryName\":\"Electricity\",\"description\":\"This is electricity biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"12\",\"billerName\":\"PHCN Enugu Prepaid\",\"billerCode\":\"phcnenu\",\"description\":\"This is phcn enugu prepaid biller\"},{\"id\":\"13\",\"billerName\":\"PHCN Enugu Postpaid\",\"billerCode\":\"phcnppenu\",\"description\":\"This is phcn enugu postpaid biller\"},{\"id\":\"15\",\"billerName\":\"PHCN Eko Prepaid\",\"billerCode\":\"phcneko\",\"description\":\"This is phcn eko prepaid biller\"},{\"id\":\"16\",\"billerName\":\"PHCN Eko Postpaid\",\"billerCode\":\"phcnppeko\",\"description\":\"This is phcn eko postpaid biller\"},{\"id\":\"19\",\"billerName\":\"PHCN Jos Prepaid\",\"billerCode\":\"phcnjos\",\"description\":\"This is phcn jos prepaid biller\"},{\"id\":\"20\",\"billerName\":\"PHCN Jos Postpaid\",\"billerCode\":\"phcnppjos\",\"description\":\"This is phcn jos postpaid biller\"},{\"id\":\"21\",\"billerName\":\"PHCN Kano Prepaid\",\"billerCode\":\"phcnkan\",\"description\":\"This is phcn kano prepaid biller\"},{\"id\":\"22\",\"billerName\":\"PHCN Kaduna Postpaid\",\"billerCode\":\"phcnppkad\",\"description\":\"This is phcn kaduna postpaid biller\"},{\"id\":\"23\",\"billerName\":\"PHCN Abuja Prepaid\",\"billerCode\":\"phcnabj\",\"description\":\"This is phcn abuja prepaid biller\"},{\"id\":\"24\",\"billerName\":\"PHCN Abuja Postpaid\",\"billerCode\":\"phcnppabj\",\"description\":\"This is phcn abuja postpaid biller\"},{\"id\":\"25\",\"billerName\":\"PHCN Ibadan Prepaid\",\"billerCode\":\"phcnibd\",\"description\":\"This is phcn ibadan prepaid biller\"},{\"id\":\"26\",\"billerName\":\"PHCN Kano PostPaid\",\"billerCode\":\"phcnppkan\",\"description\":\"This is phcn kano postpaid biller\"},{\"id\":\"27\",\"billerName\":\"PHCN Kaduna PrePaid\",\"billerCode\":\"phcnkad\",\"description\":\"This is phcn kaduna prepaid biller\"},{\"id\":\"28\",\"billerName\":\"PHCN PortHarcourt Prepaid\",\"billerCode\":\"phcnphe\",\"description\":\"This is phcn portharcourt prepaid biller\"},{\"id\":\"29\",\"billerName\":\"PHCN PortHarcourt Postpaid\",\"billerCode\":\"phcnppphe\",\"description\":\"This is phcn portharcourt postpaid biller\"},{\"id\":\"30\",\"billerName\":\"PHCN Ibadan Postpaid\",\"billerCode\":\"phcnppibd\",\"description\":\"This is phcn ibadan postpaid biller\"}]}},{\"id\":\"3\",\"categoryName\":\"Govt Tax\",\"description\":\"This is Government tax biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"7\",\"billerName\":\"Rev Pay\",\"billerCode\":\"revpay\",\"description\":\"This is rev pay biller\"}]}},{\"id\":\"4\",\"categoryName\":\"Internet Services\",\"description\":\"This is internet services biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"5\",\"billerName\":\"Smile\",\"billerCode\":\"smile\",\"description\":\"This is smile biller\"},{\"id\":\"6\",\"billerName\":\"Swift\",\"billerCode\":\"swift\",\"description\":\"This is swift biller\"}]}},{\"id\":\"5\",\"categoryName\":\"Toll Service\",\"description\":\"This is toll service biller category\",\"billers\":{\"status\":true,\"message\":\"Biller retrieved sucessfully\",\"result\":[{\"id\":\"4\",\"billerName\":\"LCC\",\"billerCode\":\"lcc\",\"description\":\"This is lcc biller\"}]}}]},\"banks\":[{\"code\":\"070\",\"name\":\"Fidelity\"},{\"code\":\"076\",\"name\":\"Polaris\"}]},\"timeStamp\":\"2021-03-26T09:47:50.910+00:00\"}";
//        billers.clear();
//        try{
//            JSONObject obj = new JSONObject(data);
//            JSONArray categoryArray = obj.getJSONObject("data").getJSONObject("billersCategories").getJSONArray("result");
//            for (int i = 0; i < categoryArray.length(); i++) {
//                String categoryId = categoryArray.getJSONObject(i).getString("id");
//                if(categoryId.equals("2")){
//                    JSONArray billArray = categoryArray.getJSONObject(i).getJSONObject("billers").getJSONArray("result");
//                    for(int u=0; u<billArray.length(); u++){
//                        String billerName = billArray.getJSONObject(u).getString("billerName");
//                        String biller_id = billArray.getJSONObject(u).getString("id");
//                        String biller_code = billArray.getJSONObject(u).getString("billerCode");
//                        billers.add(new BillpaymentModel(billerName.toUpperCase(), biller_code, biller_id));
//                    }
//                    break;
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return billers;
//    }
//
//    public static void Disconnect(){
//        CardInfo.StopTransaction(activity);
//    }
//
//    @Override
//    public void onDetectICCard(CardReadMode cardType) {
//        loadingDialog.setProgressLabel("Reading Card", false);
//        //This calls app selection.
//        Runnable runnable = ()->{
//            runPSECommand(cardType);
//        };
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(runnable, 10);
//    }
//
//    @Override
//    public void onDetectContactlessCard(CardReadMode cardType) {
//        //progressBar.setVisibility(View.VISIBLE);
//        //This calls app selection.
//        Runnable runnable = ()-> runPSECommand(cardType);
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(runnable, 10);
//    }
//
//    @Override
//    public void onCardTimeount() {
//        activity.runOnUiThread(()->{
//            Toast.makeText(activity, "CARD READ TIMEOUT", Toast.LENGTH_LONG).show();
//        });
//        Disconnect();
//    }
//
//    @Override
//    public void onCardRemoved() {
//        //Nothing here
//    }
}