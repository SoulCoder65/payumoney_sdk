import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PayumoneySdk {
  static final PayumoneySdk _instance = PayumoneySdk._internal();

  factory PayumoneySdk() {
    return _instance;
  }

  PayumoneySdk._internal();

  static const MethodChannel _channel = const MethodChannel('payumoney_sdk');



  Future<Map<dynamic,dynamic>?> buildPaymentParams({
     required String amount,
     required String transactionId,
     required String phone,
     required String productInfo,
     required String firstName,
     required String email,
     required String hash,
     required String salt,
     required String successURL,
     required String failureURL,
     required String merchantKey,
     bool isProduction=false,
     String? userCredentials,
     String merchantName="Payu",
     bool showExitConfirmation=true,
     String? udf1,
     String? udf2,
     String? udf3,
     String? udf4,
     String? udf5,
     String? udf6,
     String? udf7,
     String? udf8,
     String? udf9,
     String? udf10,


    
   })async{
     ///Success URL="https://www.payumoney.com/mobileapp/payumoney/success.php"
     ///Falure URL="https://www.payumoney.com/mobileapp/payumoney/failure.php"

   try{
   final data=  await _channel.invokeMethod("buildPaymentParams",{
       "amount":amount,
       "transactionId":transactionId,
       "phone":phone,
       "productInfo":productInfo,
       "firstName":firstName,
       "email":email,
       "successURL":successURL,
       "failureURL":failureURL,
       "isProduction":isProduction,
       "merchantKey":merchantKey,
       "userCredentials":userCredentials,
       "hash":hash,
        "salt":salt,
        "merchantName":merchantName,
        "showExitConfirmation":showExitConfirmation,
        "udf1":udf1,
        "udf2":udf2,
        "udf3":udf3,
        "udf4":udf4,
        "udf5":udf5,
        "udf6":udf6,
        "udf7":udf7,
        "udf8":udf8,
        "udf9":udf9,
        "udf10":udf10,
     });



   return data;
   }catch (e){
     debugPrint(e.toString());

     final errorResponse={
       "status":"failed",
       "message":"payment canceled"
     };
     return errorResponse;
   }
   }


 Future<void> setPayuSiParam({
     required bool isFreeTrial,
     required String billingAmt,
     required String billingCycle,
     required String setBillingInterval,
     required String paymentStartDate,
     required String paymentEndDate,
     required String remarks,}

     )async {
    try{
      _channel.invokeMethod("setPayuSiParam",{
        "isFreeTrial":"${isFreeTrial}",
        "billingAmt":billingAmt,
        "billingCycle":billingCycle,
        "setBillingInterval":setBillingInterval,
        "paymentStartDate":paymentStartDate,
        "paymentEndDate":paymentEndDate,
        "remarks":remarks

      });
    }catch(e){
      debugPrint(e.toString());
    }
 }



}
