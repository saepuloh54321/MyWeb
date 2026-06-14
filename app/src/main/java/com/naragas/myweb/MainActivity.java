package com.naragas.myweb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private View listContainer, webContainer, historyContainer;
    private WebView webView;
    private TextView currentWebTitle;
    private SwitchMaterial switchRestrict;
    private WebAdapter adapter;
    private List<WebSite> webSiteList;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;
    private String baseDomain = "";
    private String currentSort = "added"; // "added", "edited", "accessed"
    private boolean isAuthenticated = false;

    private static final String PREFS_NAME = "WebPrefs";
    private static final String KEY_SITES = "SavedSites";
    private static final String KEY_HISTORY = "SavedHistory";
    private static final String KEY_SORT = "SortPref";
    private static final String KEY_PIN = "AppPin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        listContainer = findViewById(R.id.listContainer);
        webContainer = findViewById(R.id.webContainer);
        historyContainer = findViewById(R.id.historyContainer);
        webView = findViewById(R.id.webView);
        currentWebTitle = findViewById(R.id.currentWebTitle);
        switchRestrict = findViewById(R.id.switchRestrict);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        View btnCloseWeb = findViewById(R.id.btnCloseWeb);
        View btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        View btnSort = findViewById(R.id.btnSort);
        View btnCloseHistory = findViewById(R.id.btnCloseHistory);
        View btnClearHistory = findViewById(R.id.btnClearHistory);
        NavigationView navView = findViewById(R.id.nav_view);

        // Adjust for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load Data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentSort = prefs.getString(KEY_SORT, "added");
        loadWebSites();
        loadHistory();

        // Check PIN Lock
        checkAppLock();

        // Setup RecyclerView (Websites)
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

        // Setup RecyclerView (History)
        historyAdapter = new HistoryAdapter(historyList, url -> {
            loadDirectUrl(url, null);
            closeHistory();
        });
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);

        // Setup WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!url.equals("about:blank")) {
                    addToHistory(url);
                }
            }

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

        // Open Drawer
        btnOpenDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        // Sort List
        btnSort.setOnClickListener(v -> showSortDialog());

        // Navigation Drawer Item Clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                closeWebView();
                closeHistory();
            } else if (id == R.id.nav_add) {
                showAddDialog();
            } else if (id == R.id.nav_history) {
                openHistory();
            } else if (id == R.id.nav_pin) {
                showPinSetupDialog();
            } else if (id == R.id.nav_logout) {
                logout();
            } else if (id == R.id.nav_about) {
                showAboutDialog();
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        // Add Site FAB
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Close Web UI
        btnCloseWeb.setOnClickListener(v -> closeWebView());

        // History Actions
        btnCloseHistory.setOnClickListener(v -> closeHistory());
        btnClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Riwayat")
                    .setMessage("Apakah Anda yakin ingin menghapus semua riwayat?")
                    .setPositiveButton("Ya", (d, w) -> {
                        historyList.clear();
                        historyAdapter.notifyDataSetChanged();
                        saveHistory();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else if (historyContainer.getVisibility() == View.VISIBLE) {
                    closeHistory();
                } else if (webContainer.getVisibility() == View.VISIBLE) {
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
        site.setLastAccessed(System.currentTimeMillis());
        sortWebSites();
        adapter.notifyDataSetChanged();
        saveWebSites();
        loadDirectUrl(site.getUrl(), site.getName());
    }

    private void loadDirectUrl(String url, String title) {
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://" + finalUrl;
        }

        try {
            Uri uri = Uri.parse(finalUrl);
            baseDomain = uri.getHost();
            if (baseDomain == null) {
                Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            currentWebTitle.setText(title != null ? title : finalUrl);
            webView.loadUrl(finalUrl);
            listContainer.setVisibility(View.GONE);
            historyContainer.setVisibility(View.GONE);
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

    private void openHistory() {
        listContainer.setVisibility(View.GONE);
        webContainer.setVisibility(View.GONE);
        historyContainer.setVisibility(View.VISIBLE);
        historyAdapter.notifyDataSetChanged();
    }

    private void closeHistory() {
        historyContainer.setVisibility(View.GONE);
        if (webContainer.getVisibility() != View.VISIBLE) {
            listContainer.setVisibility(View.VISIBLE);
        }
    }

    private void addToHistory(String url) {
        // Jangan simpan jika URL sama dengan yang terakhir
        if (!historyList.isEmpty() && historyList.get(0).getUrl().equals(url)) {
            return;
        }
        historyList.add(0, new HistoryItem(url, System.currentTimeMillis()));
        // Batasi histori (misal 100 item)
        if (historyList.size() > 100) {
            historyList.remove(historyList.size() - 1);
        }
        historyAdapter.notifyDataSetChanged();
        saveHistory();
    }

    private void showSortDialog() {
        String[] options = {"Baru Ditambahkan", "Baru Diubah", "Baru Diakses", "Nama A-Z", "Nama Z-A"};
        int checkedItem = 0;
        if (currentSort.equals("edited")) checkedItem = 1;
        else if (currentSort.equals("accessed")) checkedItem = 2;
        else if (currentSort.equals("az")) checkedItem = 3;
        else if (currentSort.equals("za")) checkedItem = 4;

        new AlertDialog.Builder(this)
                .setTitle("Urutkan Berdasarkan")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    if (which == 0) currentSort = "added";
                    else if (which == 1) currentSort = "edited";
                    else if (which == 2) currentSort = "accessed";
                    else if (which == 3) currentSort = "az";
                    else if (which == 4) currentSort = "za";
                    
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putString(KEY_SORT, currentSort).apply();
                    
                    sortWebSites();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .show();
    }

    private void sortWebSites() {
        if (webSiteList == null || webSiteList.isEmpty()) return;
        
        webSiteList.sort((a, b) -> {
            switch (currentSort) {
                case "edited":
                    return Long.compare(b.getUpdatedAt(), a.getUpdatedAt());
                case "accessed":
                    return Long.compare(b.getLastAccessed(), a.getLastAccessed());
                case "az":
                    return a.getName().compareToIgnoreCase(b.getName());
                case "za":
                    return b.getName().compareToIgnoreCase(a.getName());
                default: // "added"
                    return Long.compare(b.getCreatedAt(), a.getCreatedAt());
            }
        });
    }

    private void checkAppLock() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPin = prefs.getString(KEY_PIN, "");
        if (savedPin.isEmpty()) {
            isAuthenticated = true;
        } else {
            isAuthenticated = false;
            showPinEntryDialog(savedPin);
        }
    }

    private void showPinEntryDialog(String savedPin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aplikasi Terkunci");
        builder.setCancelable(false);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null);
        EditText inputPin = view.findViewById(R.id.inputPin);
        
        builder.setView(view);
        builder.setPositiveButton("Buka", null); // Set later to prevent auto-dismiss
        builder.setNegativeButton("Keluar", (d, w) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String enteredPin = inputPin.getText().toString();
            if (enteredPin.equals(savedPin)) {
                isAuthenticated = true;
                dialog.dismiss();
            } else {
                Toast.makeText(this, "PIN Salah!", Toast.LENGTH_SHORT).show();
                inputPin.setText("");
            }
        });
    }

    private void showPinSetupDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPin = prefs.getString(KEY_PIN, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(savedPin.isEmpty() ? "Atur PIN Baru" : "Ubah PIN");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null);
        TextView txtInstruction = view.findViewById(R.id.txtPinInstruction);
        EditText inputPin = view.findViewById(R.id.inputPin);
        
        txtInstruction.setText(savedPin.isEmpty() ? "Masukkan 4 angka PIN" : "Masukkan PIN Baru (Kosongkan untuk hapus)");
        
        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newPin = inputPin.getText().toString();
            if (newPin.isEmpty() || newPin.length() == 4) {
                prefs.edit().putString(KEY_PIN, newPin).apply();
                Toast.makeText(this, newPin.isEmpty() ? "PIN Dihapus" : "PIN Berhasil Disimpan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "PIN harus 4 angka", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void logout() {
        isAuthenticated = false;
        drawerLayout.closeDrawer(GravityCompat.END);
        checkAppLock();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
        
        TextView txtLink = view.findViewById(R.id.txtLink);
        String url = "https://www.saweria.co/Sorasae";
        txtLink.setText(url);
        txtLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        builder.setView(view);
        builder.setPositiveButton("Tutup", null);
        builder.show();
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
                    existingSite.setName(name);
                    existingSite.setUrl(url);
                } else {
                    webSiteList.add(new WebSite(name, url));
                }
                sortWebSites();
                adapter.notifyDataSetChanged();
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
                sortWebSites();
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

    private void loadHistory() {
        historyList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    historyList.add(HistoryItem.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                android.util.Log.e("MainActivity", "Error loading history", e);
            }
        }
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (HistoryItem item : historyList) {
                array.put(item.toJsonObject());
            }
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
        } catch (JSONException e) {
            android.util.Log.e("MainActivity", "Error saving history", e);
        }
    }
}
