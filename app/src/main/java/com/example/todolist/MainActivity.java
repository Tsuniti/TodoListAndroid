package com.example.todolist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.todolist.adapters.TodoAdapter;
import com.example.todolist.data.DBHelper;
import com.example.todolist.databinding.ActivityMainBinding;
import com.example.todolist.models.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TodoAdapter todoAdapter;

    public final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("TAG", "activityResultLauncher: " + result);
                Intent data = result.getData();
                if(result.getResultCode() ==  RESULT_OK && data != null){
                    int position = data.getIntExtra(TodoActivity.POSITION, -1);
                    int mode = data.getIntExtra(TodoActivity.MODE, -1);

                    Todo todo;
                    //New variant
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        todo = data.getSerializableExtra(TodoActivity.TODO, Todo.class);
                    } else {
                        //Old variant
                        todo = (Todo) data.getSerializableExtra(TodoActivity.TODO);
                    }
                    //
                    if(todo!= null){
                        if(mode==TodoActivity.INSERT){
                            todoAdapter.insert(todo);
                            Toast.makeText(this, "Insert Success", Toast.LENGTH_SHORT).show();
                        }
                        if(mode==TodoActivity.UPDATE){
                            todoAdapter.update(position, todo);
                            Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
    );


    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        //Locales
        String localeTag =preferences.getString("locale", Locale.ENGLISH.toLanguageTag());
        Locale locale = Locale.forLanguageTag(localeTag);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        Context context = newBase.createConfigurationContext(configuration);
        //
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Locales
        if (getSupportActionBar()!=null)
            getSupportActionBar().setTitle(R.string.app_name);
        //
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Initialization Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Change R.layout.activity_main to Binding
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Todo> list = new ArrayList<>();
        //


        try (DBHelper helper = new DBHelper(this)) {

            list = helper.selectAll();
        }

        todoAdapter = new TodoAdapter(list, activityResultLauncher);
        binding.todoRecycler.setAdapter(todoAdapter);

        binding.todoRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        ));
        //button add todo
        binding.addTodoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TodoActivity.class);
            intent.putExtra(TodoActivity.MODE, TodoActivity.INSERT);
            activityResultLauncher.launch(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        final int itemId = item.getItemId();

        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //
        if (itemId == R.id.defaultLocaleMenu) {
            editor.putString("locale", Locale.ENGLISH.toLanguageTag()).apply();
            recreate();
        }
        else if (itemId == R.id.enLocaleMenu) {
            editor.putString("locale", Locale.ENGLISH.toLanguageTag()).apply();
            recreate();

        }
        else if (itemId == R.id.ukLocaleMenu) {
            editor.putString("locale", Locale.forLanguageTag("uk").toLanguageTag()).apply();
            recreate();

        }
        else if (itemId == R.id.defaultThemeMenu) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if (itemId == R.id.lightThemeMenu) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (itemId == R.id.darkThemeMenu) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        return super.onOptionsItemSelected(item);
    }
}