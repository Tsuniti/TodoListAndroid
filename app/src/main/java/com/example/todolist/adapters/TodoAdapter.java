package com.example.todolist.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.utils.widget.MotionButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.TodoActivity;
import com.example.todolist.R;
import com.example.todolist.data.DBHelper;
import com.example.todolist.databinding.ItemTodoBinding;
import com.example.todolist.models.Todo;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoHolder> {
    private final List<Todo> list;
    private final ActivityResultLauncher<Intent> activityResultLauncher;

    public TodoAdapter(List<Todo> list, ActivityResultLauncher<Intent> activityResultLauncher) {
        this.list = list;
        this.activityResultLauncher = activityResultLauncher;
    }

    @NonNull
    @Override
    public TodoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodoBinding binding = ItemTodoBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TodoHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoHolder holder, int position) {
        Todo todo = list.get(position);
        holder.binding.titleItem.setText(String.valueOf(todo.getTitle()));
        holder.binding.textItem.setText(String.valueOf(todo.getText()));
        holder.binding.deadlineDateItem.setText(String.valueOf(todo.getDeadline()));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void insert(Todo todo) {
        list.add(todo);
        notifyItemInserted(list.size() - 1);
    }

    public void update(int position, Todo todo) {
        list.set(position, todo);
        notifyItemChanged(position);
    }

    public void delete(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    private void showConfirmDialog(Context context, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Deleting");
        builder.setMessage("Are you sure?");
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
//            dialog.cancel();
        });
        builder.setPositiveButton("Delete", (dialog, which) -> {
            try (DBHelper helper = new DBHelper(context)) {
                Todo todo = list.get(position);
                helper.delete(todo.getId());
                delete(position);
            }
        });

        builder.setOnDismissListener(dialog -> {
            Toast.makeText(context, "Dismiss", Toast.LENGTH_SHORT).show();
        });
        builder.setOnCancelListener(dialog -> {
            Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
//        alertDialog.cancel();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#ea5555"));
        negativeButton.setTextColor(Color.GRAY);

    }

    public class TodoHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

/*        public TodoHolder(@NonNull View itemView) {
            super(itemView);
            TextView surnameItem = itemView.findViewById(R.id.surnameItem);
        }*/

        final ItemTodoBinding binding;

        public TodoHolder(@NonNull ItemTodoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), TodoActivity.class);
            //
            intent.putExtra(TodoActivity.POSITION, getAdapterPosition());
            intent.putExtra(TodoActivity.MODE, TodoActivity.UPDATE);
            intent.putExtra(TodoActivity.TODO, list.get(getAdapterPosition()));
            activityResultLauncher.launch(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            //PopupMenu
/*            PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
            Menu menu = popupMenu.getMenu();
            MenuItem delete = menu.add("Delete");
            delete.setOnMenuItemClickListener(item -> {
                showConfirmDialog(v.getContext(), getAdapterPosition());

                return true;
            });
            popupMenu.show();*/
            //PopupWindow
            Button button = new MaterialButton(v.getContext());
            button.setText("Delete");
            PopupWindow popupWindow = new PopupWindow(
                    button,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    v.getHeight(),
                    true
            );
            button.setBackgroundColor(Color.parseColor("#ea5555"));
            button.setTextColor(Color.WHITE);
            button.setTextSize(20);
            button.setOnClickListener(btn -> {
                popupWindow.dismiss();
                showConfirmDialog(v.getContext(), getAdapterPosition());
            });

            int marginRightInDp = 5;
            float density = v.getContext().getResources().getDisplayMetrics().density;
            int marginRightInPx = (int) (marginRightInDp * density + 0.5f);

            // Предварительно измеряем ширину popup-а
            popupWindow.getContentView().measure(
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
            );

            int popupWidth = popupWindow.getContentView().getMeasuredWidth();

            // Смещение от левого края v до позиции, где popup встанет справа с отступом
            int xOffset = v.getWidth() - popupWidth - marginRightInPx;

            popupWindow.showAsDropDown(v, xOffset, -v.getHeight());

            return false;
        }
    }
}
