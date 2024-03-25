package com.example.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseauth.databinding.ActivityCustomBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomLoginActivity extends AppCompatActivity {

    private static final String OAUTH_KEY = "182554854076-pumefabv9hkkbphemg4skt22o12ql8a6.apps.googleusercontent.com";

    private ActivityCustomBinding binding;

    private final ActivityResultLauncher<Intent> googleLoginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleGoogleLoginResult(result)
    );

    public static Intent createIntent(Context context) {
        return new Intent(context, CustomLoginActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginEmail.setOnClickListener(v -> loginWithEmail());
        binding.loginGoogle.setOnClickListener(v -> loginWithGoogle());
        binding.createFirestore.setOnClickListener(v -> createFirestoreUser());
    }

    private void loginWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail() // optional
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        Intent intent = client.getSignInIntent();
        googleLoginLauncher.launch(intent);
    }

    private void handleGoogleLoginResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                    .addOnSuccessListener(googleSignInAccount -> {
                        String tokenId = googleSignInAccount.getIdToken();
                        firebaseAuthWithGoogle(tokenId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Google error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Google error", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String tokenId) {
        AuthCredential credential = GoogleAuthProvider.getCredential(tokenId, OAUTH_KEY);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Google login success: " + authResult.getUser().getUid(), Toast.LENGTH_SHORT).show();
                    showUser();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Google login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loginWithEmail() {
        String email = binding.emailField.getText().toString();
        String password = binding.passwordField.getText().toString();
        if (validateFields(email, password)) {
            signupFirebase(email, password);
        }
    }

    public Boolean validateFields(String email, String password) {
        if (!email.contains("@")) {
            binding.emailField.setError("Wrong email");
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordField.setError("Password should not be empty");
            return false;
        }
        return true;
    }

    private void signupFirebase(String email, String password) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login success: " + authResult.getUser().getUid(), Toast.LENGTH_SHORT).show();
                    showUser();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        signinFirebase(email, password);
                    } else {
                        Toast.makeText(this, "SignUp error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signinFirebase(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login success: " + authResult.getUser().getUid(), Toast.LENGTH_SHORT).show();
                    showUser();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "SignIn error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
