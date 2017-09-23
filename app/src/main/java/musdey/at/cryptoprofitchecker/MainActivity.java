package musdey.at.cryptoprofitchecker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener{

    public static int actualIndex = 0;
    public static double actualFetchedPrice;
    ArrayList<CryptoEntry> list;
    ListView lv;
    SwipeRefreshLayout swipeLayout;
    ArrayAdapter customAdapter;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupListView();
        loadExistingEntries();
        fetchDataTask();
    }

    private void updateListView(){
        for(CryptoEntry e : list){
            e.setActualCourse(actualFetchedPrice);
            e.calculateProfit();
        }
        synchronized (customAdapter){
            customAdapter.notifyDataSetChanged();
        }
    }

    private void setupListView() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataTask();
            }
        });
        lv = (ListView) findViewById(R.id.listview);
        lv.setOnItemLongClickListener(this);
        list = new ArrayList<CryptoEntry>();
        customAdapter = new EntryAdapter(this, list);
        lv.setAdapter(customAdapter);
    }

    private void loadExistingEntries() {
        prefs = getApplicationContext().getSharedPreferences("PREFS", 0);

        String json = prefs.getString("LIST", "empty");
        Gson gson = new Gson();

        if(json == "empty"){

            Toast.makeText(getApplicationContext(),"no entries found",Toast.LENGTH_SHORT).show();
            //no entries found
        }else{
            Type type = new TypeToken<ArrayList<CryptoEntry>>() {}.getType();
            ArrayList<CryptoEntry> availableData =  gson.fromJson(json, type);
            for(CryptoEntry e : availableData){
                list.add(e);
            }
            actualIndex = list.size();
            synchronized (customAdapter){
                customAdapter.notifyDataSetChanged();
            }
        }
    }

    public void addEntry(View v) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.text_entry, null);
        final EditText input1 = (EditText) textEntryView.findViewById(R.id.editText);
        final EditText input2 = (EditText) textEntryView.findViewById(R.id.editText2);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter your details:").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        CryptoEntry entry = new CryptoEntry();
                        entry.boughtAmount = Double.parseDouble(input1.getText().toString());
                        entry.priceWhenBought = Double.parseDouble(input2.getText().toString());
                        entry.entryId = actualIndex++;
                        list.add(entry);
                        synchronized (customAdapter){
                            customAdapter.notifyDataSetChanged();
                        }

                        String dataStr = new Gson().toJson(list);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("LIST", dataStr);
                        editor.commit();
                        updateListView();
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                    }
                });
        alert.show();
    }

    private void fetchDataTask() {
        DownloadFilesTask task = new DownloadFilesTask();
        task.execute(null, null);
    }

    private String fetchData(){
        String url = "https://api.coinbase.com/v2/prices/ETH-EUR/spot";
        int timeout = 100000;

        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        list.remove(i);
        customAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "Element deleted!", Toast.LENGTH_SHORT).show();

        String dataStr = new Gson().toJson(list);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("LIST", dataStr);
        editor.commit();

        return false;
    }

    private class DownloadFilesTask extends AsyncTask<URL, Integer, String> {

        protected void onProgressUpdate(Integer... progress) {
            // setProgressPercent(progress[0]);
        }
        @Override
        protected String doInBackground(URL... urls) {
            String data = fetchData();
            Log.i("DATA IS", data); //{"data":{"base":"ETH","currency":"EUR","amount":"235.57"}}
            JSONObject obj = null;
            try {
                obj = new JSONObject(data);
                actualFetchedPrice = Double.parseDouble(obj.getJSONObject("data").getString("amount"));
                Log.i("actualfetchedprice is",""+actualFetchedPrice);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return data;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeLayout.setRefreshing(false);
            updateListView();
            setTitle("ETH selling price @Coinbase "+actualFetchedPrice);
        }
    }
}