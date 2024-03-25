package com.example.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseauth.databinding.ActivityFirebaseBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class FirebaseLoginActivity extends AppCompatActivity {

    private ActivityFirebaseBinding binding;

    private final ActivityResultLauncher<Intent> firebaseLoginLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onLoginResult(result)
    );


    public static Intent createIntent(Context context) {
        return new Intent(context, FirebaseLoginActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFirebaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginFirebase.setOnClickListener(v -> openFirebaseAuth());
        binding.createFirestore.setOnClickListener(v -> createFirestoreUser());
    }

    private void openFirebaseAuth() {
        Intent loginIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(getAuthProviders())
                .setTheme(R.style.Base_Theme_FirebaseAuth)
                .build();
        firebaseLoginLauncher.launch(loginIntent);
    }

    private List<AuthUI.IdpConfig> getAuthProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build()
        );
    }

    private void onLoginResult(FirebaseAuthUIAuthenticationResult result) {
        resetUser();
        if (result.getResultCode() == RESULT_OK) {
            if (result.getIdpResponse() != null) {
                String email = result.getIdpResponse().getEmail();
                String phone = result.getIdpResponse().getPhoneNumber();
                Toast.makeText(this, "Login success: email=" + email + ", phone=" + phone, Toast.LENGTH_SHORT).show();
                showUser();
            } else {
                Toast.makeText(this, "Login success: no data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Login cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetUser() {
        binding.createFirestore.setVisibility(View.GONE);
        binding.info.setText("");
    }

    private void showUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String phone = user.getPhoneNumber();
            String name = user.getDisplayName();
            String uid = user.getUid();
            binding.info.setText("Email: " + email + "\nPhone: " + phone + "\nName: " + name + "\nUID: " + uid);
            binding.createFirestore.setVisibility(View.VISIBLE);
        }
    }

    private void createFirestoreUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        User firestoreUser = new User(uid, "Example name", "contact");
        FirebaseFirestore.getInstance().collection("authusers")
                .document(uid)
                .set(firestoreUser)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Firestore creation success", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore creation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
