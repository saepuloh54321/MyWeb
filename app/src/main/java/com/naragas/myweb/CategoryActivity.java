package com.naragas.myweb;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private List<WebSite> webSiteList;

    private static final String PREFS_NAME = "WebPrefs";
    private static final String KEY_SITES = "SavedSites";
    private static final String KEY_CATEGORIES = "SavedCategories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);

        // Adjust for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_category), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryList = new ArrayList<>();
        webSiteList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategories);
        View btnBack = findViewById(R.id.btnBack);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddCategory);

        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());

        loadData();

        adapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                confirmDelete(category);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Load Categories
        String catJson = prefs.getString(KEY_CATEGORIES, null);
        if (catJson != null) {
            try {
                JSONArray array = new JSONArray(catJson);
                for (int i = 0; i < array.length(); i++) {
                    categoryList.add(Category.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Load WebSites (to update references on delete)
        String siteJson = prefs.getString(KEY_SITES, null);
        if (siteJson != null) {
            try {
                JSONArray array = new JSONArray(siteJson);
                for (int i = 0; i < array.length(); i++) {
                    webSiteList.add(WebSite.fromJsonObject(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void showAddCategoryDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText input = view.findViewById(R.id.inputCategoryName);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tambah Kategori")
                .setView(view)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        categoryList.add(new Category(name));
                        saveCategories();
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText input = view.findViewById(R.id.inputCategoryName);
        input.setText(category.getName());
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Kategori")
                .setView(view)
                .setPositiveButton("Simpan", (d, w) -> {
                    category.setName(input.getText().toString().trim());
                    saveCategories();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmDelete(Category category) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Kategori")
                .setMessage("Apakah Anda yakin ingin menghapus kategori '" + category.getName() + "'?\nWebsite di dalamnya tidak akan terhapus.")
                .setPositiveButton("Hapus", (d, w) -> {
                    categoryList.remove(category);
                    for (WebSite s : webSiteList) {
                        if (s.getCategoryId().equals(category.getId())) s.setCategoryId("");
                    }
                    saveCategories();
                    saveWebSites();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
