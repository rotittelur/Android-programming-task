package com.cbs.trivia;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.cbs.trivia.databinding.ActivityResultBinding;
import com.cbs.trivia.databinding.ItemResultBreakdownBinding;
import com.cbs.trivia.model.Question;
import com.cbs.trivia.model.StudentProfile;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "Lifecycle_ResultActivity";
    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);

        Bundle bundle = getIntent().getBundleExtra(QuizActivity.EXTRA_BUNDLE_RESULT);
        if (bundle != null) {
            StudentProfile profile = getParcelableCompat(bundle, MainActivity.KEY_STUDENT_PROFILE, StudentProfile.class);
            ArrayList<Question> questions = getParcelableListCompat(bundle, QuizActivity.KEY_QUESTIONS_LIST, Question.class);
            int score = bundle.getInt(QuizActivity.KEY_FINAL_SCORE, 0);

            displayAnalytics(profile, questions, score);
            populateBreakdown(questions);
        }

        binding.btnRestartAssessment.setOnClickListener(v -> restartAssessment());
    }

    private void displayAnalytics(StudentProfile profile, ArrayList<Question> questions, int score) {
        if (profile != null) {
            binding.tvStudentSummary.setText(getString(
                    R.string.result_student_info,
                    profile.getFullName(),
                    profile.getStudentId(),
                    profile.getProgramGroup()
            ));
        }

        int totalQuestions = (questions != null) ? questions.size() : 0;
        int incorrectCount = Math.max(0, totalQuestions - score);
        double percentage = (totalQuestions > 0) ? (score * 100.0) / totalQuestions : 0.0;

        binding.tvPercentageScore.setText(getString(R.string.result_percentage_format, percentage));
        binding.tvRawScore.setText(getString(R.string.result_raw_score_format, score, totalQuestions));
        binding.tvCorrectCount.setText(getString(R.string.result_correct_label, score));
        binding.tvIncorrectCount.setText(getString(R.string.result_incorrect_label, incorrectCount));

        int tierTextRes;
        if (percentage >= 80.0) {
            tierTextRes = R.string.tier_distinction;
        } else if (percentage >= 60.0) {
            tierTextRes = R.string.tier_merit;
        } else if (percentage >= 40.0) {
            tierTextRes = R.string.tier_pass;
        } else {
            tierTextRes = R.string.tier_needs_improvement;
        }
        binding.tvPerformanceTier.setText(tierTextRes);
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
            } else {
                itemBinding.ivItemStatus.setImageResource(R.drawable.ic_feedback_wrong);
                itemBinding.tvItemUserAnswer.setTextColor(ContextCompat.getColor(this, R.color.status_wrong));
            }

            binding.llBreakdownContainer.addView(itemBinding.getRoot());
        }
    }

    private void restartAssessment() {
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