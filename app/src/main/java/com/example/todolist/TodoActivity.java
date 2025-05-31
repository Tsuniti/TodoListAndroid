package com.example.todolist;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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

import com.example.todolist.data.DBHelper;
import com.example.todolist.databinding.ActivityTodoBinding;
import com.example.todolist.models.Todo;

import java.time.LocalDate;
import java.util.Locale;

public class TodoActivity extends AppCompatActivity {

    public static final int INSERT = 0;
    public static final int UPDATE = 1;

    public static final String MODE = "mode";
    public static final String POSITION = "position";
    public static final String TODO = "todo";

    private int mode;
    private int todoId;

    private String action;
    private ActivityTodoBinding binding;

    private boolean sharing;

    public final ActivityResultLauncher<String> sendToPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if(result) {
                    String phone;
                    phone = binding.phoneEdit.getText().toString();
                    if(!phone.isBlank()) {
                        String phoneUrl = "smsto:" + phone;
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(phoneUrl));
                        //
                        String smsText = getString(R.string.TODO) + ":\n" +
                                getString(R.string.title) + " " + binding.titleTodoEdit.getText().toString() + "\n" +
                                getString(R.string.text) + " " + binding.textTodoEdit.getText().toString() + "\n" +
                                getString(R.string.complete_by) + " " + binding.deadlineTodoEdit.getText().toString();
                        //
                        intent.putExtra("sms_body", smsText);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Give permission to send sms", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        //Locales
        String localeTag =preferences.getString("locale", null);
        if(localeTag != null)
        {
            Locale locale = Locale.forLanguageTag(localeTag);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.setLocale(locale);
            Context context = newBase.createConfigurationContext(configuration);
            newBase = newBase.createConfigurationContext(configuration);
        }
        //
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Locales
        if (getSupportActionBar()!=null)
            getSupportActionBar().setTitle(R.string.app_name);
        //
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //
        binding = ActivityTodoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //
        Intent intent = getIntent();
        mode = intent.getIntExtra(MODE, -1);

        if (mode == UPDATE) {
            Todo todo;
            //New variant
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                todo = intent.getSerializableExtra(TODO, Todo.class);
            } else {
                //Old variant
                todo = (Todo) intent.getSerializableExtra(TODO);
            }
            if (todo != null) {
                todoId = todo.getId();
                binding.titleTodoEdit.setText(String.valueOf(todo.getTitle()));
                binding.textTodoEdit.setText(String.valueOf(todo.getText()));
                binding.deadlineTodoEdit.setText(String.valueOf(todo.getDeadline()));
                //
            }
        }
        binding.saveTodoButton.setOnClickListener(v -> {
            if (
                    ! binding.titleTodoEdit.getText().toString().isBlank() &&
                            ! binding.textTodoEdit.getText().toString().isBlank() &&
                            ! binding.deadlineTodoEdit.getText().toString().isBlank()
            ){
                Todo todo = new Todo(
                        todoId,
                        binding.titleTodoEdit.getText().toString(),
                        binding.textTodoEdit.getText().toString(),
                        LocalDate.parse(
                                binding.deadlineTodoEdit.getText().toString()
                        )
                );
                //Save in db
                try (DBHelper helper = new DBHelper(TodoActivity.this)) {
                    if (mode == INSERT)
                        helper.insert(todo);
                    else if (mode == UPDATE)
                        helper.update(todo);
                    //Save result activity
                    int position = getIntent().getIntExtra(POSITION, -1);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(POSITION, position);
                    resultIntent.putExtra(MODE, mode);
                    resultIntent.putExtra(TODO, todo);
                    //
                    setResult(RESULT_OK, resultIntent);
                    //
                    finish();

                }

            }
        });

        binding.toggleShareMenuButton.setOnClickListener(v -> {
            if(!sharing){
                sharing = true;
                binding.phoneEdit.setVisibility(VISIBLE);
                binding.sendSmsToButton.setVisibility(VISIBLE);
            } else {
                sharing = false;
                binding.phoneEdit.setVisibility(GONE);
                binding.sendSmsToButton.setVisibility(GONE);
            }
        });
        binding.sendSmsToButton.setOnClickListener(v ->
        {
            sendToPermissionLauncher.launch(Manifest.permission.SEND_SMS);
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
            editor.remove("locale").apply();
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
        else if (itemId == R.id.deLocaleMenu) {
            editor.putString("locale", Locale.forLanguageTag("de").toLanguageTag()).apply();
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