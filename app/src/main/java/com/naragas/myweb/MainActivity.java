package com.naragas.myweb;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebResourceRequest;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private View listContainer, webContainer, historyContainer;
    private WebView webView;
    private ProgressBar progressBar;
    private EditText editSearch;
    private TextView currentWebTitle, textWebCount;
    private SwitchMaterial switchRestrict, switchDesktop;
    private TabLayout tabCategories;
    private WebAdapter adapter;
    private List<WebSite> webSiteList;
    private List<Category> categoryList;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;
    private String baseDomain = "";
    private String currentSort = "added"; // "added", "edited", "accessed"
    private String selectedCategoryId = "all"; // "all" for all categories
    private boolean isAuthenticated = false;
    private ValueCallback<Uri[]> uploadMessage;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    private static final String PREFS_NAME = "WebPrefs";
    private static final String KEY_SITES = "SavedSites";
    private static final String KEY_CATEGORIES = "SavedCategories";
    private static final String KEY_HISTORY = "SavedHistory";
    private static final String KEY_SORT = "SortPref";
    private static final String KEY_PIN = "AppPin";
    private static final int PERMISSION_REQUEST_ACCOUNTS = 101;

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
        progressBar = findViewById(R.id.progressBar);
        editSearch = findViewById(R.id.editSearch);
        currentWebTitle = findViewById(R.id.currentWebTitle);
        textWebCount = findViewById(R.id.textWebCount);
        switchRestrict = findViewById(R.id.switchRestrict);
        switchDesktop = findViewById(R.id.switchDesktop);
        tabCategories = findViewById(R.id.tabCategories);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        View btnCloseWeb = findViewById(R.id.btnCloseWeb);
        View btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        View btnSort = findViewById(R.id.btnSort);
        View btnRefresh = findViewById(R.id.btnRefresh);
        View btnCloseHistory = findViewById(R.id.btnCloseHistory);
        View btnClearHistory = findViewById(R.id.btnClearHistory);
        NavigationView navView = findViewById(R.id.nav_view);

        // Adjust for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Update Drawer Header with Email
        updateDrawerHeader(navView);

        // Setup RecyclerViews early to avoid NullPointer
        webSiteList = new ArrayList<>();
        categoryList = new ArrayList<>();
        historyList = new ArrayList<>();

        adapter = new WebAdapter(webSiteList, new WebAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WebSite site) {
                openWebsite(site);
            }

            @Override
            public void onEditClick(int position, WebSite site) {
                showEditDialog(site);
            }

            @Override
            public void onDeleteClick(WebSite site) {
                webSiteList.remove(site);
                adapter.updateList(webSiteList);
                saveWebSites();
                updateWebCount();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        historyAdapter = new HistoryAdapter(historyList, url -> {
            loadDirectUrl(url, null);
            closeHistory();
        });
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);

        // Load Data
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentSort = prefs.getString(KEY_SORT, "added");
        loadCategories();
        loadWebSites();
        loadHistory();
        updateWebCount();
        setupCategoryTabs();

        // Setup Export Launcher
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri != null) performExport(uri);
                }
        );

        // Setup Import Launcher
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) performImport(uri);
                }
        );

        // Setup File Picker for Upload
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && uploadMessage != null) {
                        Intent data = result.getData();
                        Uri[] results = null;
                        if (data != null) {
                            results = new Uri[]{data.getData()};
                        }
                        uploadMessage.onReceiveValue(results);
                        uploadMessage = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                }
        );

        // Search Logic
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Check PIN Lock
        checkAppLock();

        // Setup WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        // Desktop Mode Support
        String mobileUserAgent = webView.getSettings().getUserAgentString();
        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        switchDesktop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                webView.getSettings().setUserAgentString(desktopUserAgent);
                webView.getSettings().setUseWideViewPort(true);
                webView.getSettings().setLoadWithOverviewMode(true);
            } else {
                webView.getSettings().setUserAgentString(mobileUserAgent);
                webView.getSettings().setUseWideViewPort(false);
                webView.getSettings().setLoadWithOverviewMode(false);
            }
            webView.reload();
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    filePickerLauncher.launch(intent);
                    return true;
                } catch (Exception e) {
                    uploadMessage = null;
                    return false;
                }
            }
        });

        // Setup Download Listener
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                
                // Create download directory
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }
                
                File file = new File(downloadDir, fileName);
                
                // Show download confirmation dialog
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Download File")
                        .setMessage("Download " + fileName + "?")
                        .setPositiveButton("Download", (dialog, which) -> {
                            // Use download manager or open in browser
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Membuka di browser untuk download", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Gagal download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        // Refresh WebView
        btnRefresh.setOnClickListener(v -> webView.reload());

        // Navigation Drawer Item Clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                closeWebView();
                closeHistory();
            } else if (id == R.id.nav_add) {
                showAddDialog();
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(this, CategoryActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_history) {
                openHistory();
            } else if (id == R.id.nav_pin) {
                showPinSetupDialog();
            } else if (id == R.id.nav_clear) {
                clearAppData();
            } else if (id == R.id.nav_export) {
                exportData();
            } else if (id == R.id.nav_import) {
                importData();
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
            new MaterialAlertDialogBuilder(this)
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data because they might have been changed in CategoryActivity
        loadCategories();
        loadWebSites();
        setupCategoryTabs();
        sortWebSites();
        updateWebCount();
    }

    private void updateDrawerHeader(NavigationView navView) {
        View headerView = navView.getHeaderView(0);
        TextView textEmail = headerView.findViewById(R.id.textAccountEmail);
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_REQUEST_ACCOUNTS);
            textEmail.setText("Izin Akun diperlukan");
            return;
        }

        try {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType("com.google");
            if (accounts.length > 0) {
                // Tampilkan email pertama yang ditemukan
                textEmail.setText("Backup: " + accounts[0].name);
            } else {
                textEmail.setText("Auto Backup Aktif (Sistem)");
            }
        } catch (SecurityException e) {
            textEmail.setText("Auto Backup Aktif (Sistem)");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCOUNTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, perbarui header
                NavigationView navView = findViewById(R.id.nav_view);
                updateDrawerHeader(navView);
            }
        }
    }

    private void openWebsite(WebSite site) {
        site.setLastAccessed(System.currentTimeMillis());
        sortWebSites();
        adapter.updateList(webSiteList);
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
        if (url == null || url.isEmpty() || url.equals("about:blank")) return;
        
        // Normalisasi URL: Hapus trailing slash jika ada
        String finalUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        // Jangan simpan jika URL sama dengan yang terakhir (abaikan trailing slash)
        if (!historyList.isEmpty()) {
            String lastUrl = historyList.get(0).getUrl();
            if (lastUrl.endsWith("/")) lastUrl = lastUrl.substring(0, lastUrl.length() - 1);
            if (lastUrl.equals(finalUrl)) return;
        }
        
        historyList.add(0, new HistoryItem(url, System.currentTimeMillis()));
        // Batasi histori (misal 100 item)
        if (historyList.size() > 100) {
            historyList.remove(historyList.size() - 1);
        }
        
        runOnUiThread(() -> {
            if (historyAdapter != null) {
                historyAdapter.notifyDataSetChanged();
            }
        });
        saveHistory();
    }

    private void showSortDialog() {
        String[] options = {"Baru Ditambahkan", "Baru Diubah", "Baru Diakses", "Nama A-Z", "Nama Z-A"};
        int checkedItem = 0;
        if (currentSort.equals("edited")) checkedItem = 1;
        else if (currentSort.equals("accessed")) checkedItem = 2;
        else if (currentSort.equals("az")) checkedItem = 3;
        else if (currentSort.equals("za")) checkedItem = 4;

        new MaterialAlertDialogBuilder(this)
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
                    adapter.updateList(webSiteList);
                    dialog.dismiss();
                })
                .show();
    }

    private void sortWebSites() {
        if (webSiteList == null || webSiteList.isEmpty()) return;
        
        List<WebSite> filteredList = webSiteList;
        if (!selectedCategoryId.equals("all")) {
            filteredList = webSiteList.stream()
                    .filter(s -> s.getCategoryId().equals(selectedCategoryId))
                    .collect(java.util.stream.Collectors.toList());
        }

        filteredList.sort((a, b) -> {
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
        
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void setupCategoryTabs() {
        tabCategories.removeAllTabs();
        tabCategories.addTab(tabCategories.newTab().setText("Semua").setTag("all"));
        for (Category cat : categoryList) {
            tabCategories.addTab(tabCategories.newTab().setText(cat.getName()).setTag(cat.getId()));
        }

        tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategoryId = (String) tab.getTag();
                sortWebSites();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
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

    private void clearAppData() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Bersihkan Data")
                .setMessage("Apakah Anda yakin ingin menghapus Cache, Cookie, dan Data Penjelajahan?\n\n(Anda mungkin harus login ulang di beberapa website)")
                .setPositiveButton("Ya, Bersihkan", (dialog, which) -> {
                    // Clear Cache
                    webView.clearCache(true);
                    
                    // Clear Cookies
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                    
                    // Clear Web Storage (Databases, Local Storage)
                    WebStorage.getInstance().deleteAllData();
                    
                    Toast.makeText(this, "Data penjelajahan berhasil dibersihkan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void exportData() {
        exportLauncher.launch("myweb_backup.json");
    }

    private void performExport(Uri uri) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONObject backup = new JSONObject();
            backup.put(KEY_SITES, new JSONArray(prefs.getString(KEY_SITES, "[]")));
            backup.put(KEY_CATEGORIES, new JSONArray(prefs.getString(KEY_CATEGORIES, "[]")));
            backup.put(KEY_HISTORY, new JSONArray(prefs.getString(KEY_HISTORY, "[]")));
            backup.put(KEY_PIN, prefs.getString(KEY_PIN, ""));
            backup.put(KEY_SORT, prefs.getString(KEY_SORT, "added"));

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(backup.toString(4).getBytes());
                outputStream.close();
                Toast.makeText(this, "Data Berhasil Diekspor", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal Ekspor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importData() {
        importLauncher.launch(new String[]{"application/json"});
    }

    private void performImport(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();

            JSONObject backup = new JSONObject(stringBuilder.toString());
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (backup.has(KEY_SITES)) editor.putString(KEY_SITES, backup.getJSONArray(KEY_SITES).toString());
            if (backup.has(KEY_CATEGORIES)) editor.putString(KEY_CATEGORIES, backup.getJSONArray(KEY_CATEGORIES).toString());
            if (backup.has(KEY_HISTORY)) editor.putString(KEY_HISTORY, backup.getJSONArray(KEY_HISTORY).toString());
            if (backup.has(KEY_PIN)) editor.putString(KEY_PIN, backup.getString(KEY_PIN));
            if (backup.has(KEY_SORT)) editor.putString(KEY_SORT, backup.getString(KEY_SORT));

            editor.apply();
            
            Toast.makeText(this, "Data Berhasil Diimpor", Toast.LENGTH_SHORT).show();
            
            // Refresh App
            loadCategories();
            loadWebSites();
            loadHistory();
            currentSort = prefs.getString(KEY_SORT, "added");
            setupCategoryTabs();
            sortWebSites();
            historyAdapter.notifyDataSetChanged();
            updateWebCount();
            
        } catch (Exception e) {
            Toast.makeText(this, "Gagal Impor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWebCount() {
        if (textWebCount != null && webSiteList != null) {
            textWebCount.setText("(" + webSiteList.size() + ")");
        }
    }

    private void showAboutDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
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
        showSiteDialog(null);
    }

    private void showEditDialog(WebSite site) {
        showSiteDialog(site);
    }

    private void showSiteDialog(WebSite existingSite) {
        boolean isEdit = existingSite != null;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(isEdit ? "Ubah Website" : "Tambah Website");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_site, null);
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputUrl = view.findViewById(R.id.inputUrl);
        EditText inputDesc = view.findViewById(R.id.inputDescription);
        Spinner spinnerCat = view.findViewById(R.id.spinnerCategory);

        // Setup Category Spinner
        List<Category> spinnerList = new ArrayList<>();
        spinnerList.add(new Category("", "-- Tanpa Kategori --"));
        spinnerList.addAll(categoryList);
        ArrayAdapter<Category> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapterCat);

        if (isEdit) {
            inputName.setText(existingSite.getName());
            inputUrl.setText(existingSite.getUrl());
            inputDesc.setText(existingSite.getDescription());
            for (int i = 0; i < spinnerList.size(); i++) {
                if (spinnerList.get(i).getId().equals(existingSite.getCategoryId())) {
                    spinnerCat.setSelection(i);
                    break;
                }
            }
        }
        
        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String url = inputUrl.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String catId = ((Category) spinnerCat.getSelectedItem()).getId();
            
            if (!name.isEmpty() && !url.isEmpty()) {
                if (isEdit) {
                    existingSite.setName(name);
                    existingSite.setUrl(url);
                    existingSite.setDescription(desc);
                    existingSite.setCategoryId(catId);
                } else {
                    WebSite newSite = new WebSite(name, url);
                    newSite.setDescription(desc);
                    newSite.setCategoryId(catId);
                    webSiteList.add(newSite);
                }
                sortWebSites();
                saveWebSites();
                updateWebCount();
            } else {
                Toast.makeText(this, "Nama dan URL harus diisi", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void loadWebSites() {
        if (webSiteList == null) webSiteList = new ArrayList<>();
        webSiteList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SITES, null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    webSiteList.add(WebSite.fromJsonObject(array.getJSONObject(i)));
                }
                sortWebSites();
                updateWebCount();
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
        if (historyList == null) historyList = new ArrayList<>();
        historyList.clear();
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

    private void loadCategories() {
        if (categoryList == null) categoryList = new ArrayList<>();
        categoryList.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CATEGORIES, null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    categoryList.add(Category.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                android.util.Log.e("MainActivity", "Error loading categories", e);
            }
        }
    }

    private void saveCategories() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (Category cat : categoryList) {
                array.put(cat.toJsonObject());
            }
            prefs.edit().putString(KEY_CATEGORIES, array.toString()).apply();
        } catch (JSONException e) {
            android.util.Log.e("MainActivity", "Error saving categories", e);
        }
    }
}
