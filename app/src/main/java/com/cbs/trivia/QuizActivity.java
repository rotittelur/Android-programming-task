package com.cbs.trivia;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.cbs.trivia.databinding.ActivityQuizBinding;
import com.cbs.trivia.model.Question;
import com.cbs.trivia.model.StudentProfile;
import com.cbs.trivia.network.TriviaApiClient;
import com.cbs.trivia.network.VolleySingleton;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_QuizActivity";

    public static final String EXTRA_BUNDLE_RESULT = "BUNDLE_RESULT_DATA";
    public static final String KEY_QUESTIONS_LIST = "KEY_QUESTIONS_LIST";
    public static final String KEY_FINAL_SCORE = "KEY_FINAL_SCORE";

    private static final String STATE_QUESTIONS = "STATE_QUESTIONS";
    private static final String STATE_INDEX = "STATE_INDEX";
    private static final String STATE_SCORE = "STATE_SCORE";

    private ActivityQuizBinding binding;
    private MaterialButton[] optionButtons;
    private StudentProfile studentProfile;
    private ArrayList<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Toast activeToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_quiz);
        optionButtons = new MaterialButton[]{
                binding.btnOption1,
                binding.btnOption2,
                binding.btnOption3,
                binding.btnOption4
        };

        extractStudentProfile();
        setupListeners();

        if (savedInstanceState != null) {
            ArrayList<Question> restored = getParcelableListCompat(savedInstanceState, STATE_QUESTIONS, Question.class);
            if (restored != null && !restored.isEmpty()) {
                questionList = restored;
                currentQuestionIndex = savedInstanceState.getInt(STATE_INDEX, 0);
                score = savedInstanceState.getInt(STATE_SCORE, 0);
                showQuizContent();
                renderCurrentQuestion();
                return;
            }
        }

        loadTriviaQuestions();
    }

    private void extractStudentProfile() {
        Bundle bundle = getIntent().getBundleExtra(MainActivity.EXTRA_BUNDLE_SESSION);
        if (bundle != null) {
            studentProfile = getParcelableCompat(bundle, MainActivity.KEY_STUDENT_PROFILE, StudentProfile.class);
        }
        if (studentProfile != null) {
            binding.tvStudentHeader.setText(getString(
                    R.string.quiz_student_label,
                    studentProfile.getFullName(),
                    studentProfile.getStudentId()
            ));
        }
    }

    private void setupListeners() {
        for (MaterialButton button : optionButtons) {
            button.setOnClickListener(v -> handleAnswerSelection((MaterialButton) v));
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
    }

    private void loadTriviaQuestions() {
        showLoadingState();
        TriviaApiClient.fetchTriviaQuestions(this, new TriviaApiClient.TriviaCallback() {
            @Override
            public void onSuccess(ArrayList<Question> questions) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                questionList = questions;
                currentQuestionIndex = 0;
                score = 0;
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
        });
    }

    private void renderCurrentQuestion() {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) {
            return;
        }

        Question current = questionList.get(currentQuestionIndex);
        int total = questionList.size();

        binding.tvQuestionProgress.setText(getString(R.string.quiz_progress_format, currentQuestionIndex + 1, total));
        binding.tvLiveScore.setText(getString(R.string.quiz_score_format, score));
        binding.progressQuiz.setMax(total);
        binding.progressQuiz.setProgressCompat(currentQuestionIndex + 1, true);

        binding.tvCategoryBadge.setText(current.getCategory());
        binding.tvQuestionText.setText(current.getQuestionText());

        ArrayList<String> options = current.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            MaterialButton btn = optionButtons[i];
            if (i < options.size()) {
                btn.setVisibility(View.VISIBLE);
                btn.setText(options.get(i));
                resetOptionButtonStyle(btn);
            } else {
                btn.setVisibility(View.GONE);
            }
        }

        if (currentQuestionIndex == total - 1) {
            binding.btnNextQuestion.setText(R.string.btn_view_results);
        } else {
            binding.btnNextQuestion.setText(R.string.btn_next_question);
        }

        if (current.isAnswered()) {
            applyAnsweredVisualState(current);
        } else {
            binding.ivFeedback.setImageResource(R.drawable.ic_feedback_neutral);
            binding.tvFeedbackStatus.setText(R.string.feedback_prompt_default);
            binding.tvFeedbackStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            binding.btnNextQuestion.setEnabled(false);
        }
    }

    private void handleAnswerSelection(MaterialButton clickedButton) {
        Question current = questionList.get(currentQuestionIndex);
        if (current.isAnswered()) {
            return;
        }

        String chosenAnswer = clickedButton.getText().toString();
        current.setSelectedAnswer(chosenAnswer);

        if (current.isCorrect()) {
            score++;
            binding.tvLiveScore.setText(getString(R.string.quiz_score_format, score));
            showToastFeedback(getString(R.string.toast_correct));
        } else {
            showToastFeedback(getString(R.string.toast_wrong, current.getCorrectAnswer()));
        }

        applyAnsweredVisualState(current);
    }

    private void applyAnsweredVisualState(Question question) {
        for (MaterialButton btn : optionButtons) {
            btn.setEnabled(false);
            String btnText = btn.getText().toString();

            if (btnText.equals(question.getCorrectAnswer())) {
                styleOptionButton(btn, R.color.status_correct_bg, R.color.status_correct, R.color.status_correct);
            } else if (btnText.equals(question.getSelectedAnswer()) && !question.isCorrect()) {
                styleOptionButton(btn, R.color.status_wrong_bg, R.color.status_wrong, R.color.status_wrong);
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

        binding.btnNextQuestion.setEnabled(true);
    }

    private void resetOptionButtonStyle(MaterialButton btn) {
        btn.setEnabled(true);
        styleOptionButton(btn, R.color.option_default_bg, R.color.text_primary, R.color.card_stroke);
    }

    private void styleOptionButton(MaterialButton btn, int bgColorRes, int textColorRes, int strokeColorRes) {
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, bgColorRes)));
        btn.setTextColor(ContextCompat.getColor(this, textColorRes));
        btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, strokeColorRes)));
    }

    private void showToastFeedback(String message) {
        if (activeToast != null) {
            activeToast.cancel();
        }
        activeToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        activeToast.show();
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
        Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(MainActivity.KEY_STUDENT_PROFILE, studentProfile);
        resultBundle.putParcelableArrayList(KEY_QUESTIONS_LIST, questionList);
        resultBundle.putInt(KEY_FINAL_SCORE, score);

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
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(TriviaApiClient.REQUEST_TAG);
        Log.d(TAG, "onDestroy() called");
    }
}
