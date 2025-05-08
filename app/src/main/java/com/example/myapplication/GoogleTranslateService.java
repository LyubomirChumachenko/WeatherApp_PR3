package com.example.myapplication;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;

public class GoogleTranslateService {
    void translateCityNameAsync(Activity activity, String cityName, String targetLang, TextView targetTextView) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        String jsonBody = String.format("{\"text\":\"%s\",\"to\":\"%s\",\"from_lang\":\"\"}",
                cityName,
                targetLang.equals("EN") ? "en" : "ru");

        RequestBody body = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .url("https://google-api31.p.rapidapi.com/gtranslate")
                .post(body)
                .addHeader("x-rapidapi-key", "7546d5bd47msh919f3edb1276fd2p1d3a71jsn392cfd6d4411")
                .addHeader("x-rapidapi-host", "google-api31.p.rapidapi.com")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.runOnUiThread(() -> {
                    targetTextView.setText(cityName);
                    Log.e("Translation", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                activity.runOnUiThread(() -> {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String translatedText = json.getString("translated_text");
                        targetTextView.setText(translatedText);
                    } catch (Exception e) {
                        targetTextView.setText(cityName);
                        Log.e("Translation", "Error parsing response: " + e.getMessage());
                    }
                });
            }
        });
    }
}