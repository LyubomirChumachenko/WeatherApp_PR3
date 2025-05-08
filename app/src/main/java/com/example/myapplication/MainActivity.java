package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private TextView buttonRu;
    private TextView buttonEn;
    private String currentLanguage = "RU";
    private LinearLayout cityList;
    private TextView textPlaceholder;
    private TextView tvWeather;
    private View selectedCityView; // Храним выбранную карточку
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        buttonRu = findViewById(R.id.tvRu);
        buttonEn = findViewById(R.id.tvEn);
        ImageButton btnNext = findViewById(R.id.addCityButton);
        cityList = findViewById(R.id.cityList);
        textPlaceholder = findViewById(R.id.textPlaceholder);
        tvWeather = findViewById(R.id.tvWeather);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("ADD_CITY", false)) {
                            addCityToLayout();
                        }
                    }
                }
        );

        buttonRu.setOnClickListener(v -> switchLanguage("RU"));
        buttonEn.setOnClickListener(v -> switchLanguage("EN"));
        btnNext.setOnClickListener(v -> goToNextActivity());

        updateLanguageUI();
    }

    @SuppressLint({"Range", "StringFormatInvalid"})
    private void addCityToLayout() {
        // Добавление тестового города в базу данных
        long id = dbHelper.addCity("Красноярск", "-18°C", "12 м/с", "43");

        // Загрузка данных из базы
        try (Cursor cursor = dbHelper.getAllCities()) {
            if (cursor.moveToLast()) {
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String tem = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TEMP));
                String wind = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_VETER));
                String ikv = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IKV));

                View cityView = LayoutInflater.from(this).inflate(R.layout.city_item, cityList, false);
                TextView tvWind = cityView.findViewById(R.id.tvWind);
                TextView tvIKV = cityView.findViewById(R.id.tvIKV);
                TextView tvCity = cityView.findViewById(R.id.tvCity);
                TextView tvTemperature = cityView.findViewById(R.id.tvTemperature);
                TextView tvWindValue = cityView.findViewById(R.id.tvWindValue);
                TextView tvIKValue = cityView.findViewById(R.id.tvIKValue);

                // Установка значений в TextView
                tvCity.setText(name);
                tvTemperature.setText(tem);
                tvWindValue.setText(wind);
                tvIKValue.setText(ikv);

                String cityName = tvCity.getText().toString();

                // Вызов функции перевода
                new GoogleTranslateService().translateCityNameAsync(
                        MainActivity.this,  // Правильная передача Activity
                        cityName,
                        currentLanguage,
                        tvCity
                );

                tvWind.setText(getString(R.string.tvWind, tvWind)); // Используем строковый ресурс
                tvIKV.setText(getString(R.string.tvIKV, tvIKV));
                cityView.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));

                cityView.setOnClickListener(v -> {
                    selectedCityView = cityView;
                    showDeleteBottomSheet();
                });

                cityList.addView(cityView);
                if (textPlaceholder.getVisibility() == View.VISIBLE) {
                    textPlaceholder.setVisibility(View.GONE);
                }
            }
        }
    }

    // Показываем BottomSheet с кнопкой "Удалить"
    private void showDeleteBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialog);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.delete_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Убираем стандартный фон BottomSheet
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Находим кнопку и устанавливаем текст в зависимости от текущего языка
        Button btnDelete = bottomSheetView.findViewById(R.id.btnDelete);
        btnDelete.setText(getString(R.string.btnDelete)); // Используем строковый ресурс

        btnDelete.setOnClickListener(v -> {
            if (selectedCityView != null) {
                long id = (long) selectedCityView.getTag();
                dbHelper.deleteCity(id);
                cityList.removeView(selectedCityView);
                if (cityList.getChildCount() == 0) {
                    textPlaceholder.setVisibility(View.VISIBLE);
                }
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void switchLanguage(String language) {
        if (!language.equals(currentLanguage)) {
            currentLanguage = language;
            setAppLocale(language);
            updateLanguageUI();
            Toast.makeText(this,
                    getString(R.string.language_selected, language),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setAppLocale(String languageCode) {
        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(new Locale(languageCode));
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @SuppressLint({"StringFormatInvalid"})
    private void updateLanguageUI() {
        // Обновление кнопок выбора языка
        buttonRu.setBackgroundResource(
                currentLanguage.equals("RU") ?
                        R.drawable.selected_language_background :
                        R.drawable.unselected_language_background
        );
        buttonEn.setBackgroundResource(
                currentLanguage.equals("EN") ?
                        R.drawable.selected_language_background :
                        R.drawable.unselected_language_background
        );

        // Обновление текстовых элементов
        textPlaceholder.setText(getString(R.string.textPlaceholder));
        tvWeather.setText(getString(R.string.tvWeather));

        // Обновление полей в уже созданных карточках городов
        for (int i = 0; i < cityList.getChildCount(); i++) {
            View cityView = cityList.getChildAt(i);
            TextView tvWind = cityView.findViewById(R.id.tvWind);
            TextView tvIKV = cityView.findViewById(R.id.tvIKV);
            TextView tvCity = cityView.findViewById(R.id.tvCity);

            // Получаем текущие значения
            String windValue = tvWind.getText().toString();
            String ikvValue = tvIKV.getText().toString();
            String cityName = tvCity.getText().toString();

            // Обновляем погодные параметры
            tvWind.setText(getString(R.string.tvWind, windValue));
            tvIKV.setText(getString(R.string.tvIKV, ikvValue));

            // Вызов функции перевода
            new GoogleTranslateService().translateCityNameAsync(
                    MainActivity.this,  // Правильная передача Activity
                    cityName,
                    currentLanguage,
                    tvCity
            );
        }
    }



    private void goToNextActivity() {
        try {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("LANGUAGE", currentLanguage);
            activityResultLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка открытия карты", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}