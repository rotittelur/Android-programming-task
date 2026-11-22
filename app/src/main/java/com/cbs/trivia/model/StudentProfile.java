package com.cbs.trivia.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class StudentProfile implements Parcelable {

    private final String fullName;
    private final String studentId;
    private final String programGroup;

    public StudentProfile(String fullName, String studentId, String programGroup) {
        this.fullName = fullName;
        this.studentId = studentId;
        this.programGroup = programGroup;
    }

    protected StudentProfile(Parcel in) {
        fullName = in.readString();
        studentId = in.readString();
        programGroup = in.readString();
    }

    public static final Creator<StudentProfile> CREATOR = new Creator<StudentProfile>() {
        @Override
        public StudentProfile createFromParcel(Parcel in) {
            return new StudentProfile(in);
        }

        @Override
        public StudentProfile[] newArray(int size) {
            return new StudentProfile[size];
        }
    };

    public String getFullName() {
        return fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getProgramGroup() {
        return programGroup;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(studentId);
        dest.writeString(programGroup);
    }
}