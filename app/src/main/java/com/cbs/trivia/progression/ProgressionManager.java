package com.cbs.trivia.progression;

import android.content.Context;
import android.content.SharedPreferences;

import com.cbs.trivia.model.StudentProfile;

import java.util.Locale;

/**
 * Computes RPG XP, streak bonuses, difficulty multipliers, player levels, class titles,
 * and run grades, while persisting cumulative progress in SharedPreferences.
 */
public class ProgressionManager {

    public static final String PREFS_NAME = "sidequest_progression_prefs";
    public static final int MAX_LEVEL = 8;

    private static final int[] LEVEL_THRESHOLDS = {0, 100, 250, 450, 700, 1000, 1400, 1900};
    private static final String[] RANK_TITLES = {
            "Novice",
            "Scout",
            "Apprentice",
            "Tactician",
            "Sentinel",
            "Vanguard",
            "Architect",
            "Grandmaster"
    };

    private static final String KEY_XP_PREFIX = "xp_";
    private static final String KEY_QUIZZES_PREFIX = "quizzes_";
    private static final String KEY_LAST_NAME = "last_profile_name";
    private static final String KEY_LAST_ID = "last_profile_id";
    private static final String KEY_LAST_GROUP = "last_profile_group";

    private final SharedPreferences prefs;

