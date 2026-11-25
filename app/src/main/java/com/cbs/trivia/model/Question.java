package com.cbs.trivia.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single trivia question returned by the SideQuest FastAPI backend.
 */
public class Question implements Parcelable {

    private final int id;
    private final String category;
    private final int level;
    private final String question;
    private final ArrayList<String> options;
    private final int correctIndex;
    private final String explanation;
    private int selectedIndex;

    public Question(
            int id,
            String category,
            int level,
            String question,
            List<String> options,
            int correctIndex,
            String explanation
    ) {
        this.id = id;
        this.category = category;
        this.level = level;
        this.question = question;
        this.options = (options != null) ? new ArrayList<>(options) : new ArrayList<>();
        this.correctIndex = correctIndex;
        this.explanation = (explanation != null) ? explanation : "";
        this.selectedIndex = -1;
    }

    protected Question(Parcel in) {
        id = in.readInt();
        category = in.readString();
        level = in.readInt();
        question = in.readString();
        ArrayList<String> readOptions = in.createStringArrayList();
        options = (readOptions != null) ? readOptions : new ArrayList<>();
        correctIndex = in.readInt();
        explanation = in.readString();
        selectedIndex = in.readInt();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public int getLevel() {
        return level;
    }

    public String getQuestion() {
        return question;
    }

    public String getQuestionText() {
        return question;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public String getCorrectAnswer() {
        if (correctIndex >= 0 && correctIndex < options.size()) {
            return options.get(correctIndex);
        }
        return "";
    }

    public String getSelectedAnswer() {
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            return options.get(selectedIndex);
        }
        return null;
    }

    public boolean isAnswered() {
        return selectedIndex >= 0 && selectedIndex < options.size();
    }

    public boolean isCorrect() {
        return isAnswered() && selectedIndex == correctIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(category);
        dest.writeInt(level);
        dest.writeString(question);
        dest.writeStringList(options);
        dest.writeInt(correctIndex);
        dest.writeString(explanation);
        dest.writeInt(selectedIndex);
    }
}
