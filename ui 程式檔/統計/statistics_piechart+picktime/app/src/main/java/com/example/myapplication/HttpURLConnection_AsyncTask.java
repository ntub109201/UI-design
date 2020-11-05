package com.example.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class HttpURLConnection_AsyncTask extends AsyncTask<Map<String, String>, Void, String> {
    @Override
    protected String doInBackground(Map<String, String>... maps) {
        String result = "";
        HttpURLConnection connection = null;
        InputStream is = null;
        try{
            String APIUrl = "https://guidary.000webhostapp.com/director.php";
            URL url = new URL(APIUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            //connection.setRequestProperty("authentication", MainActivity.Authentication);
            connection.setDoInput(true);
            connection.setDoOutput(true);
//            connection.setInstanceFollowRedirects(true);//设置只作用于当前的实例
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//            connection.setRequestProperty("Cookie", "SSID=" + token);
//            connection.setConnectTimeout(20*1000);//设置连接主机超时（单位：毫秒）
//            connection.setReadTimeout(20*1000);//设置从主机读取数据超时（单位：毫秒）

//            版权声明：本文为CSDN博主「傅小逗_」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
//            原文链接：https://blog.csdn.net/qq_41117947/article/details/79361094
            connection.connect();

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<String> iterator = maps[0].keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                stringBuilder.append(key).append("=").append(URLEncoder.encode(maps[0].get(key), "UTF-8")).append("&");
            }
//              stringBuilder.append("parameter1=").append(URLEncoder.encode(parameter1, "UTF-8")).append("&");
//              stringBuilder.append("parameter2=").append(URLEncoder.encode(parameter2, "UTF-8")).append("&");
//              stringBuilder.append("parameter3=").append(URLEncoder.encode(parameter3, "UTF-8")).append("&");
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            outputStream.writeBytes((stringBuilder.toString()));
            outputStream.flush();
            outputStream.close();

            InputStream inputStream = connection.getInputStream();
            int status = connection.getResponseCode();
            Log.d("Log Test", String.valueOf(status));
            if(inputStream != null){
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                //StringBuilder builder = new StringBuilder();
                String line="";
                while((line = bufReader.readLine()) != null){
                    result += (line+"\n");
                }
            }else {
                result = "No Results";
            }
            inputStream.close();

        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
