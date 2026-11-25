package com.cbs.trivia;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.cbs.trivia.databinding.ActivityResultBinding;
import com.cbs.trivia.databinding.ItemResultBreakdownBinding;
import com.cbs.trivia.model.Category;
import com.cbs.trivia.model.Question;
import com.cbs.trivia.model.StudentProfile;
import com.cbs.trivia.progression.ProgressionManager;
import com.cbs.trivia.progression.ProgressionSnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_ResultActivity";
    private static final int XP_BAR_MAX = 1000;

    private ActivityResultBinding binding;
    private StudentProfile studentProfile;
    private String categoryId = Category.ID_CYBERSECURITY;
    private String categoryName = "Cybersecurity & Networks";
    private int difficultyLevel = 1;
    private double accuracyPercentage = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);

        Bundle bundle = getIntent().getBundleExtra(QuizActivity.EXTRA_BUNDLE_RESULT);
        if (bundle != null) {
            studentProfile = getParcelableCompat(bundle, MainActivity.KEY_STUDENT_PROFILE, StudentProfile.class);
            ArrayList<Question> questions = getParcelableListCompat(bundle, QuizActivity.KEY_QUESTIONS_LIST, Question.class);
            int score = bundle.getInt(QuizActivity.KEY_FINAL_SCORE, 0);
            int maxStreak = bundle.getInt(QuizActivity.KEY_MAX_STREAK, 0);
            categoryId = bundle.getString(MainActivity.KEY_CATEGORY_ID, Category.ID_CYBERSECURITY);
            categoryName = bundle.getString(MainActivity.KEY_CATEGORY_NAME, categoryName);
            difficultyLevel = bundle.getInt(MainActivity.KEY_DIFFICULTY_LEVEL, 1);
            ProgressionSnapshot snapshot = getParcelableCompat(
                    bundle,
                    QuizActivity.KEY_PROGRESSION_SNAPSHOT,
                    ProgressionSnapshot.class
            );

            if (snapshot == null) {
                int total = (questions != null) ? questions.size() : 0;
                snapshot = ProgressionManager.evaluateProgression(0, 0, score, total, maxStreak, difficultyLevel);
            }

            displayRpgSummary(studentProfile, questions, score, maxStreak, snapshot);
            populateBreakdown(questions);
        }

        setupActionButtons();
    }

    private void displayRpgSummary(
            StudentProfile profile,
            ArrayList<Question> questions,
            int score,
            int maxStreak,
            ProgressionSnapshot snapshot
    ) {
        int totalQuestions = (questions != null) ? questions.size() : 0;
        accuracyPercentage = (totalQuestions > 0) ? (score * 100.0) / totalQuestions : 0.0;
        int roundedAccuracy = (int) Math.round(accuracyPercentage);

        if (accuracyPercentage >= 80.0) {
            binding.tvQuestStatus.setText(R.string.quest_status_complete);
            binding.tvQuestStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_gold));
        } else if (accuracyPercentage >= 50.0) {
            binding.tvQuestStatus.setText(R.string.quest_status_cleared);
            binding.tvQuestStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_cyan));
        } else {
            binding.tvQuestStatus.setText(R.string.quest_status_failed);
            binding.tvQuestStatus.setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
        }

        binding.tvResultCategoryTag.setText(getString(
                R.string.quiz_category_level_format,
                QuizActivity.formatCategoryShortTag(categoryId),
                difficultyLevel
        ));

        binding.tvRunGrade.setText(getString(R.string.result_grade_format, snapshot.getRunGrade()));
        binding.tvAchievedLevelTitle.setText(getString(
                R.string.result_level_title_format,
                snapshot.getNewLevel(),
                snapshot.getRankTitle().toUpperCase(Locale.US)
        ));

        if (profile != null) {
            binding.tvStudentSummary.setText(getString(
                    R.string.result_student_info,
                    profile.getFullName(),
                    profile.getStudentId(),
                    profile.getProgramGroup()
            ));
        }

        if (snapshot.isMaxLevel()) {
            binding.tvXpProgressLabel.setText(getString(
                    R.string.result_xp_bar_max_format,
                    snapshot.getNewTotalXp(),
                    snapshot.getXpEarned()
            ));
        } else {
            binding.tvXpProgressLabel.setText(getString(
                    R.string.result_xp_bar_format,
                    snapshot.getCurrentLevelXp(),
                    snapshot.getNextLevelXp(),
                    snapshot.getXpEarned()
            ));
        }

        binding.tvStatScoreValue.setText(getString(R.string.stat_value_score_format, score, totalQuestions));
        binding.tvStatAccuracyValue.setText(getString(R.string.stat_value_accuracy_format, roundedAccuracy));
        binding.tvStatXpGainedValue.setText(getString(R.string.stat_value_xp_format, snapshot.getXpEarned()));
        binding.tvStatBestStreakValue.setText(getString(R.string.stat_value_streak_format, maxStreak));

        animateXpBarAndLevelUp(snapshot);
    }

    private void animateXpBarAndLevelUp(ProgressionSnapshot snapshot) {
        int startProgress = Math.round(snapshot.getPreviousProgressRatio() * XP_BAR_MAX);
        int endProgress = Math.round(snapshot.getNewProgressRatio() * XP_BAR_MAX);

        binding.pbResultXp.setMax(XP_BAR_MAX);
        binding.pbResultXp.setProgress(startProgress);

        if (snapshot.isLeveledUp()) {
            ObjectAnimator fillToMax = ObjectAnimator.ofInt(binding.pbResultXp, "progress", startProgress, XP_BAR_MAX);
            fillToMax.setDuration(450);
            fillToMax.setInterpolator(new DecelerateInterpolator());

            ObjectAnimator fillNewLevel = ObjectAnimator.ofInt(binding.pbResultXp, "progress", 0, endProgress);
            fillNewLevel.setDuration(550);
            fillNewLevel.setInterpolator(new DecelerateInterpolator());

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(fillToMax, fillNewLevel);
            set.start();

            binding.tvLevelUpBadge.setVisibility(View.VISIBLE);
            binding.tvLevelUpBadge.setAlpha(0f);
            binding.tvLevelUpBadge.setScaleX(0.75f);
            binding.tvLevelUpBadge.setScaleY(0.75f);
            binding.tvLevelUpBadge.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(350)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        } else {
            binding.tvLevelUpBadge.setVisibility(View.GONE);
            ObjectAnimator fillXp = ObjectAnimator.ofInt(binding.pbResultXp, "progress", startProgress, endProgress);
            fillXp.setDuration(650);
            fillXp.setInterpolator(new DecelerateInterpolator());
            fillXp.start();
        }
    }

    private void setupActionButtons() {
        final boolean advanceToNextLevel = accuracyPercentage >= 60.0 && difficultyLevel < 3;
        final int targetLevel = advanceToNextLevel ? (difficultyLevel + 1) : difficultyLevel;

        if (advanceToNextLevel) {
            binding.btnPlayNextOrRetry.setText(getString(R.string.btn_play_next_level, targetLevel));
        } else {
            binding.btnPlayNextOrRetry.setText(R.string.btn_retry_stage);
        }

        binding.btnPlayNextOrRetry.setOnClickListener(v -> launchQuiz(targetLevel));
        binding.btnBackToHub.setOnClickListener(v -> returnToHub());
    }

    private void launchQuiz(int targetLevel) {
        Bundle payload = new Bundle();
        payload.putParcelable(MainActivity.KEY_STUDENT_PROFILE, studentProfile);
        payload.putString(MainActivity.KEY_CATEGORY_ID, categoryId);
        payload.putString(MainActivity.KEY_CATEGORY_NAME, categoryName);
        payload.putInt(MainActivity.KEY_DIFFICULTY_LEVEL, targetLevel);

        Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
        intent.putExtra(MainActivity.EXTRA_BUNDLE_SESSION, payload);
        startActivity(intent);
        finish();
    }

    private void populateBreakdown(ArrayList<Question> questions) {
        if (questions == null) {
            return;
        }

        binding.llBreakdownContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            ItemResultBreakdownBinding itemBinding = ItemResultBreakdownBinding.inflate(
                    inflater,
                    binding.llBreakdownContainer,
                    false
            );

            itemBinding.tvItemQuestion.setText(getString(
                    R.string.breakdown_question_format,
                    i + 1,
                    question.getQuestionText()
            ));

            String userAnswer = (question.getSelectedAnswer() != null)
                    ? question.getSelectedAnswer()
                    : getString(R.string.breakdown_unanswered);

            itemBinding.tvItemUserAnswer.setText(getString(R.string.breakdown_your_answer, userAnswer));
            itemBinding.tvItemCorrectAnswer.setText(getString(R.string.breakdown_correct_answer, question.getCorrectAnswer()));

            if (question.isCorrect()) {
                itemBinding.ivItemStatus.setImageResource(R.drawable.ic_feedback_correct);
                itemBinding.tvItemUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.status_correct));
                itemBinding.tvItemCorrectAnswer.setVisibility(View.GONE);
            } else {
                itemBinding.ivItemStatus.setImageResource(R.drawable.ic_feedback_wrong);
                itemBinding.tvItemUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
                itemBinding.tvItemCorrectAnswer.setVisibility(View.VISIBLE);
            }

            String explanation = question.getExplanation();
            if (explanation != null && !explanation.trim().isEmpty()) {
                itemBinding.tvItemExplanation.setVisibility(View.VISIBLE);
                itemBinding.tvItemExplanation.setText(explanation.trim());
            } else {
                itemBinding.tvItemExplanation.setVisibility(View.GONE);
            }

            binding.llBreakdownContainer.addView(itemBinding.getRoot());
        }
    }

    private void returnToHub() {
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        Log.d(TAG, "onDestroy() called");
    }
}