    public ProgressionManager(Context context) {
        this(context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
    }

    public ProgressionManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static double getTierMultiplier(int difficultyLevel) {
        if (difficultyLevel <= 1) {
            return 1.0;
        }
        if (difficultyLevel == 2) {
            return 1.5;
        }
        return 2.0;
    }

    public static int calculateXpEarned(
            int correctCount,
            int totalQuestions,
            int maxStreak,
            int difficultyLevel
    ) {
        int safeCorrect = Math.max(0, correctCount);
        int safeStreak = Math.max(0, maxStreak);
        int baseXp = safeCorrect * 25;
        int streakBonus = safeStreak * 10;
        int perfectBonus = (totalQuestions > 0 && safeCorrect == totalQuestions) ? 50 : 0;
        double multiplier = getTierMultiplier(difficultyLevel);
        return (int) Math.floor((baseXp + streakBonus + perfectBonus) * multiplier);
    }

    public static int getLevelForXp(int totalXp) {
        int safeXp = Math.max(0, totalXp);
        int level = 1;
        for (int i = 0; i < LEVEL_THRESHOLDS.length; i++) {
            if (safeXp >= LEVEL_THRESHOLDS[i]) {
                level = i + 1;
            } else {
                break;
            }
        }
        return level;
    }

    public static String getRankTitleForLevel(int level) {
        int clamped = Math.max(1, Math.min(MAX_LEVEL, level));
        return RANK_TITLES[clamped - 1];
    }

    public static int getXpThresholdForLevel(int level) {
        int clamped = Math.max(1, Math.min(MAX_LEVEL, level));
        return LEVEL_THRESHOLDS[clamped - 1];
    }

    public static int getNextLevelThreshold(int level) {
        int clamped = Math.max(1, Math.min(MAX_LEVEL, level));
        if (clamped >= MAX_LEVEL) {
            return LEVEL_THRESHOLDS[MAX_LEVEL - 1];
        }
        return LEVEL_THRESHOLDS[clamped];
    }

    public static String calculateRunGrade(int correctCount, int totalQuestions) {
        if (totalQuestions <= 0) {
            return "D";
        }
        double percentage = (Math.max(0, correctCount) * 100.0) / totalQuestions;
        if (percentage >= 90.0) {
            return "S";
        }
        if (percentage >= 75.0) {
            return "A";
        }
        if (percentage >= 60.0) {
            return "B";
        }
        if (percentage >= 40.0) {
            return "C";
        }
        return "D";
    }

    public static ProgressionSnapshot evaluateProgression(
            int previousTotalXp,
            int previousQuizzesCompleted,
            int correctCount,
            int totalQuestions,
            int maxStreak,
            int difficultyLevel
    ) {
        int safePrevXp = Math.max(0, previousTotalXp);
        int safePrevQuizzes = Math.max(0, previousQuizzesCompleted);

        int previousLevel = getLevelForXp(safePrevXp);
        int xpEarned = calculateXpEarned(correctCount, totalQuestions, maxStreak, difficultyLevel);
        int newTotalXp = safePrevXp + xpEarned;
        int newLevel = getLevelForXp(newTotalXp);
        boolean leveledUp = newLevel > previousLevel;

        int currentLevelFloorXp = getXpThresholdForLevel(newLevel);
        int nextLevelXp = getNextLevelThreshold(newLevel);
        String rankTitle = getRankTitleForLevel(newLevel);
        String runGrade = calculateRunGrade(correctCount, totalQuestions);

        return new ProgressionSnapshot(
                safePrevXp,
                newTotalXp,
                previousLevel,
                newLevel,
                leveledUp,
                xpEarned,
                currentLevelFloorXp,
                newTotalXp,
                nextLevelXp,
                rankTitle,
                runGrade,
                safePrevQuizzes + 1
        );
    }

    public static String normalizeStudentKey(String studentKey) {
        if (studentKey == null) {
            return "default";
        }
        String trimmed = studentKey.trim().toLowerCase(Locale.US);
        return trimmed.isEmpty() ? "default" : trimmed;
    }

    public int getTotalXp(String studentKey) {
        String key = normalizeStudentKey(studentKey);
        return Math.max(0, prefs.getInt(KEY_XP_PREFIX + key, 0));
    }

    public int getQuizzesCompleted(String studentKey) {
        String key = normalizeStudentKey(studentKey);
        return Math.max(0, prefs.getInt(KEY_QUIZZES_PREFIX + key, 0));
    }

    public ProgressionSnapshot getCurrentSnapshot(String studentKey) {
        int totalXp = getTotalXp(studentKey);
        int quizzesCompleted = getQuizzesCompleted(studentKey);
        int level = getLevelForXp(totalXp);
        int floorXp = getXpThresholdForLevel(level);
        int nextXp = getNextLevelThreshold(level);
        String rankTitle = getRankTitleForLevel(level);

        return new ProgressionSnapshot(
                totalXp,
                totalXp,
                level,
                level,
                false,
                0,
                floorXp,
                totalXp,
                nextXp,
                rankTitle,
                "-",
                quizzesCompleted
        );
    }

    public ProgressionSnapshot recordQuizCompletion(
            String studentKey,
            int correctCount,
            int totalQuestions,
            int maxStreak,
            int difficultyLevel
    ) {
        String key = normalizeStudentKey(studentKey);
        int previousXp = getTotalXp(key);
        int previousQuizzes = getQuizzesCompleted(key);

        ProgressionSnapshot snapshot = evaluateProgression(
                previousXp,
                previousQuizzes,
                correctCount,
                totalQuestions,
                maxStreak,
                difficultyLevel
        );

        prefs.edit()
                .putInt(KEY_XP_PREFIX + key, snapshot.getNewTotalXp())
                .putInt(KEY_QUIZZES_PREFIX + key, snapshot.getQuizzesCompleted())
                .apply();

        return snapshot;
    }

    public void saveLastProfile(StudentProfile profile) {
        if (profile == null) {
            return;
        }
        prefs.edit()
                .putString(KEY_LAST_NAME, profile.getFullName())
                .putString(KEY_LAST_ID, profile.getStudentId())
                .putString(KEY_LAST_GROUP, profile.getProgramGroup())
                .apply();
    }

    public StudentProfile getLastProfile() {
        String name = prefs.getString(KEY_LAST_NAME, "");
        String id = prefs.getString(KEY_LAST_ID, "");
        String group = prefs.getString(KEY_LAST_GROUP, "");
        if (name.isEmpty() && id.isEmpty() && group.isEmpty()) {
            return null;
        }
        return new StudentProfile(name, id, group);
    }
}
