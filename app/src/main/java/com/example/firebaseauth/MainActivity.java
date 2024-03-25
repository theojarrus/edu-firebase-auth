package com.example.firebaseauth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseauth.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.openFirebaseLogin.setOnClickListener(v -> startActivity(FirebaseLoginActivity.createIntent(this)));
        binding.openCustomLogin.setOnClickListener(v -> startActivity(CustomLoginActivity.createIntent(this)));

        String uid = FirebaseAuth.getInstance().getUid();
        Toast.makeText(this, "Login: " + uid, Toast.LENGTH_SHORT).show();
    }
}
