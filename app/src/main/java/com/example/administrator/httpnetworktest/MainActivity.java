package com.example.administrator.httpnetworktest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Struct;


public class MainActivity extends Activity {
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init();
    }

    private void Init()
    {
        imageView=(ImageView)findViewById(R.id.img);
        new DownloadImageTask().execute("http://www.mayoff.com/5-01cablecarDCP01934.jpg");
        new DownloadTextTask().execute("http://www.baidu.com");
    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException {
        InputStream in = null;
        int respone=-1;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if(!(conn instanceof HttpURLConnection))
        {
            throw new IOException("not an HTTP connection");
        }
        try
        {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            respone = httpConn.getResponseCode();
            if(respone==HttpURLConnection.HTTP_OK)
            {
                in=httpConn.getInputStream();
            }
        }
        catch(Exception ex)
        {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("ERROR connecting");
        }
        return  in;
    }

    private Bitmap DownloadImage(String url)
    {
        Bitmap bitmap = null;
        InputStream in = null;
        try
        {
            in=OpenHttpConnection(url);
            bitmap= BitmapFactory.decodeStream(in);
            in.close();
        }
        catch (IOException ioe)
        {
            Log.d("NetworkingActivity", ioe.getLocalizedMessage());
        }

        return bitmap;
    }

    private String DownloadText(String url)
    {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        try
        {
            in=OpenHttpConnection(url);
        }
        catch (IOException ioe)
        {
            Log.d("Networking", ioe.getLocalizedMessage());
            return "";
        }
        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];
        try
        {
            while ((charRead=isr.read(inputBuffer))>0)
            {
                String readString=
                        String.copyValueOf(inputBuffer,0,charRead);
                str+=readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            isr.close();
            in.close();
        }
        catch (IOException ioe)
        {
            Log.d("Networking", ioe.getLocalizedMessage());
            return "";
        }
        return str;
    }

    private class  DownloadImageTask extends AsyncTask<String,Void,Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls) {
            return DownloadImage(urls[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private class DownloadTextTask extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... urls) {
            return DownloadText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
        }
    }
}
