# Project Title

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Version](https://img.shields.io/badge/version-1.0.0-green.svg)

---

# Smart Learning Hub 🎓

An interactive Android Trivia & Quiz application built in Java. The app fetches live quiz questions across multiple categories and difficulty levels, allowing users to test their knowledge, track their scores, and review their answers.

---

## 🛠️ Tech Stack & Technologies Used

* **Language:** Java (Android SDK)
* **IDE:** Android Studio
* **Networking:** [Volley](https://developer.android.com/training/volley) (`JsonObjectRequest` for asynchronous HTTP calls)
* **API:** [Open Trivia Database (OpenTDB)](https://opentdb.com/)
* **UI Components:** Android XML Layouts, Material Design Components (`CardView`, `Button`, `ProgressBar`, `TextView`)
* **JSON Parsing:** Native `org.json` (`JSONObject`, `JSONArray`)

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** (Electric Eel or newer recommended)
* **JDK:** 11 or 17
* **Android SDK:** API Level 24+ (Android 7.0+)

## 🔑 API & Key Implementation Guide

### Default API (Open Trivia DB)
By default, this app uses the **Open Trivia Database (`opentdb.com`)**, which is a **free REST API that requires NO API key**. The app works out of the box after cloning!

---

### Where to Update or Add an API Key
If you want to switch to a different trivia provider (e.g., API Ninjas, QuizAPI.io) or add custom API authentication, all network request configurations are centralized in **one file**:

📍 **File Path:**  
`app/src/main/java/com/gmi/smartlearninghub/network/TriviaApiClient.java`

#### 1. Adding an API Key via Query Parameter
If your API requires the key inside the URL endpoint, modify the `TRIVIA_API_URL` constant:

```java
public class TriviaApiClient {
    // Replace with your endpoint and API key
    private static final String TRIVIA_API_URL = 
        "https://your-api-provider.com/v1/questions?amount=10&apiKey=YOUR_API_KEY_HERE";
}
```

#### 2. Adding an API Key via HTTP Headers (Recommended for Secure APIs)
If your API requires an authorization header (e.g., `X-Api-Key` or `Bearer Token`), override `getHeaders()` inside the `JsonObjectRequest` in `TriviaApiClient.java`:

```java
JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.GET,
        TRIVIA_API_URL,
        null,
        response -> {
            // Process response
        },
        error -> {
            // Handle error
        }
) {
    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Api-Key", "YOUR_API_KEY_HERE");
        // Or for Bearer tokens:
        // headers.put("Authorization", "Bearer YOUR_TOKEN_HERE");
        return headers;
    }
};
```

#### 3. Updating JSON Parsing (If Changing APIs)
If you switch to a different provider, remember to update the JSON keys inside `TriviaApiClient.java` to match your new API's response keys:
* `"results"` -> Main list array
* `"question"` -> Question text
* `"correct_answer"` -> Correct option
* `"incorrect_answers"` -> Wrong options array

---

## 📂 Key Project Structure

```text
app/src/main/java/com/gmi/smartlearninghub/
├── data/
│   └── Question.java            # Model class for trivia questions
├── network/
│   └── TriviaApiClient.java     # Network manager for API requests & Volley queue
└── ui/
    ├── MainActivity.java        # Home screen & category selection
    └── QuizActivity.java        # Quiz interface & question flow
```

---

## 🤝 Contributing & Customization

1. Fork or branch off from `main`.
2. Make your UI or logic improvements in `QuizActivity` or `TriviaApiClient`.
3. Test network edge cases (e.g., offline state, slow network).
4. Create a Pull Request!

---

## 📄 License

This project is open source under the MIT License.