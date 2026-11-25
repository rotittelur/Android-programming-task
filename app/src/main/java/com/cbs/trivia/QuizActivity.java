package com.cbs.trivia;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;

import com.cbs.trivia.databinding.ActivityQuizBinding;
import com.cbs.trivia.model.Category;
import com.cbs.trivia.model.Question;
import com.cbs.trivia.model.StudentProfile;
import com.cbs.trivia.network.TriviaApiClient;
import com.cbs.trivia.network.VolleySingleton;
import com.cbs.trivia.progression.ProgressionManager;
import com.cbs.trivia.progression.ProgressionSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_QuizActivity";

    public static final String EXTRA_BUNDLE_RESULT = "BUNDLE_RESULT_DATA";
    public static final String KEY_QUESTIONS_LIST = "KEY_QUESTIONS_LIST";
    public static final String KEY_FINAL_SCORE = "KEY_FINAL_SCORE";
    public static final String KEY_MAX_STREAK = "KEY_MAX_STREAK";
    public static final String KEY_PROGRESSION_SNAPSHOT = "KEY_PROGRESSION_SNAPSHOT";

    private static final String STATE_QUESTIONS = "STATE_QUESTIONS";
    private static final String STATE_INDEX = "STATE_INDEX";
    private static final String STATE_SCORE = "STATE_SCORE";
    private static final String STATE_CURRENT_STREAK = "STATE_CURRENT_STREAK";
    private static final String STATE_MAX_STREAK = "STATE_MAX_STREAK";

    private ActivityQuizBinding binding;
    private ProgressionManager progressionManager;
    private LinearLayout[] optionRows;
    private TextView[] optionLetters;
    private TextView[] optionTexts;
    private ImageView[] optionStatusIcons;

    private StudentProfile studentProfile;
    private String categoryId = Category.ID_CYBERSECURITY;
    private String categoryName = "Cybersecurity & Networks";
    private int difficultyLevel = 1;

    private ArrayList<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_quiz);
        applySystemBarInsets();
        progressionManager = new ProgressionManager(this);

        optionRows = new LinearLayout[]{
                binding.optionRow1,
                binding.optionRow2,
                binding.optionRow3,
                binding.optionRow4
        };
        optionLetters = new TextView[]{
                binding.tvOptionLetter1,
                binding.tvOptionLetter2,
                binding.tvOptionLetter3,
                binding.tvOptionLetter4
        };
        optionTexts = new TextView[]{
                binding.tvOptionText1,
                binding.tvOptionText2,
                binding.tvOptionText3,
                binding.tvOptionText4
        };
        optionStatusIcons = new ImageView[]{
                binding.ivOptionStatus1,
                binding.ivOptionStatus2,
                binding.ivOptionStatus3,
                binding.ivOptionStatus4
        };

        extractSessionExtras();
        setupListeners();

        if (savedInstanceState != null) {
            ArrayList<Question> restored = getParcelableListCompat(savedInstanceState, STATE_QUESTIONS, Question.class);
            if (restored != null && !restored.isEmpty()) {
                questionList = restored;
                currentQuestionIndex = savedInstanceState.getInt(STATE_INDEX, 0);
                score = savedInstanceState.getInt(STATE_SCORE, 0);
                currentStreak = savedInstanceState.getInt(STATE_CURRENT_STREAK, 0);
                maxStreak = savedInstanceState.getInt(STATE_MAX_STREAK, 0);
                showQuizContent();
                renderCurrentQuestion();
                return;
            }
        }

        loadTriviaQuestions();
    }

    private void applySystemBarInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(binding.rootLayout);
    }

    private void extractSessionExtras() {
        Bundle bundle = getIntent().getBundleExtra(MainActivity.EXTRA_BUNDLE_SESSION);
        if (bundle != null) {
            studentProfile = getParcelableCompat(bundle, MainActivity.KEY_STUDENT_PROFILE, StudentProfile.class);
            categoryId = bundle.getString(MainActivity.KEY_CATEGORY_ID, Category.ID_CYBERSECURITY);
            categoryName = bundle.getString(MainActivity.KEY_CATEGORY_NAME, categoryName);
            difficultyLevel = bundle.getInt(MainActivity.KEY_DIFFICULTY_LEVEL, 1);
        }

        if (studentProfile != null) {
            binding.tvStudentHeader.setText(getString(
                    R.string.quiz_student_label,
                    studentProfile.getFullName(),
                    studentProfile.getStudentId()
            ));
        }

        binding.tvCategoryLevelPill.setText(getString(
                R.string.quiz_category_level_format,
                formatCategoryShortTag(categoryId),
                difficultyLevel
        ));
        binding.tvLoadingSubtitle.setText(getString(
                R.string.quiz_loading_subtitle,
                categoryName,
                difficultyLevel
        ));
    }

    public static String formatCategoryShortTag(String categoryId) {
        if (Category.ID_CYBERSECURITY.equals(categoryId)) {
            return "CYBER";
        }
        if (Category.ID_COMPUTER_SCIENCE.equals(categoryId)) {
            return "COMPSCI";
        }
        if (Category.ID_ANDROID_DEV.equals(categoryId)) {
            return "ANDROID";
        }
        if (Category.ID_DFIR_CRYPTO.equals(categoryId)) {
            return "DFIR";
        }
        if (Category.ID_LINUX_CLOUD.equals(categoryId)) {
            return "LINUX";
        }
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return "QUEST";
        }
        return categoryId.trim().toUpperCase(Locale.US);
    }

    private void setupListeners() {
        for (int i = 0; i < optionRows.length; i++) {
            final int optionIndex = i;
            optionRows[i].setOnClickListener(v -> handleAnswerSelection(optionIndex));
        }

        binding.btnNextQuestion.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                renderCurrentQuestion();
            } else {
                navigateToResults();
            }
        });

        binding.btnRetry.setOnClickListener(v -> loadTriviaQuestions());
        binding.btnBackToHubError.setOnClickListener(v -> finish());
    }

    private void loadTriviaQuestions() {
        showLoadingState();
        TriviaApiClient.fetchQuestions(
                this,
                categoryId,
                difficultyLevel,
                TriviaApiClient.DEFAULT_QUESTION_LIMIT,
                new TriviaApiClient.QuestionsCallback() {
                    @Override
                    public void onSuccess(ArrayList<Question> questions) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        questionList = questions;
                        currentQuestionIndex = 0;
                        score = 0;
                        currentStreak = 0;
                        maxStreak = 0;
                        showQuizContent();
                        renderCurrentQuestion();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        showErrorState(errorMessage);
                    }
                }
        );
    }

    private void renderCurrentQuestion() {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) {
            return;
        }

        Question current = questionList.get(currentQuestionIndex);
        int total = questionList.size();

        binding.tvQuestionProgress.setText(getString(R.string.quiz_progress_format, currentQuestionIndex + 1, total));
        binding.tvStreakBadge.setText(getString(R.string.quiz_streak_format, currentStreak));
        binding.progressQuiz.setMax(total);
        binding.progressQuiz.setProgressCompat(currentQuestionIndex + 1, true);

        binding.tvQuestionText.setText(current.getQuestionText());

        List<String> options = current.getOptions();
        for (int i = 0; i < optionRows.length; i++) {
            if (i < options.size()) {
                optionRows[i].setVisibility(View.VISIBLE);
                optionTexts[i].setText(options.get(i));
                resetOptionRowStyle(i);
            } else {
                optionRows[i].setVisibility(View.GONE);
            }
        }

        if (currentQuestionIndex == total - 1) {
            binding.btnNextQuestion.setText(R.string.btn_view_results);
        } else {
            binding.btnNextQuestion.setText(R.string.btn_next_question);
        }

        if (current.isAnswered()) {
            applyAnsweredVisualState(current, false);
        } else {
            binding.layoutExplanation.setVisibility(View.GONE);
            binding.btnNextQuestion.setEnabled(false);
        }
    }

    private void handleAnswerSelection(int chosenIndex) {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) {
            return;
        }

        Question current = questionList.get(currentQuestionIndex);
        if (current.isAnswered()) {
            return;
        }

        current.setSelectedIndex(chosenIndex);

        if (current.isCorrect()) {
            score++;
            currentStreak++;
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak;
            }
        } else {
            currentStreak = 0;
        }

        binding.tvStreakBadge.setText(getString(R.string.quiz_streak_format, currentStreak));
        applyAnsweredVisualState(current, true);
    }

    private void applyAnsweredVisualState(Question question, boolean animateExplanation) {
        int correctIdx = question.getCorrectIndex();
        int selectedIdx = question.getSelectedIndex();

        for (int i = 0; i < optionRows.length; i++) {
            optionRows[i].setClickable(false);
            optionRows[i].setFocusable(false);

            if (i == correctIdx) {
                optionRows[i].setBackgroundResource(R.drawable.bg_option_correct);
                optionTexts[i].setTextColor(ContextCompat.getColor(this, R.color.status_correct));
                optionLetters[i].setTextColor(ContextCompat.getColor(this, R.color.status_correct));
                optionStatusIcons[i].setImageResource(R.drawable.ic_feedback_correct);
                optionStatusIcons[i].setVisibility(View.VISIBLE);
            } else if (i == selectedIdx && !question.isCorrect()) {
                optionRows[i].setBackgroundResource(R.drawable.bg_option_wrong);
                optionTexts[i].setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
                optionLetters[i].setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
                optionStatusIcons[i].setImageResource(R.drawable.ic_feedback_wrong);
                optionStatusIcons[i].setVisibility(View.VISIBLE);
            } else {
                optionTexts[i].setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                optionStatusIcons[i].setVisibility(View.GONE);
            }
        }

        if (question.isCorrect()) {
            binding.ivFeedback.setImageResource(R.drawable.ic_feedback_correct);
            binding.tvFeedbackStatus.setText(R.string.feedback_correct_label);
            binding.tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.status_correct));
        } else {
            binding.ivFeedback.setImageResource(R.drawable.ic_feedback_wrong);
            binding.tvFeedbackStatus.setText(getString(R.string.feedback_wrong_label, question.getCorrectAnswer()));
            binding.tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
        }

        String explanation = question.getExplanation();
        if (explanation == null || explanation.trim().isEmpty()) {
            binding.tvExplanationText.setText(R.string.explanation_default);
        } else {
            binding.tvExplanationText.setText(explanation.trim());
        }

        if (animateExplanation) {
            binding.layoutExplanation.setAlpha(0f);
            binding.layoutExplanation.setVisibility(View.VISIBLE);
            binding.layoutExplanation.animate().alpha(1f).setDuration(180).start();
        } else {
            binding.layoutExplanation.setAlpha(1f);
            binding.layoutExplanation.setVisibility(View.VISIBLE);
        }

        binding.btnNextQuestion.setEnabled(true);
    }

    private void resetOptionRowStyle(int index) {
        optionRows[index].setClickable(true);
        optionRows[index].setFocusable(true);
        optionRows[index].setBackgroundResource(R.drawable.bg_option_default);
        optionLetters[index].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        optionTexts[index].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        optionStatusIcons[index].setVisibility(View.GONE);
    }

    private void showLoadingState() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutQuizContent.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.layoutQuizContent.setVisibility(View.GONE);
        binding.tvErrorMessage.setText(message);
    }

    private void showQuizContent() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.layoutQuizContent.setVisibility(View.VISIBLE);
    }

    private void navigateToResults() {
        String studentKey = (studentProfile != null) ? studentProfile.getStudentId() : "default";
        ProgressionSnapshot snapshot = progressionManager.recordQuizCompletion(
                studentKey,
                score,
                questionList.size(),
                maxStreak,
                difficultyLevel
        );

        Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(MainActivity.KEY_STUDENT_PROFILE, studentProfile);
        resultBundle.putParcelableArrayList(KEY_QUESTIONS_LIST, questionList);
        resultBundle.putInt(KEY_FINAL_SCORE, score);
        resultBundle.putInt(KEY_MAX_STREAK, maxStreak);
        resultBundle.putString(MainActivity.KEY_CATEGORY_ID, categoryId);
        resultBundle.putString(MainActivity.KEY_CATEGORY_NAME, categoryName);
        resultBundle.putInt(MainActivity.KEY_DIFFICULTY_LEVEL, difficultyLevel);
        resultBundle.putParcelable(KEY_PROGRESSION_SNAPSHOT, snapshot);

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra(EXTRA_BUNDLE_RESULT, resultBundle);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_QUESTIONS, questionList);
        outState.putInt(STATE_INDEX, currentQuestionIndex);
        outState.putInt(STATE_SCORE, score);
        outState.putInt(STATE_CURRENT_STREAK, currentStreak);
        outState.putInt(STATE_MAX_STREAK, maxStreak);
    }

    @SuppressWarnings("deprecation")
    private <T extends Parcelable> T getParcelableCompat(Bundle bundle, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(key, clazz);
        }
        return bundle.getParcelable(key);
    }

    @SuppressWarnings("deprecation")
    private <T extends Parcelable> ArrayList<T> getParcelableListCompat(Bundle bundle, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelableArrayList(key, clazz);
        }
        return bundle.getParcelableArrayList(key);
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
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(TriviaApiClient.REQUEST_TAG_QUESTIONS);
        Log.d(TAG, "onDestroy() called");
    }
}
