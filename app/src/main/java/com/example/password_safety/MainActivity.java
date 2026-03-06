package com.example.password_safety;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    EditText passwordInput;
    TextView resultText, generatedPassword;
    ProgressBar strengthBar;
    Button checkBtn, generateBtn, copyBtn;
    CheckBox showPasswordCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordInput = findViewById(R.id.passwordInput);
        resultText = findViewById(R.id.resultText);
        generatedPassword = findViewById(R.id.generatedPassword);
        strengthBar = findViewById(R.id.strengthBar);

        checkBtn = findViewById(R.id.checkBtn);
        generateBtn = findViewById(R.id.generateBtn);
        copyBtn = findViewById(R.id.copyBtn);

        checkBtn.setOnClickListener(v -> checkPassword());
        generateBtn.setOnClickListener(v -> generatePassword());
        copyBtn.setOnClickListener(v -> copyPassword());

        showPasswordCheck = findViewById(R.id.showPasswordCheck);

        showPasswordCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordInput.setInputType(
                        android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
            } else {
                passwordInput.setInputType(
                        android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }

            passwordInput.setSelection(passwordInput.getText().length());
        });

    }

    private void checkPassword() {
        String pwd = passwordInput.getText().toString();

        if (pwd.isEmpty()) {
            resultText.setText("Adj meg egy jelszót!");
            strengthBar.setProgress(0);
            return;
        }

        int score = 0;
        StringBuilder feedback = new StringBuilder();

        if (pwd.length() >= 8) {
            score += 25;
        } else {
            feedback.append("• Legalább 8 karakter hosszú legyen\n");
        }

        if (pwd.matches(".*[A-Z].*")) {
            score += 20;
        } else {
            feedback.append("• Tartalmazzon nagybetűt\n");
        }

        if (pwd.matches(".*[a-z].*")) {
            score += 20;
        } else {
            feedback.append("• Tartalmazzon kisbetűt\n");
        }

        if (pwd.matches(".*[0-9].*")) {
            score += 20;
        } else {
            feedback.append("• Tartalmazzon számot\n");
        }

        if (pwd.matches(".*[!@#$%^&*()].*")) {
            score += 15;
        } else {
            feedback.append("• Tartalmazzon speciális karaktert\n");
        }

        strengthBar.setProgress(score);

        String strengthText;
        if (score < 40) {
            strengthText = "Gyenge jelszó\n";
        } else if (score < 70) {
            strengthText = "Közepes jelszó\n";
        } else {
            strengthText = "Erős jelszó\n";
        }

        resultText.setText(strengthText + feedback.toString());

        String crackTime = estimateCrackTime(pwd);
        resultText.append("\nBecsült feltörési idő: " + crackTime);
    }

    private String estimateCrackTime(String password) {

        int charsetSize = 0;

        if (password.matches(".*[a-z].*")) charsetSize += 26;
        if (password.matches(".*[A-Z].*")) charsetSize += 26;
        if (password.matches(".*[0-9].*")) charsetSize += 10;
        if (password.matches(".*[!@#$%^&*()].*")) charsetSize += 10;

        if (charsetSize == 0) return "Nem becsülhető";

        double combinations = Math.pow(charsetSize, password.length());

        double guessesPerSecond = 1_000_000_000; // 1 milliárd/sec
        double seconds = combinations / guessesPerSecond;

        if (seconds < 60) return "Kevesebb mint 1 perc";
        if (seconds < 3600) return (int)(seconds / 60) + " perc";
        if (seconds < 86400) return (int)(seconds / 3600) + " óra";
        if (seconds < 31536000) return (int)(seconds / 86400) + " nap";
        if (seconds < 31536000 * 1000) return (int)(seconds / 31536000) + " év";

        return "Több ezer év";
    }

    private void generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 20; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        generatedPassword.setText(sb.toString());
    }

    private void copyPassword() {
        String text = generatedPassword.getText().toString();
        if (text.isEmpty()) return;

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(
                ClipData.newPlainText("password", text));

        Toast.makeText(this, "Jelszó másolva!", Toast.LENGTH_SHORT).show();
    }
}
