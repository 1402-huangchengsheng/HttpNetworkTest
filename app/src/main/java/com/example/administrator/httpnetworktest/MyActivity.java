package com.example.administrator.httpnetworktest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class MyActivity extends Activity {
    private TextView getText;
    private TextView postText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Init();
    }

    void Init() {
        getText = (TextView) findViewById(R.id.getText);
        postText = (TextView) findViewById(R.id.postText);
        new GetDownloadText().execute("http://10.0.2.2:8080/GetTest");
        new PostDownloadText().execute(new String[]{"http://10.0.2.2:8080/PostTest","number=2"});

    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException {//GET
        InputStream inputStream = null;
        int respone = -1;
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("not an HTTP connection");
        }
        try {
            HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            respone = httpConn.getResponseCode();
            if (respone == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("ERROR connecting");
        }
        return inputStream;
    }

    private InputStream OpenHttpConnection(String urlString,String postString)
            throws IOException
    {//POST
        InputStream inputStream = null;
        int respone = -1;
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        if(!(urlConnection instanceof HttpURLConnection))
        {
            return  null;
        }
        HttpURLConnection conn = (HttpURLConnection)urlConnection;
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        PrintWriter pw = new PrintWriter(conn.getOutputStream());
        Log.d("Test", postString);
        pw.print(postString);
        pw.flush();
        pw.close();
        conn.connect();
        respone=conn.getResponseCode();
        if(respone==HttpURLConnection.HTTP_OK)
        {
            inputStream = conn.getInputStream();
        }

        return inputStream;
    }

    private String DownloadText(String url,String postString) throws UnsupportedEncodingException {
        int BUFFER_SIZE = 1024;
        InputStream in = null;
        try {
            if(postString==null)
            {
                in = OpenHttpConnection(url);
            }
            else
            {
                in=OpenHttpConnection(url,postString);
            }
        } catch (IOException ioe) {
            Log.d("Networking", ioe.getLocalizedMessage());
            return "";
        }
        InputStreamReader isr = new InputStreamReader(in,"UTF-8");
        int charRead = -1;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];
        try {
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString =
                        String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            isr.close();
            in.close();
        } catch (IOException ioe) {
            Log.d("Networking", ioe.getLocalizedMessage());
            return "";
        }
        return str;
    }

    /**
     *将JSON 转换为List
     * @param url 地址
     * @return JSON
     * @throws Exception
     */
    public List<Map<String, String>> getJSONObject(String url,String postString)
            throws Exception {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        String json = DownloadText(url,postString);
        Log.d("Test","json:"+json);
        if (json==null||json.equals("")||json.equals("{}"))
            return list;
         JSONObject jsonObject = new JSONObject(json); // 返回的数据形式是一个Object类型，所以可以直接转换成一个Object
        // 里面有一个数组数据，可以用getJSONArray获取数组
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        Log.d("Test",jsonArray.toString());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i); // 得到每个对象
            String className = item.getString("className");
            String teacherName = item.getString("teacherName");
            String address = item.getString("address");
            int whatDay = item.getInt("whatDay");
            int start = item.getInt("start");
            int length = item.getInt("length");
            map = new HashMap<String, String>();
            map.put("className",className);
            map.put("teacherName",teacherName);
            map.put("address",address);
            map.put("whatDay",whatDay+"");
            map.put("start",start+"");
            map.put("length",length+"");
            list.add(map);
        }
        return list;
    }

    private class GetDownloadText extends AsyncTask<String,Void,List<Map<String,String>>>
    {
        @Override
        protected List<Map<String, String>> doInBackground(String... strings)
        {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            try
            {
                list = getJSONObject(strings[0],null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> maps) {
            getText.setText(maps.toString());
        }
    }

    private class PostDownloadText extends AsyncTask<String,Void,List<Map<String,String>>>
    {
        @Override
        protected List<Map<String, String>> doInBackground(String... strings)
        {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            try
            {
                list = getJSONObject(strings[0],strings[1]);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> maps) {
            postText.setText(maps.toString());
        }
    }

}
