package com.cbs.trivia.network;

import android.content.Context;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cbs.trivia.R;
import com.cbs.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class TriviaApiClient {

    private static final String TAG = "TriviaApiClient";
    private static final String TRIVIA_API_URL = "https://opentdb.com/api.php?amount=10&type=multiple";
    public static final String REQUEST_TAG = "TRIVIA_FETCH_REQUEST";

    public interface TriviaCallback {
        void onSuccess(ArrayList<Question> questions);
        void onError(String errorMessage);
    }

    public static void fetchTriviaQuestions(Context context, TriviaCallback callback) {
        Context appContext = context.getApplicationContext();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                TRIVIA_API_URL,
                null,
                response -> {
                    try {
                        int responseCode = response.getInt("response_code");
                        if (responseCode != 0) {
                            Log.e(TAG, "API returned non-zero response_code: " + responseCode);
                            callback.onError(appContext.getString(R.string.quiz_error_api_code));
                            return;
                        }

                        JSONArray resultsArray = response.getJSONArray("results");
                        ArrayList<Question> parsedQuestions = new ArrayList<>();

                        for (int i = 0; i < resultsArray.length(); i++) {
                            JSONObject item = resultsArray.getJSONObject(i);

                            String category = decodeHtml(item.getString("category"));
                            String questionText = decodeHtml(item.getString("question"));
                            String correctAnswer = decodeHtml(item.getString("correct_answer"));

                            JSONArray incorrectArray = item.getJSONArray("incorrect_answers");
                            ArrayList<String> options = new ArrayList<>();
                            options.add(correctAnswer);

                            for (int j = 0; j < incorrectArray.length(); j++) {
                                options.add(decodeHtml(incorrectArray.getString(j)));
                            }

                            Collections.shuffle(options);
                            parsedQuestions.add(new Question(category, questionText, options, correctAnswer));
                        }

                        if (parsedQuestions.isEmpty()) {
                            callback.onError(appContext.getString(R.string.quiz_error_api_code));
                        } else {
                            Log.d(TAG, "Successfully parsed " + parsedQuestions.size() + " trivia questions.");
                            callback.onSuccess(parsedQuestions);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing failed", e);
                        callback.onError(appContext.getString(R.string.quiz_error_parse));
                    }
                },
                error -> {
                    Log.e(TAG, "Volley network error: " + error.toString());
                    callback.onError(appContext.getString(R.string.quiz_error_default));
                }
        );

        request.setTag(REQUEST_TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(appContext).addToRequestQueue(request);
    }

    private static String decodeHtml(String encoded) {
        return HtmlCompat.fromHtml(encoded, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim();
    }
}