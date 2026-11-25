package com.cbs.trivia.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Represents a quiz category offered by the SideQuest backend.
 */
public class Category implements Parcelable {

    public static final String ID_CYBERSECURITY = "cybersecurity";
    public static final String ID_COMPUTER_SCIENCE = "computer_science";
    public static final String ID_ANDROID_DEV = "android_dev";
    public static final String ID_DFIR_CRYPTO = "dfir_crypto";
    public static final String ID_LINUX_CLOUD = "linux_cloud";

    private final String id;
    private final String name;
    private final String subtitle;
    private final int questionCount;

    public Category(String id, String name, String subtitle, int questionCount) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.questionCount = questionCount;
    }

    protected Category(Parcel in) {
        id = in.readString();
        name = in.readString();
        subtitle = in.readString();
        questionCount = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    /**
     * Provides the default fallback categories while the backend response is loading or offline.
     */
    public static ArrayList<Category> getDefaultCategories() {
        ArrayList<Category> defaults = new ArrayList<>();
        defaults.add(new Category(
                ID_CYBERSECURITY,
                "Cybersecurity & Networks",
                "Protocols, web security, defense & threat analysis",
                15
        ));
        defaults.add(new Category(
                ID_COMPUTER_SCIENCE,
                "Computer Science & Systems",
                "Algorithms, data structures, OS internals & concurrency",
                15
        ));
        defaults.add(new Category(
                ID_ANDROID_DEV,
                "Android & Mobile Dev",
                "Activities, lifecycles, layouts, Volley & storage",
                15
        ));
        defaults.add(new Category(
                ID_DFIR_CRYPTO,
                "Forensics & Cryptography",
                "Incident response, hashing, ciphers & evidence integrity",
                15
        ));
        defaults.add(new Category(
                ID_LINUX_CLOUD,
                "Linux, Cloud & DevOps",
                "Shell mastery, systemd, containers, networking & cloud",
                15
        ));
        return defaults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(subtitle);
        dest.writeInt(questionCount);
    }
}
