package com.openclassrooms.savemytrip.tripbook;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.databinding.ActivityTripBookBinding;
import com.openclassrooms.savemytrip.utils.StorageUtils;

import java.io.File;

public class TripBookActivity extends AppCompatActivity {

    private ActivityTripBookBinding binding;

    //File PURPOSE
    private static final String FILENAME = "tripBook.txt";
    private static final String FOLDERNAME = "bookTrip";

    //PERMISSION PURPOSE
    private static final int RC_STORAGE_WRITE_PERMS = 100;

    // DEFINE THE AUTORITY OF THE FILPROVIDER
    private static final String AUTHORITY="com.openclassrooms.savemytrip.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        readFromStorage();
    }

    // -------------------
    // UI
    // -------------------

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CompoundButton.OnCheckedChangeListener checkedChangeListener = (button, isChecked) -> {
            if (isChecked) {
                int id = button.getId();
                if (id == R.id.trip_book_activity_radio_internal) {
                    binding.tripBookActivityExternalChoice.setVisibility(View.GONE);
                    binding.tripBookActivityInternalChoice.setVisibility(View.VISIBLE);
                } else if (id == R.id.trip_book_activity_radio_external) {
                    binding.tripBookActivityExternalChoice.setVisibility(View.VISIBLE);
                    binding.tripBookActivityInternalChoice.setVisibility(View.GONE);
                }
            }
            readFromStorage();
        };
        binding.tripBookActivityRadioInternal.setOnCheckedChangeListener(checkedChangeListener);
        binding.tripBookActivityRadioExternal.setOnCheckedChangeListener(checkedChangeListener);
        binding.tripBookActivityRadioPrivate.setOnCheckedChangeListener(checkedChangeListener);
        binding.tripBookActivityRadioPublic.setOnCheckedChangeListener(checkedChangeListener);
        binding.tripBookActivityRadioNormal.setOnCheckedChangeListener(checkedChangeListener);
        binding.tripBookActivityRadioVolatile.setOnCheckedChangeListener(checkedChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share) {
            shareFile();
            return true;
        } else if (itemId == R.id.action_save) {
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // --------------------

    // ACTIONS

    // --------------------

    //  Save after user clicked on button

    private void save() {

        if (binding.tripBookActivityRadioExternal.isChecked()) {

            this.writeOnExternalStorage(); //Save on external storage

        }
        if (binding.tripBookActivityRadioInternal.isChecked()) {

            this.writeOnInternalStorage(); //Save on internal storage

        }

    }
    // ----------------------------------

    // UTILS - STORAGE

    // ----------------------------------

    //  Read from storage

    private void readFromStorage() {
        if (checkWriteExternalStoragePermission()) return;

        if (binding.tripBookActivityRadioExternal.isChecked()) {

            if (StorageUtils.isExternalStorageReadable()) {

                File directory;
                // EXTERNAL

                if (binding.tripBookActivityRadioPublic.isChecked()) {

                    // External - Public

                    directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                } else {

                    // External - Private

                    directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

                }

                binding.tripBookActivityEditText.setText(StorageUtils.getTextFromStorage(directory, this, FILENAME, FOLDERNAME));
            }

        }

        if (binding.tripBookActivityRadioInternal.isChecked()) {

            File directory = null;
            if (binding.tripBookActivityRadioVolatile.isChecked()) {
                directory = getCacheDir();
            }

            if (binding.tripBookActivityRadioNormal.isChecked()) {
                directory = getFilesDir();
            }
            binding.tripBookActivityEditText.setText(StorageUtils.getTextFromStorage(directory, this, FILENAME, FOLDERNAME));
        }

    }

    //  Write on external storage


    private void writeOnExternalStorage() {

        if (StorageUtils.isExternalStorageWritable()) {

            File directory;

            if (binding.tripBookActivityRadioPublic.isChecked()) {

                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            } else {

                directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            }

            StorageUtils.setTextInStorage(directory, this, FILENAME, FOLDERNAME, binding.tripBookActivityEditText.getText().toString());

        } else {

            Toast.makeText(this, getString(R.string.external_storage_impossible_create_file), Toast.LENGTH_LONG).show();

        }
    }

    //  Write on internal storage

    private void writeOnInternalStorage() {

        File directory = null;

        if (binding.tripBookActivityRadioVolatile.isChecked()) {

            directory = getCacheDir();

        }
        if (binding.tripBookActivityRadioNormal.isChecked()) {

            directory = getFilesDir();

        }

        StorageUtils.setTextInStorage(directory, this, FILENAME, FOLDERNAME, binding.tripBookActivityEditText.getText().toString());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_STORAGE_WRITE_PERMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFromStorage();
            }
        }
    }

    private boolean checkWriteExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,

                    new String[]{WRITE_EXTERNAL_STORAGE},

                    RC_STORAGE_WRITE_PERMS);

            return true;

        }

        return false;

    }

    // ----------------------------------

    // SHARE FILE

    // ----------------------------------

    // Share the internal file

    private void shareFile(){

        File internalFile = StorageUtils.getFileFromStorage(getFilesDir(),this, FILENAME, FOLDERNAME);

        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), AUTHORITY, internalFile);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);


        startActivity(Intent.createChooser(sharingIntent, getString(R.string.trip_book_share)));

    }

}
