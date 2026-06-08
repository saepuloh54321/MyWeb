package com.naragas.myweb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View listContainer, webContainer;
    private WebView webView;
    private TextView currentWebTitle;
    private SwitchMaterial switchRestrict;
    private RecyclerView recyclerView;
    private WebAdapter adapter;
    private List<WebSite> webSiteList;
    private String baseDomain = "";

    private static final String PREFS_NAME = "WebPrefs";
    private static final String KEY_SITES = "SavedSites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Views
        listContainer = findViewById(R.id.listContainer);
        webContainer = findViewById(R.id.webContainer);
        webView = findViewById(R.id.webView);
        currentWebTitle = findViewById(R.id.currentWebTitle);
        switchRestrict = findViewById(R.id.switchRestrict);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        View btnCloseWeb = findViewById(R.id.btnCloseWeb);

        // Adjust for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load Data
        loadWebSites();

        // Setup RecyclerView
        adapter = new WebAdapter(webSiteList, new WebAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WebSite site) {
                openWebsite(site);
            }

            @Override
            public void onEditClick(int position, WebSite site) {
                showEditDialog(position, site);
            }

            @Override
            public void onDeleteClick(int position) {
                webSiteList.remove(position);
                adapter.notifyItemRemoved(position);
                saveWebSites();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (switchRestrict.isChecked() && !baseDomain.isEmpty()) {
                    Uri uri = Uri.parse(url);
                    String host = uri.getHost();
                    if (host != null && host.endsWith(baseDomain)) {
                        return false;
                    } else {
                        Toast.makeText(MainActivity.this, "Akses dibatasi ke domain: " + baseDomain, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });

        // Add Site FAB
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Close Web UI
        btnCloseWeb.setOnClickListener(v -> closeWebView());

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webContainer.getVisibility() == View.VISIBLE) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        closeWebView();
                    }
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void openWebsite(WebSite site) {
        String url = site.getUrl();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            Uri uri = Uri.parse(url);
            baseDomain = uri.getHost();
            if (baseDomain == null) {
                Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            currentWebTitle.setText(site.getName());
            webView.loadUrl(url);
            listContainer.setVisibility(View.GONE);
            webContainer.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memuat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeWebView() {
        webView.loadUrl("about:blank");
        webContainer.setVisibility(View.GONE);
        listContainer.setVisibility(View.VISIBLE);
    }

    private void showAddDialog() {
        showSiteDialog(null, null);
    }

    private void showEditDialog(int position, WebSite site) {
        showSiteDialog(position, site);
    }

    private void showSiteDialog(Integer position, WebSite existingSite) {
        boolean isEdit = existingSite != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Ubah Website" : "Tambah Website");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_site, null);
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputUrl = view.findViewById(R.id.inputUrl);

        if (isEdit) {
            inputName.setText(existingSite.getName());
            inputUrl.setText(existingSite.getUrl());
        }
        
        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String url = inputUrl.getText().toString().trim();
            
            if (!name.isEmpty() && !url.isEmpty()) {
                if (isEdit) {
                    webSiteList.set(position, new WebSite(name, url));
                    adapter.notifyItemChanged(position);
                } else {
                    webSiteList.add(new WebSite(name, url));
                    adapter.notifyItemInserted(webSiteList.size() - 1);
                }
                saveWebSites();
            } else {
                Toast.makeText(this, "Nama dan URL harus diisi", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void loadWebSites() {
        webSiteList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SITES, null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    webSiteList.add(WebSite.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                android.util.Log.e("MainActivity", "Error loading sites", e);
            }
        }
    }

    private void saveWebSites() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (WebSite site : webSiteList) {
                array.put(site.toJsonObject());
            }
            prefs.edit().putString(KEY_SITES, array.toString()).apply();
        } catch (JSONException e) {
            android.util.Log.e("MainActivity", "Error saving sites", e);
        }
    }
}
