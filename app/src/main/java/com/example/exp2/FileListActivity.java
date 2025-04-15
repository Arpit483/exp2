package com.example.exp2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exp2.recycler.FileAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FileListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private ProgressBar progressBar;

    private static final String EXCEL_FILES_URL = "https://9ed6-2409-40c2-104c-bae3-19d2-b49f-ec06-317a.ngrok-free.app/excel-files";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        recyclerView = findViewById(R.id.recyclerViewFiles);
        progressBar = findViewById(R.id.progressBarFiles);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        fetchExcelFiles();
    }

    private void fetchExcelFiles() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                URL url = new URL(EXCEL_FILES_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray filesArray = jsonResponse.getJSONArray("files");

                    ArrayList<String> fileList = new ArrayList<>();
                    for (int i = 0; i < filesArray.length(); i++) {
                        fileList.add(filesArray.getString(i));
                    }

                    runOnUiThread(() -> {
                        adapter.updateFiles(fileList);
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    throw new Exception("Server responded with code: " + code);
                }

            } catch (Exception e) {
                Log.e("FileListActivity", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load files: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
