package com.cbs.trivia.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cbs.trivia.BuildConfig;
import com.cbs.trivia.R;
import com.cbs.trivia.model.Category;
import com.cbs.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Volley API client communicating with the self-hosted SideQuest FastAPI backend.
 */
public class TriviaApiClient {

    private static final String TAG = "TriviaApiClient";

    /**
     * Centralized API base URL configured in app/build.gradle.kts via BuildConfig.
     */
    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    public static final String REQUEST_TAG_CATEGORIES = "SIDEQUEST_CATEGORIES_REQUEST";
    public static final String REQUEST_TAG_QUESTIONS = "SIDEQUEST_QUESTIONS_REQUEST";
    public static final String REQUEST_TAG = REQUEST_TAG_QUESTIONS;
    public static final int DEFAULT_QUESTION_LIMIT = 10;

    public interface CategoriesCallback {
        void onSuccess(ArrayList<Category> categories);

        void onError(String errorMessage);
    }

    public interface QuestionsCallback {
        void onSuccess(ArrayList<Question> questions);

        void onError(String errorMessage);
    }

    public static void fetchCategories(Context context, CategoriesCallback callback) {
        Context appContext = context.getApplicationContext();
        String url = getTrimmedBaseUrl() + "/api/categories";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        ArrayList<Category> categories = parseCategories(response);
                        if (categories.isEmpty()) {
                            callback.onError(appContext.getString(R.string.quiz_error_api_code));
                        } else {
                            Log.d(TAG, "Fetched " + categories.size() + " categories.");
                            callback.onSuccess(categories);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Category JSON parsing failed", e);
                        callback.onError(appContext.getString(R.string.quiz_error_parse));
                    }
                },
                error -> {
                    Log.e(TAG, "Category fetch failed: " + error.toString());
                    callback.onError(resolveVolleyError(appContext, error));
                }
        );

        request.setTag(REQUEST_TAG_CATEGORIES);
        request.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        VolleySingleton.getInstance(appContext).addToRequestQueue(request);
    }

    public static void fetchQuestions(
            Context context,
            String categoryId,
            int level,
            int limit,
            QuestionsCallback callback
    ) {
        Context appContext = context.getApplicationContext();
        int clampedLevel = Math.max(1, Math.min(3, level));
        int safeLimit = limit > 0 ? limit : DEFAULT_QUESTION_LIMIT;
        String safeCategory = (categoryId != null && !categoryId.trim().isEmpty())
                ? categoryId.trim()
                : Category.ID_CYBERSECURITY;

        String url = getTrimmedBaseUrl()
                + "/api/questions?category=" + Uri.encode(safeCategory)
                + "&level=" + clampedLevel
                + "&limit=" + safeLimit;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        ArrayList<Question> questions = parseQuestions(response);
                        if (questions.isEmpty()) {
                            callback.onError(appContext.getString(R.string.quiz_error_api_code));
                        } else {
                            Log.d(TAG, "Fetched " + questions.size() + " questions for " + safeCategory + " lv." + clampedLevel);
                            callback.onSuccess(questions);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Questions JSON parsing failed", e);
                        callback.onError(appContext.getString(R.string.quiz_error_parse));
                    }
                },
                error -> {
                    Log.e(TAG, "Question fetch failed: " + error.toString());
                    callback.onError(resolveVolleyError(appContext, error));
                }
        );

        request.setTag(REQUEST_TAG_QUESTIONS);
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        VolleySingleton.getInstance(appContext).addToRequestQueue(request);
    }

    private static ArrayList<Category> parseCategories(JSONArray array) throws JSONException {
        ArrayList<Category> categories = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String id = obj.getString("id");
            String name = obj.getString("name");
            String subtitle = obj.optString("subtitle", "");
            int questionCount = obj.optInt("question_count", 0);
            categories.add(new Category(id, name, subtitle, questionCount));
        }
        return categories;
    }

    private static ArrayList<Question> parseQuestions(JSONObject response) throws JSONException {
        JSONArray questionsArray = response.getJSONArray("questions");
        ArrayList<Question> parsed = new ArrayList<>();

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject item = questionsArray.getJSONObject(i);
            int id = item.optInt("id", i + 1);
            String category = item.optString("category", response.optString("category", ""));
            int level = item.optInt("level", response.optInt("level", 1));
            String questionText = item.getString("question");

            JSONArray optionsArray = item.getJSONArray("options");
            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                options.add(optionsArray.getString(j));
            }

            int correctIndex = item.getInt("correct_index");
            if (correctIndex < 0 || correctIndex >= options.size()) {
                throw new JSONException("Invalid correct_index " + correctIndex + " for question id " + id);
            }

            String explanation = item.optString("explanation", "");
            parsed.add(new Question(id, category, level, questionText, options, correctIndex, explanation));
        }
        return parsed;
    }

    private static String getTrimmedBaseUrl() {
        String trimmed = BASE_URL != null ? BASE_URL.trim() : "";
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String resolveVolleyError(Context context, VolleyError error) {
        if (error instanceof TimeoutError) {
            return context.getString(R.string.quiz_error_timeout);
        }
        if (error instanceof NoConnectionError) {
            return context.getString(R.string.quiz_error_no_connection);
        }
        if (error instanceof ServerError) {
            return context.getString(R.string.quiz_error_server);
        }
        if (error instanceof NetworkError) {
            return context.getString(R.string.quiz_error_network);
        }
        return context.getString(R.string.quiz_error_default);
    }
}
