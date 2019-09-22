package com.example.bntouch.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class API {
    public static final String BASE_URL = "https://us-central1-safechat-7256d.cloudfunctions.net/";
    private RequestQueue requestQueue;

    public API(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void POST(final Object aClass, String endpoint, final ResponseAPI resp) {
        try {
            final Method[] methods = aClass.getClass().getDeclaredMethods();
            StringRequest postRequest = new StringRequest(Request.Method.POST, endpoint, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    resp.onSuccess(response);
                    Log.d("ResponseAPI", response);
                }
            }, new Response.ErrorListener()  {
                public void onErrorResponse(VolleyError error) {
                    resp.onFailure();
                    if(error.networkResponse != null) {
                        Log.d("ResponseAPI", Integer.toString(error.networkResponse.statusCode));
                    } else {
                        Log.d("ResponseAPI", error.toString());
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams () {
                    Map<String, String> params = new HashMap<String, String>();
                    try {
                        for (java.lang.reflect.Method method : methods) {
                            System.out.println("Method: " + aClass.getClass().getMethod(method.getName()));
                            Object output = aClass.getClass().getMethod(method.getName()).invoke(aClass);
                            System.out.println(" exec function: " + output);
                            params.put(getBaseName(method.getName()), output.toString());
                        }
                    }
                    catch (Exception ex) {
                        Log.d("Invoke", ex.getMessage());
                    }
                    finally {
                        return params;
                    }
                }
            };
            this.requestQueue.add(postRequest);
        }
        catch (Exception ex) {
            Log.d("ResponseAPI", ex.getMessage());
        }
    }

    private String getBaseName(String methodName) {
        System.out.println("getBaseName(): " + methodName.substring(3, methodName.length()).toLowerCase());
        return methodName.substring(3, methodName.length()).toLowerCase();
    }

}
