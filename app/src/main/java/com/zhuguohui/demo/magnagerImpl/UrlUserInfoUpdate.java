package com.zhuguohui.demo.magnagerImpl;



import com.zhuguohui.demo.userstate.UserInfoUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/14
 * Time: 17:20
 * Desc:在用户登录以后。用于替换request中的userId信息
 * </pre>
 */
public class UrlUserInfoUpdate implements UserInfoUpdate {

    @Override
    public Request updateUserInfo(Request oldRequest) {
        boolean isPostMethod=oldRequest.method().equals("POST");
        if(isPostMethod){
            return dealPostRequest(oldRequest);
        }else{
            return dealNoPostRequest(oldRequest);
        }
    }

    private Request dealPostRequest(Request oldRequest) {
        MediaType mediaType = oldRequest.body().contentType();
        boolean isJson=isJson(mediaType);
        if(isJson){
            String json = bodyToString(oldRequest);
            String newJson="";
            try {
                JSONObject jsonObject=new JSONObject(json);
                jsonObject.remove("userId");
                jsonObject.put("userId",getUserId());
                 newJson = jsonObject.toString();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return oldRequest.newBuilder()
                    .post(RequestBody.create(mediaType,newJson))
                    .build();

        }
        return oldRequest;
    }

    private String getUserId() {
        //模拟方法，
        return "123";
    }


    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private boolean isJson(MediaType mediaType) {
        if(mediaType==null){
            return false;
        }
        return "application".equals(mediaType.type()) && "json".equals(mediaType.subtype());
    }

    private Request dealNoPostRequest(Request oldRequest){
        HttpUrl url = oldRequest.url();
        HttpUrl url2 = url.newBuilder().removeAllQueryParameters("userId")
                .addQueryParameter("userId", getUserId())
                .build();
        return oldRequest.newBuilder()
                .url(url2)
                .build();
    }
}
