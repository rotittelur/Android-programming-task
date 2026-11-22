package com.cbs.trivia.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Question implements Parcelable {

    private final String category;
    private final String questionText;
    private final ArrayList<String> options;
    private final String correctAnswer;
    private String selectedAnswer;

    public Question(String category, String questionText, ArrayList<String> options, String correctAnswer) {
        this.category = category;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.selectedAnswer = null;
    }

    protected Question(Parcel in) {
        category = in.readString();
        questionText = in.readString();
        options = in.createStringArrayList();
        correctAnswer = in.readString();
        selectedAnswer = in.readString();
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

    public String getCategory() {
        return category;
    }

    public String getQuestionText() {
        return questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public boolean isAnswered() {
        return selectedAnswer != null;
    }

    public boolean isCorrect() {
        return selectedAnswer != null && selectedAnswer.equals(correctAnswer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(questionText);
        dest.writeStringList(options);
        dest.writeString(correctAnswer);
        dest.writeString(selectedAnswer);
    }
}