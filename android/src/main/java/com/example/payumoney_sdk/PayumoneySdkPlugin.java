package com.example.payumoney_sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



import com.payu.base.models.ErrorResponse;
import com.payu.base.models.OrderDetails;
import com.payu.base.models.PayUBillingCycle;
import com.payu.base.models.PayUPaymentParams;
import com.payu.base.models.PayUSIParams;
import com.payu.base.models.PaymentMode;
import com.payu.base.models.PaymentType;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;



import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import io.flutter.plugin.common.PluginRegistry;
import static android.app.Activity.RESULT_OK;





/** PayumoneySdkPlugin */
public class PayumoneySdkPlugin implements FlutterPlugin, MethodCallHandler,PluginRegistry.ActivityResultListener,ActivityAware {
  private MethodChannel channel;
  private MethodChannel.Result mainResult;
  private Activity activity;
  private  Context context;
  private ActivityPluginBinding pluginBinding;
  private static final String TAG = "PayuMoney Flutter Plugin";
  private boolean isProduction = false;
  PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
  PayUPaymentParams payUPaymentParams =null;
  PayUSIParams siDetails=null;





  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "payumoney_sdk");
    channel.setMethodCallHandler(this);



  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.mainResult=result;
     if(call.method.equals("buildPaymentParams")){

       buildPaymentParams(call);
     }else if(call.method.equals("setPayuSiParam")){
       setPayuSiParam(call);
    }else{
      result.notImplemented();
    }
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data){


    Log.e(TAG,"request code "+requestCode+" result code "+resultCode);

    return  false;
  }





  private  void setPayuSiParam(MethodCall call){
   siDetails= new PayUSIParams.Builder()
            .setIsFreeTrial((boolean)call.argument("isFreeTrial")) //set it to true for free trial. Default value is false
            .setBillingAmount((String)call.argument("billingAmt"))
            .setBillingCycle(PayUBillingCycle.valueOf((String)call.argument("billingCycle")))
            .setBillingInterval(Integer.parseInt(((String)call.argument("setBillingInterval"))))
            .setPaymentStartDate((String)call.argument("paymentStartDate"))
            .setPaymentEndDate((String)call.argument("paymentEndDate"))
            .setRemarks((String)call.argument("remarks"))
            .build();
  }


  private void buildPaymentParams(MethodCall call) {
      PayUCheckoutProConfig payUCheckoutProConfig = new PayUCheckoutProConfig ();
      payUCheckoutProConfig.setMerchantName((String)call.argument("merchantName"));
      HashMap<String, Object> additionalParams = new HashMap<>(); 
      additionalParams.put(PayUCheckoutProConstants.CP_UDF1, "udf1"); 
      additionalParams.put(PayUCheckoutProConstants.CP_UDF2, "udf2"); 
      additionalParams.put(PayUCheckoutProConstants.CP_UDF3, "udf3"); 
      additionalParams.put(PayUCheckoutProConstants.CP_UDF4, "udf4"); 
      additionalParams.put(PayUCheckoutProConstants.CP_UDF5, "udf5"); 
     
        // ArrayList<PaymentMode> checkoutOrderList = new ArrayList<>();
        // checkoutOrderList.add(new PaymentMode(PaymentType.UPI));
        // checkoutOrderList.add(new PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY));
        // checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE));
        // checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM));
        // checkoutOrderList.add(new PaymentMode(PaymentType.CARD));
        // payUCheckoutProConfig.setPaymentModesOrder(checkoutOrderList);
        ArrayList<HashMap<String,String>> enforceList = new ArrayList();
        HashMap<String,String> map = new HashMap<>();
        map.put(PayUCheckoutProConstants.CP_PAYMENT_TYPE, PaymentType.UPI.name());
        // map.put(PayUCheckoutProConstants.CP_CARD_TYPE, CardType.CC.name());
        enforceList.add(map);
        payUCheckoutProConfig.setEnforcePaymentList(enforceList);







      builder.setAmount((String) call.argument("amount"))
            .setTransactionId((String) call.argument("transactionId"))
            .setPhone((String) call.argument("phone"))
            .setProductInfo((String) call.argument("productInfo"))
            .setFirstName((String) call.argument("firstName"))
            .setEmail((String) call.argument("email"))
            .setSurl((String)call.argument("successURL"))
            .setFurl((String)call.argument("failureURL"))
            .setIsProduction((boolean)call.argument("isProduction"))
            .setKey((String)call.argument("merchantKey"))
            .setUserCredential((String)call.argument("userCredentials"))
            .setAdditionalParams(additionalParams); 

    



    if(siDetails!=null){
      builder.setPayUSIParams(siDetails);
    }



    try {
      this.payUPaymentParams = builder.build();

      startPayment(this.payUPaymentParams,payUCheckoutProConfig,(String)call.argument("salt"));



    } catch (Exception e) {
      mainResult.error("ERROR", e.getMessage(), null);
      Log.d(TAG, "Error : " + e.toString());
    }
  }


  private void startPayment(PayUPaymentParams payUPaymentParams,PayUCheckoutProConfig payUCheckoutProConfig,final  String salt){
   PayUCheckoutPro.open(
            activity,
            payUPaymentParams,
            payUCheckoutProConfig,
            new PayUCheckoutProListener() {

              @Override
              public void onPaymentSuccess(Object response) {
                //Cast response object to HashMap
                HashMap<String,Object> result = (HashMap<String, Object>) response;
                String payuResponse = (String)result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                result.put("status", "success");
                result.put("message","Payment success");
                mainResult.success(result);

              }

              @Override
              public void onPaymentFailure(Object response) {
                //Cast response object to HashMap
                HashMap<String,Object> result = (HashMap<String, Object>) response;
                String payuResponse = (String)result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE);
                String merchantResponse = (String) result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE);
                result.put("status", "failed");
                result.put("message","Payment failed");
                mainResult.success(result);
              }

              @Override
              public void onPaymentCancel(boolean isTxnInitiated) {

                  HashMap<String,Object> result =new HashMap<String, Object>();
                result.put("status", "failed");
                result.put("message","Payment canceled");
                mainResult.success(result);
              }

              @Override
              public void onError(ErrorResponse errorResponse) {
                String errorMessage = errorResponse.getErrorMessage();
                HashMap<String,Object> result =new HashMap<String, Object>();
                result.put("status", "failed");
                result.put("message","Something went wrong");
                mainResult.success(result);
              }

              @Override
              public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {
                //For setting webview properties, if any. Check Customized Integration section for more details on this
              }

              @Override
              public void generateHash(HashMap<String, String> valueMap, PayUHashGenerationListener hashGenerationListener) {

                     String hashName = valueMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                    String hashData = valueMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                    String hashVal="";
                    HashMap<String, String> dataMap = new HashMap<>();
                    if(hashName!=null&&(!hashName.isEmpty())&&hashName.equals("vas_for_mobile_sdk")){
                        hashVal=hashData+salt;
                    }else {
                        hashVal=hashData+salt;
                    }

                    String calculatedHash=hashCal("SHA-512", hashVal);
                    dataMap.put(hashName, calculatedHash);
                    hashGenerationListener.onHashGenerated(dataMap);


              }
            }
    );
  }




  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    this.pluginBinding=binding;
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
   onDetachedFromActivity();

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }


  @Override
  public void onDetachedFromActivity() {
   pluginBinding.removeActivityResultListener(this);
   pluginBinding=null;
  }



  public static String hashCal(String type, String hashString) {
    StringBuilder hash = new StringBuilder();
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance(type);
      messageDigest.update(hashString.getBytes());
      byte[] mdbytes = messageDigest.digest();
      for (byte hashByte : mdbytes) {
        hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return hash.toString();
  }
}
