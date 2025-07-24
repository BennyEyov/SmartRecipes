package com.example.smartrecipes.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartrecipes.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerRedirectButton;
    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerRedirectButton = view.findViewById(R.id.registerRedirectButton);

        // Login
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to RegisterFragment
        registerRedirectButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_loginFragment_to_registerFragment));

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_loginFragment_to_homeFragment);
                    } else {
                        Toast.makeText(getContext(),
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        resetFields();
    }

    private void resetFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
    }

}
