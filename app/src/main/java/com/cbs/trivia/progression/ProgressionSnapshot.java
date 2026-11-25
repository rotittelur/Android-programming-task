package com.cbs.trivia.progression;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Immutable snapshot of player progression before and after a quiz run.
 */
public class ProgressionSnapshot implements Parcelable {

    private final int previousTotalXp;
    private final int newTotalXp;
    private final int previousLevel;
    private final int newLevel;
    private final boolean leveledUp;
    private final int xpEarned;
    private final int currentLevelFloorXp;
    private final int currentLevelXp;
    private final int nextLevelXp;
    private final String rankTitle;
    private final String runGrade;
    private final int quizzesCompleted;

    public ProgressionSnapshot(
            int previousTotalXp,
            int newTotalXp,
            int previousLevel,
            int newLevel,
            boolean leveledUp,
            int xpEarned,
            int currentLevelFloorXp,
            int currentLevelXp,
            int nextLevelXp,
            String rankTitle,
            String runGrade,
            int quizzesCompleted
    ) {
        this.previousTotalXp = previousTotalXp;
        this.newTotalXp = newTotalXp;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.leveledUp = leveledUp;
        this.xpEarned = xpEarned;
        this.currentLevelFloorXp = currentLevelFloorXp;
        this.currentLevelXp = currentLevelXp;
        this.nextLevelXp = nextLevelXp;
        this.rankTitle = rankTitle;
        this.runGrade = runGrade;
        this.quizzesCompleted = quizzesCompleted;
    }

    protected ProgressionSnapshot(Parcel in) {
        previousTotalXp = in.readInt();
        newTotalXp = in.readInt();
        previousLevel = in.readInt();
        newLevel = in.readInt();
        leveledUp = in.readByte() != 0;
        xpEarned = in.readInt();
        currentLevelFloorXp = in.readInt();
        currentLevelXp = in.readInt();
        nextLevelXp = in.readInt();
        rankTitle = in.readString();
        runGrade = in.readString();
        quizzesCompleted = in.readInt();
    }

    public static final Creator<ProgressionSnapshot> CREATOR = new Creator<ProgressionSnapshot>() {
        @Override
        public ProgressionSnapshot createFromParcel(Parcel in) {
            return new ProgressionSnapshot(in);
        }

        @Override
        public ProgressionSnapshot[] newArray(int size) {
            return new ProgressionSnapshot[size];
        }
    };

    public int getPreviousTotalXp() {
        return previousTotalXp;
    }

    public int getNewTotalXp() {
        return newTotalXp;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public boolean isLeveledUp() {
        return leveledUp;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public int getCurrentLevelFloorXp() {
        return currentLevelFloorXp;
    }

    public int getCurrentLevelXp() {
        return currentLevelXp;
    }

    public int getNextLevelXp() {
        return nextLevelXp;
    }

    public String getRankTitle() {
        return rankTitle;
    }

    public String getRunGrade() {
        return runGrade;
    }

    public int getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public boolean isMaxLevel() {
        return newLevel >= ProgressionManager.MAX_LEVEL;
    }

    public int getXpIntoCurrentLevel() {
        return Math.max(0, newTotalXp - currentLevelFloorXp);
    }

    public int getXpSpanForCurrentLevel() {
        if (isMaxLevel()) {
            return 1;
        }
        return Math.max(1, nextLevelXp - currentLevelFloorXp);
    }

    public float getPreviousProgressRatio() {
        if (previousLevel >= ProgressionManager.MAX_LEVEL) {
            return 1f;
        }
        int prevFloor = ProgressionManager.getXpThresholdForLevel(previousLevel);
        int prevNext = ProgressionManager.getNextLevelThreshold(previousLevel);
        int span = Math.max(1, prevNext - prevFloor);
        float ratio = (float) Math.max(0, previousTotalXp - prevFloor) / span;
        return Math.max(0f, Math.min(1f, ratio));
    }

    public float getNewProgressRatio() {
        if (isMaxLevel()) {
            return 1f;
        }
        float ratio = (float) getXpIntoCurrentLevel() / getXpSpanForCurrentLevel();
        return Math.max(0f, Math.min(1f, ratio));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(previousTotalXp);
        dest.writeInt(newTotalXp);
        dest.writeInt(previousLevel);
        dest.writeInt(newLevel);
        dest.writeByte((byte) (leveledUp ? 1 : 0));
        dest.writeInt(xpEarned);
        dest.writeInt(currentLevelFloorXp);
        dest.writeInt(currentLevelXp);
        dest.writeInt(nextLevelXp);
        dest.writeString(rankTitle);
        dest.writeString(runGrade);
        dest.writeInt(quizzesCompleted);
    }
}
