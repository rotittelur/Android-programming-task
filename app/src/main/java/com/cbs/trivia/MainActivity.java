package com.cbs.trivia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cbs.trivia.databinding.ActivityMainBinding;
import com.cbs.trivia.model.Category;
import com.cbs.trivia.model.StudentProfile;
import com.cbs.trivia.network.TriviaApiClient;
import com.cbs.trivia.network.VolleySingleton;
import com.cbs.trivia.progression.ProgressionManager;
import com.cbs.trivia.progression.ProgressionSnapshot;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_MainActivity";

    public static final String EXTRA_BUNDLE_SESSION = "BUNDLE_SESSION_DATA";
    public static final String KEY_STUDENT_PROFILE = "KEY_STUDENT_PROFILE";
    public static final String KEY_CATEGORY_ID = "KEY_CATEGORY_ID";
    public static final String KEY_CATEGORY_NAME = "KEY_CATEGORY_NAME";
    public static final String KEY_DIFFICULTY_LEVEL = "KEY_DIFFICULTY_LEVEL";

    private static final String STATE_CATEGORY_ID = "STATE_CATEGORY_ID";
    private static final String STATE_DIFFICULTY_LEVEL = "STATE_DIFFICULTY_LEVEL";

    private ActivityMainBinding binding;
    private ProgressionManager progressionManager;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categories = Category.getDefaultCategories();
    private String selectedCategoryId = Category.ID_CYBERSECURITY;
    private int selectedDifficultyLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        progressionManager = new ProgressionManager(this);

        if (savedInstanceState != null) {
            selectedCategoryId = savedInstanceState.getString(STATE_CATEGORY_ID, Category.ID_CYBERSECURITY);
            selectedDifficultyLevel = savedInstanceState.getInt(STATE_DIFFICULTY_LEVEL, 1);
        } else {
            preloadSavedProfile();
        }

        setupCategoryRecyclerView();
        renderDifficultySelection();
        refreshPlayerHud();
        setupListeners();
        fetchCategoriesFromBackend();
    }

    private void preloadSavedProfile() {
        StudentProfile saved = progressionManager.getLastProfile();
        if (saved != null) {
            binding.etFullName.setText(saved.getFullName());
            binding.etStudentId.setText(saved.getStudentId());
            binding.etProgramGroup.setText(saved.getProgramGroup());
        }
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories, selectedCategoryId, category -> {
            selectedCategoryId = category.getId();
            categoryAdapter.setSelectedCategoryId(selectedCategoryId);
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setHasFixedSize(false);
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        binding.btnTier1.setOnClickListener(v -> selectDifficulty(1));
        binding.btnTier2.setOnClickListener(v -> selectDifficulty(2));
        binding.btnTier3.setOnClickListener(v -> selectDifficulty(3));

        binding.etStudentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshPlayerHud();
            }
        });

        binding.btnStartAssessment.setOnClickListener(v -> validateAndLaunchQuiz());
    }

    private void selectDifficulty(int level) {
        selectedDifficultyLevel = level;
        renderDifficultySelection();
    }

    private void fetchCategoriesFromBackend() {
        TriviaApiClient.fetchCategories(this, new TriviaApiClient.CategoriesCallback() {
            @Override
            public void onSuccess(ArrayList<Category> fetched) {
                if (isFinishing() || isDestroyed() || fetched == null || fetched.isEmpty()) {
                    return;
                }
                categories = fetched;
                boolean selectedStillPresent = false;
                for (Category category : categories) {
                    if (category.getId().equals(selectedCategoryId)) {
                        selectedStillPresent = true;
                        break;
                    }
                }
                if (!selectedStillPresent) {
                    selectedCategoryId = categories.get(0).getId();
                }
                categoryAdapter.submitCategories(categories, selectedCategoryId);
            }

            @Override
            public void onError(String errorMessage) {
                Log.w(TAG, "Using fallback categories; backend fetch failed: " + errorMessage);
            }
        });
    }

    private void renderDifficultySelection() {
        styleTierButton(binding.btnTier1, binding.tvTier1Title, binding.tvTier1Mult, selectedDifficultyLevel == 1);
        styleTierButton(binding.btnTier2, binding.tvTier2Title, binding.tvTier2Mult, selectedDifficultyLevel == 2);
        styleTierButton(binding.btnTier3, binding.tvTier3Title, binding.tvTier3Mult, selectedDifficultyLevel == 3);
    }

    private void styleTierButton(LinearLayout container, TextView title, TextView mult, boolean selected) {
        if (selected) {
            container.setBackgroundResource(R.drawable.bg_tier_selected);
            title.setTextColor(ContextCompat.getColor(this, R.color.accent_gold));
            mult.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            container.setBackgroundResource(R.drawable.bg_tier_default);
            title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            mult.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void refreshPlayerHud() {
        String studentId = binding.etStudentId.getText() != null
                ? binding.etStudentId.getText().toString().trim() : "";
        ProgressionSnapshot snapshot = progressionManager.getCurrentSnapshot(studentId);

        binding.tvHudLevel.setText(getString(R.string.hud_level_format, snapshot.getNewLevel()));
        binding.tvHudRank.setText(snapshot.getRankTitle().toUpperCase(Locale.US));

        if (snapshot.isMaxLevel()) {
            binding.tvHudXpText.setText(getString(R.string.hud_xp_max_format, snapshot.getNewTotalXp()));
            binding.pbHudXp.setProgress(1000);
        } else {
            binding.tvHudXpText.setText(getString(
                    R.string.hud_xp_format,
                    snapshot.getCurrentLevelXp(),
                    snapshot.getNextLevelXp()
            ));
            binding.pbHudXp.setProgress(Math.round(snapshot.getNewProgressRatio() * 1000f));
        }
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
        progressionManager.saveLastProfile(profile);

        Category activeCategory = getSelectedCategory();

        Bundle payload = new Bundle();
        payload.putParcelable(KEY_STUDENT_PROFILE, profile);
        payload.putString(KEY_CATEGORY_ID, activeCategory.getId());
        payload.putString(KEY_CATEGORY_NAME, activeCategory.getName());
        payload.putInt(KEY_DIFFICULTY_LEVEL, selectedDifficultyLevel);

        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra(EXTRA_BUNDLE_SESSION, payload);
        startActivity(intent);
    }

    private Category getSelectedCategory() {
        for (Category category : categories) {
            if (category.getId().equals(selectedCategoryId)) {
                return category;
            }
        }
        return categories.get(0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CATEGORY_ID, selectedCategoryId);
        outState.putInt(STATE_DIFFICULTY_LEVEL, selectedDifficultyLevel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlayerHud();
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
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(TriviaApiClient.REQUEST_TAG_CATEGORIES);
        Log.d(TAG, "onDestroy() called");
    }
}
