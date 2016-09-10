package org.telegram.messenger.shamChat.Components;

import android.content.Context;


import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Msa on 8/10/16.
 */
public  class msaOkHttp {
    Context context ;
    JSONObject postParams  ;
    JsonObject jsonObjectParms ;
    onResalt onresalt ;

   // ArrayMap<String , File>  mFileUpload = new ArrayMap<>() ;

    JSONObject mFileUpload = new JSONObject() ;
    String url ;
    boolean onUi =true  ;

    public msaOkHttp(){
    }

    public msaOkHttp with(Context context) {
        this.context = context ;
        this.postParams = new JSONObject() ;
        return this ;
    }

    public msaOkHttp with() {
        this.postParams = new JSONObject() ;
        return this ;
    }

    public msaOkHttp addFile( String key , File file) {

        try {
            this.mFileUpload.put(key ,file) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this ;
    }
    public msaOkHttp onUI(boolean onUi) {
        this.onUi = onUi ;
        return  this ;
    }

    public  msaOkHttp url(String url ) {
        this.url = url ;
        return this ;
    }

    public msaOkHttp addPostParams(String key , String value ) {
        try {
            this.postParams.put(key , value) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this ;
    }

    public msaOkHttp addJsonPut (JsonObject js) {
        this.jsonObjectParms = js ;
        return  this ;
    }

    public msaOkHttp setOnResalt(onResalt onr) {
        this.onresalt = onr ;
        return  this ;
    }

    public void run() throws Throwable {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                .connectTimeout(60 , TimeUnit.SECONDS)
                .readTimeout(60 , TimeUnit.SECONDS)
                .build();

        RequestBody formBody = null;

        if (this.jsonObjectParms != null) {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            formBody = RequestBody.create(JSON, jsonObjectParms.toString());
        }else {
            if (mFileUpload.length() == 0 ) {
                FormBody.Builder formBuilder = new FormBody.Builder();
                Iterator<String> temp = postParams.keys();
                while (temp.hasNext()) {
                    String key = temp.next();
                    String value = postParams.getString(key);
                    formBuilder.add(key, value);
                }

                formBody = formBuilder.build();

            }else {


                MultipartBody.Builder requestBody = new MultipartBody.Builder() ;
                requestBody.setType(MultipartBody.FORM) ;



                Iterator<String> temp = postParams.keys();
                while (temp.hasNext()) {
                    String key = temp.next();
                    String value = postParams.getString(key);
                    requestBody.addFormDataPart(key, value);

                }


                Iterator<String> tempFile = mFileUpload.keys();
                while (tempFile.hasNext()) {
                    String key = tempFile.next();
                    File valueFile = (File) mFileUpload.get(key);
                    requestBody.addFormDataPart(key,"upload.png",
                            RequestBody.create(MediaType.parse("image/png") , valueFile)) ;

                }

                formBody = requestBody.build() ;

            }
        }


        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        // check if request was successful
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        final String stringResponse = response.body().string();
        response.body().close();



        JSONObject checkEror = new JSONObject(stringResponse);
        int status = checkEror.getInt("status");
        if (status != 200) throw new IOException("status");


        if (onUi) {


            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        msaOkHttp.this.onresalt.onComplete(stringResponse.trim());
                    } catch (Throwable throwable) {
                        msaOkHttp.this.onresalt.onFailure(throwable , msaOkHttp.this);
                    }
                }
            });


        }else {
            try {
                msaOkHttp.this.onresalt.onComplete(stringResponse.trim());
            } catch (Throwable throwable) {
                msaOkHttp.this.onresalt.onFailure(throwable , msaOkHttp.this);
            }
        }



        // check if API processed the leave request successfully
    }

    public void Run()  {
        new Thread() {
            @Override
            public void run()   {
                try {
                    msaOkHttp.this.run();
                } catch (final Throwable throwable) {
                    throwable.printStackTrace();
                    if (onUi) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                msaOkHttp.this.onresalt.onFailure(throwable , msaOkHttp.this) ;
                            }
                        });
                    }else {
                        msaOkHttp.this.onresalt.onFailure(throwable , msaOkHttp.this);
                    }
                    }

            }
        }.start();


    }

    public  interface  onResalt {
        void onComplete(String result) throws Throwable ;
         void onFailure(Throwable throwable, msaOkHttp Conn);
    }
}
