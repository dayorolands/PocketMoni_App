package com.etranzact.pocketmoni.Model;

import android.content.Context;
import com.etranzact.pocketmoni.TransEnvironment.TMS;
import java.util.Locale;
import Utils.Emv;
import Utils.Keys;
import Utils.TransType;

public class CardModel {

    public static String getTransactionData(Context context){
        if(Emv.transactionType == TransType.CASHOUT){
            return getCashoutTransactionData(context);
        }else if(Emv.transactionType == TransType.TRANSFER){
            return getTransferTransactionData(context);
        } else if(Emv.transactionType == TransType.ELECTRICITY){
            return getElectricityTransactionData(context);
        }
        return "";
    }

    private static String getCashoutTransactionData(Context context){
        //Concatenate rrn with serialNo.
        String resultRRN = (Emv.environment.equals("TMS")) ? Keys.genKimonoRRN(context, Emv.transactionStan) : Keys.genNibssRRN(context,Emv.transactionStan);
        return Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
                Keys.hexStringToASCII(Emv.getEmv("5F20")) + "|" + //2 CARD HOLDER NAME
                Emv.getMaskedPan() + "|" +  //3 MASKED PAN
                Emv.transactionType.toString() + "|" + //4 TRANSACTION TYPE
                Emv.responseMessage + "|" + //5 RESPONSE MESSAGE
                String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(Emv.getMinorAmount())/100) + "|" +  //6 TRANSACTION MINOR AMOUNT
                Emv.responseCode + "|" + //7 RESPONSE CODE
                resultRRN + "|" + //8 RRN
                Emv.transactionStan + "|" + //9 STAN
                Emv.getEmv("95") + "|" + //10 TVR
                Emv.getEmv("9B").toUpperCase() + "|" + //11 TSI
                Keys.hexStringToASCII(Emv.getEmv("50")); //12 CARD TYPE
    }


    private static String getTransferTransactionData(Context context){
        TransferModel model = new TransferModel();
        //Concatenate rrn with serialNo.
        String resultRRN = (Emv.environment.equals("TMS")) ? Keys.genKimonoRRN(context, Emv.transactionStan) : Keys.genNibssRRN(context,Emv.transactionStan);
        return Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
                Keys.hexStringToASCII(Emv.getEmv("5F20")) + "|" + //2 CARD HOLDER NAME
                Emv.getMaskedPan() + "|" +  //3 MASKED PAN
                Emv.transactionType.toString() + "|" + //4 TRANSACTION TYPE
                Emv.responseMessage + "|" + //5 RESPONSE MESSAGE
                String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(Emv.getMinorAmount())/100) + "|" +  //6 TRANSACTION MINOR AMOUNT
                Emv.responseCode + "|" + //7 RESPONSE CODE
                resultRRN + "|" + //8 RRN
                Emv.transactionStan + "|" + //9 STAN
                Emv.getEmv("95") + "|" + //10 TVR
                Emv.getEmv("9B").toUpperCase() + "|" + //11 TSI
                Keys.hexStringToASCII(Emv.getEmv("50")) + "|" + //12 CARD TYPE
                model.getSendersName() + "|" + //13 Transfer Name
                model.getAcctNo() + "|" + //14 Transfer Account No
                model.getTransferRef(); //15 Transfer Ref
    }

    private static String getElectricityTransactionData(Context context){
        ElectricityModel model = new ElectricityModel();
        //Concatenate rrn with serialNo.
        String resultRRN = (Emv.environment.equals("TMS")) ? Keys.genKimonoRRN(context, Emv.transactionStan) : Keys.genNibssRRN(context,Emv.transactionStan);
        return Emv.getTransactionDate() + "|" + //0 TRANSACTION DATE
                Emv.getTransactionTime() + "|" + //1 TRANSACTION TIME
                Keys.hexStringToASCII(Emv.getEmv("5F20")) + "|" + //2 CARD HOLDER NAME
                Emv.getMaskedPan() + "|" +  //3 MASKED PAN
                Emv.transactionType.toString() + "|" + //4 TRANSACTION TYPE
                Emv.responseMessage + "|" + //5 RESPONSE MESSAGE
                String.format(Locale.getDefault(),"%,.2f",Double.parseDouble(Emv.getMinorAmount())/100) + "|" +  //6 TRANSACTION MINOR AMOUNT
                Emv.responseCode + "|" + //7 RESPONSE CODE
                resultRRN + "|" + //8 RRN
                Emv.transactionStan + "|" + //9 STAN
                Emv.getEmv("95") + "|" + //10 TVR
                Emv.getEmv("9B").toUpperCase() + "|" + //11 TSI
                Keys.hexStringToASCII(Emv.getEmv("50")) + "|" + //12 CARD TYP
                model.getCustomerName() + "|" + //13 Electricity Customer Name
                model.getCustomerId() + "|" + //14 Electricity Customer ID
                model.getBillerName() + "|" + //15 Electricity Biller Name
                model.getPaymentRef() + "|" + //16 Electricity Payment Ref
                model.getDescription() + "|" + //17 Electricity Description
                model.getToken(); // 18 Recharge Token

    }

    public static String getRequeryPayload(){
        String data = "<transactionRequeryRequest>\n" +
                "    <applicationType>gTransfer</applicationType>\n" +
                "    <originalTransStan>"+Emv.getTransactionStan()+"</originalTransStan>\n" +
                "    <originalMinorAmount>"+Emv.getMinorAmount()+"</originalMinorAmount>\n" +
                "    <terminalInformation>\n" +
                "        <terminalId>"+Emv.terminalId+"</terminalId>\n" +
                "        <merchantId>"+Emv.merchantId+"</merchantId>\n" +
                "        <transmissionDate>"+ TMS.originalTransDate+"</transmissionDate>\n" +
                "    </terminalInformation>\n" +
                "</transactionRequeryRequest>";
        return data;
    }
}
