package com.cbs.fuelest;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.cbs.fuelest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private FuelViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FuelViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        String[] fuelOptions = {
                "BUDI RON95 (RM1.99)",
                "BUDI Diesel (RM2.10)",
                "RON95 Full (RM3.77)",
                "RON97 Full (RM4.25)",
                "Diesel Full (RM4.67)",
                "Custom"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                fuelOptions
        );
        binding.fuelTypeSpinner.setAdapter(adapter);

        binding.fuelTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int type, long id) {
                viewModel.fueltype(type);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
}