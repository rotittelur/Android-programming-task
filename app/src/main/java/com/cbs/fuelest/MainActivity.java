package com.cbs.fuelest;

import android.os.Bundle;
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

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        // Vehicle Selection
        binding.rvCars.setAdapter(new VehicleAdapter(viewModel.getCars(), viewModel));
        binding.rvMotorcycles.setAdapter(new VehicleAdapter(viewModel.getMotorcycles(), viewModel));
        binding.rvTrucks.setAdapter(new VehicleAdapter(viewModel.getTrucks(), viewModel));
        binding.rvBuses.setAdapter(new VehicleAdapter(viewModel.getBuses(), viewModel));
        binding.rvCustom.setAdapter(new VehicleAdapter(viewModel.getCustom(), viewModel));

        // Fuel Selection
        viewModel.getSelectedVehicle().observe(this, vehicle -> {
            if (vehicle != null) {
                binding.rvFuels.setAdapter(new FuelAdapter(viewModel.getAvailableFuels(), viewModel));
            }
        });
    }
}
