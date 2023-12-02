package com.example.libraryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int NEW_BOOK_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_BOOK_ACTIVITY_REQUEST_CODE = 2;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BookViewModel bookViewModel;
    private Book editedBook = null;

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;
        private Book book;

        public BookHolder(LayoutInflater inflater, ViewGroup view) {
            super(inflater.inflate(R.layout.book_list_item, view, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            bookTitleTextView = itemView.findViewById(R.id.book_title);
            bookAuthorTextView = itemView.findViewById(R.id.book_author);
        }

        public void bind(Book book) {
            this.book = book;
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
        }

        @Override
        public void onClick(View v) {
            MainActivity.this.editedBook = this.book;
            Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE, book.getTitle());
            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR, book.getAuthor());
            mStartForResult.launch(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            MainActivity.this.bookViewModel.delete(this.book);
            return true;
        }
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_BOOK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Book book = new Book(data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE),
                    data.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR));
            bookViewModel.insert(book);
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.book_added),
                    Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(findViewById(R.id.coordinator_layout),
                    getString(R.string.empty_not_saved),
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }
*/
    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> books;
        private final ItemTouchHelper.Callback itemTouchHelperCallback;
        public BookAdapter(ItemTouchHelper.Callback itemTouchHelperCallback) {
            this.itemTouchHelperCallback = itemTouchHelperCallback;
        }

        @NonNull
        @Override
        public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BookHolder holder, int position) {
            if (books != null) {
                Book book = books.get(position);
                holder.bind(book);
            }
            else {
                Log.d("MainActivity", "No books");
            }
        }

        @Override
        public int getItemCount() {
            if (books != null) {
                return books.size();
            }
            else {
                return 0;
            }
        }
        void setBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }
    }
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent intent = result.getData();
                    Log.d("MainActivity", "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK && intent != null) {
                        Log.d("MainActivity", "Activity result OK");
                            if(editedBook == null) {
                                Log.d("MainActivity", "Adding new book");
                                Book book = new Book(intent.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE),
                                        intent.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR));
                                bookViewModel.insert(book);
                                Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.book_added),
                                        Snackbar.LENGTH_LONG).show();
                            }
                            else {
                                Log.d("MainActivity", "Editing existing book");
                                editedBook.setTitle(intent.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE));
                                editedBook.setAuthor(intent.getStringExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR));
                                bookViewModel.update(editedBook);
                                editedBook = null;
                                Snackbar.make(findViewById(R.id.coordinator_layout),
                                                getString(R.string.book_edited),
                                                Snackbar.LENGTH_LONG)
                                        .show();
                            }

                    }
                    else  {
                        Log.d("MainActivity", "Activity result not OK");
                        Snackbar.make(findViewById(R.id.coordinator_layout),
                                        getString(R.string.empty_not_saved),
                                        Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final BookAdapter adapter = new BookAdapter(itemTouchHelperCallBack);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        FloatingActionButton addBookButton = findViewById(R.id.add_button); //TODO
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartForResult.launch(new Intent (MainActivity.this, EditBookActivity.class));

                //Intent intent = new Intent (MainActivity.this, EditBookActivity.class);
                //startActivityForResult(intent, NEW_BOOK_ACTIVITY_REQUEST_CODE);
            }
        });
        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);
        bookViewModel.findAll().observe(this, adapter::setBooks);

    }
    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Log.d("Main", "onSwiped");
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.archived_book), Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}