package com.example.justinjoco.wildfiresensornet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Button btnSendRequest;
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String url = "http://sensorgurusandroid.mybluemix.net/api/v1/sensors";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSendRequest = (Button)findViewById(R.id.btnSendRequest);

        btnSendRequest.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                sendRequestAndPrintResponse();

            }


        });


    }

    private void sendRequestAndPrintResponse() {
        mRequestQueue = Volley.newRequestQueue(this);

        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Response: "+ response.toString());
                Log.i(TAG, "Response: "+ response.getClass().getName());
                try {
                    JSONArray arr = new JSONArray(response);
                    JSONObject obj = (JSONObject)arr.get(0);
                    Log.i(TAG, "JSON: "+ obj.toString());
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    Log.i(TAG, "Error: "+ error.toString());

                }



        });
        mRequestQueue.add(stringRequest);





    }


    public void openMap(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }
}
