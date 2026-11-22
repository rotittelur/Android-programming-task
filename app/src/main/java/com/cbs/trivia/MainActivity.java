package com.cbs.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cbs.trivia.databinding.ActivityMainBinding;
import com.cbs.trivia.model.StudentProfile;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_MainActivity";
    public static final String EXTRA_BUNDLE_SESSION = "BUNDLE_SESSION_DATA";
    public static final String KEY_STUDENT_PROFILE = "KEY_STUDENT_PROFILE";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btnStartAssessment.setOnClickListener(v -> validateAndLaunchQuiz());
    }

    private void validateAndLaunchQuiz() {
        String fullName = binding.etFullName.getText() != null
                ? binding.etFullName.getText().toString().trim() : "";
        String studentId = binding.etStudentId.getText() != null
                ? binding.etStudentId.getText().toString().trim() : "";
        String programGroup = binding.etProgramGroup.getText() != null
                ? binding.etProgramGroup.getText().toString().trim() : "";

        binding.tilFullName.setError(null);
        binding.tilStudentId.setError(null);
        binding.tilProgramGroup.setError(null);

        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.err_name_empty));
            isValid = false;
        } else if (!fullName.matches("^[a-zA-Z .'-]{3,}$")) {
            binding.tilFullName.setError(getString(R.string.err_name_invalid));
            isValid = false;
        }

        if (TextUtils.isEmpty(studentId)) {
            binding.tilStudentId.setError(getString(R.string.err_id_empty));
            isValid = false;
        } else if (!studentId.matches("^[a-zA-Z0-9-]{4,}$")) {
            binding.tilStudentId.setError(getString(R.string.err_id_invalid));
            isValid = false;
        }

        if (TextUtils.isEmpty(programGroup)) {
            binding.tilProgramGroup.setError(getString(R.string.err_group_empty));
            isValid = false;
        } else if (programGroup.length() < 2) {
            binding.tilProgramGroup.setError(getString(R.string.err_group_invalid));
            isValid = false;
        }

        if (!isValid) {
            Snackbar.make(binding.getRoot(), R.string.msg_fix_validation, Snackbar.LENGTH_SHORT).show();
            return;
        }

        StudentProfile profile = new StudentProfile(fullName, studentId, programGroup);
        Bundle payload = new Bundle();
        payload.putParcelable(KEY_STUDENT_PROFILE, profile);

        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra(EXTRA_BUNDLE_SESSION, payload);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}