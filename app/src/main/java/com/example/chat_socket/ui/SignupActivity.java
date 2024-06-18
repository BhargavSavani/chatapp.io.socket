package com.example.chat_socket.ui;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chat_socket.R;
import com.example.chat_socket.service.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.activity.EdgeToEdge;


public class SignupActivity extends AppCompatActivity {


    private EditText edtFName, edtLName, edtPNumber, edtEmail, edtPassword;
    private Button signupButton;
    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private TextView tvLogin;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private CircleImageView cviv;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        cviv = findViewById(R.id.cviv);
        edtFName = findViewById(R.id.edtFName);
        edtLName = findViewById(R.id.edtLName);
        edtPNumber = findViewById(R.id.edtPNumber);
        edtEmail = findViewById(R.id.edtEmail1);
        edtPassword = findViewById(R.id.edtPassword1);
        signupButton = findViewById(R.id.Signup);
        tvLogin = findViewById(R.id.Login1);

        cviv.setOnClickListener(v -> checkAndRequestPermissions());

        cviv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for storage permissions
                if (ContextCompat.checkSelfPermission(SignupActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SignupActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    openGallery();
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = edtFName.getText().toString().trim();
                String lName = edtLName.getText().toString().trim();
                String pNumber = edtPNumber.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (!fName.isEmpty() && !lName.isEmpty() && !pNumber.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    if (selectedImageUri != null) {
                        performSignup(fName, lName, pNumber, email, password, selectedImageUri);
                    } else {
                        Toast.makeText(SignupActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performSignup(String fName, String lName, String pNumber, String email, String password, Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getApiService();

        // Create RequestBody instances for text fields
        RequestBody fNameBody = RequestBody.create(MediaType.parse("text/plain"), fName);
        RequestBody lNameBody = RequestBody.create(MediaType.parse("text/plain"), lName);
        RequestBody pNumberBody = RequestBody.create(MediaType.parse("text/plain"), pNumber);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

        // Convert image Uri to File
        File imageFile = new File(getRealPathFromURI(imageUri));
        if (!imageFile.exists()) {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        Call<ResponseBody> call = apiService.uploadImage(body, fNameBody, lNameBody, pNumberBody, emailBody, passwordBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("SignUpActivity", "onFailure: " + t.getMessage());
            }

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String message = json.getString("message");

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignupActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.apply();

                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException | IOException e) {
                        runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    try {
                        JSONObject json = new JSONObject(response.errorBody().string());
                        String error = json.getString("error");
                        runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Signup Error: " + error, Toast.LENGTH_LONG).show());
                    } catch (JSONException | IOException e) {
                        runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            if (data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        cviv.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }
}