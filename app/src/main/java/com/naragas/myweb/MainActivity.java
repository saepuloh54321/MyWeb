package com.naragas.myweb;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private SwitchMaterial switchRestrict;
    private String baseDomain = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        switchRestrict = findViewById(R.id.switchRestrict);
        Button btnGo = findViewById(R.id.btnGo);

        // Adjust for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // WebView settings
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

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Button listener
        btnGo.setOnClickListener(v -> {
            String input = urlInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan URL terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!input.startsWith("http://") && !input.startsWith("https://")) {
                input = "https://" + input;
            }

            try {
                Uri uri = Uri.parse(input);
                baseDomain = uri.getHost();
                if (baseDomain == null) {
                    Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                webView.loadUrl(input);
            } catch (Exception e) {
                Toast.makeText(this, "Kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
