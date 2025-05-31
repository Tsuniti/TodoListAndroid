package com.example.todolist.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.todolist.models.Todo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {
    private final String TAG = "TAG";

    private final String TABLE = "table_todos";
    private final String ID = "id";
    private final String TITLE = "title";
    private final String TEXT = "text";
    private final String DEADLINE = "deadline";

    public DBHelper(@Nullable Context context) {
        super(context, "todos_db", null, 1);
        createTable();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private ContentValues convertToContentValues(Todo todo) {
        ContentValues values = new ContentValues();
        values.put(TITLE, todo.getTitle());
        values.put(TEXT, todo.getText());
        values.put(DEADLINE, String.valueOf(todo.getDeadline()));
        return values;
    }

    @SuppressLint("Range")
    private Todo convertToTodo(Cursor cursor) {
        return new Todo(
                cursor.getInt(cursor.getColumnIndex(ID)),
                cursor.getString(cursor.getColumnIndex(TITLE)),
                cursor.getString(cursor.getColumnIndex(TEXT)),
                LocalDate.parse(
                        cursor.getString(cursor.getColumnIndex(DEADLINE))
                )
        );
    }

    public void createTable() {
        String sql = "create table if not exists %s(%s integer primary key autoincrement, %s text, %s text, %s text)";
        sql = String.format(sql, TABLE, ID, TITLE, TEXT, DEADLINE);
        getWritableDatabase().execSQL(sql);
        Log.d(TAG, "createTable: ");
    }

    public void dropTable() {
        String sql = "drop table if exists  " + TABLE;
        getWritableDatabase().execSQL(sql);
        Log.d(TAG, "dropTable: ");
    }

    public List<Todo> selectAll() {
        List<Todo> list = new ArrayList<>();
        String sql = "select * from  " + TABLE;
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, null)) {
            Todo todo;
            while (cursor.moveToNext()) {
                todo = convertToTodo(cursor);
                list.add(todo);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "selectAll: ", e);
        }
        return list;
    }

    //selectById
    public Todo select(int todoId) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE,
                null,
                ID + "=?",
                new String[]{String.valueOf(todoId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToNext()) {
                return convertToTodo(cursor);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "select: ", e);
        }
        return null;
    }

    public Todo insert(Todo todo) {
        ContentValues values = convertToContentValues(todo);
        long id = getWritableDatabase().insert(TABLE, null, values);
        return select((int) id);
    }

    public void insertAll(List<Todo> todoList) {
        for (Todo todo : todoList) {
            insert(todo);
        }
    }

    //deleteById
    public boolean delete(int todoId) {
        int count = getWritableDatabase().delete(
                TABLE,
                ID + "=?",
                new String[]{String.valueOf(todoId)}
        );
        return count > 0;
    }

    public boolean update(Todo todo) {
        int count = getWritableDatabase().update(
                TABLE,
                convertToContentValues(todo),
                ID + "=?",
                new String[]{String.valueOf(todo.getId())}
        );
        return count > 0;
    }
}
