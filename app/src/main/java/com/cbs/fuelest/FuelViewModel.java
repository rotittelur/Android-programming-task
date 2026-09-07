package com.cbs.fuelest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class FuelViewModel extends ViewModel {

    public enum Step {
        WELCOME,
        VEHICLE_SELECTION,
        CUSTOM_EFFICIENCY_INPUT,
        FUEL_SELECTION,
        DISTANCE_INPUT,
        RESULT
    }

    private final MutableLiveData<Step> currentStep = new MutableLiveData<>(Step.WELCOME);
    public LiveData<Step> getCurrentStep() { return currentStep; }

    // --- Selections ---
    private final MutableLiveData<VehicleItem> selectedVehicle = new MutableLiveData<>();
    public LiveData<VehicleItem> getSelectedVehicle() { return selectedVehicle; }

    private final MutableLiveData<FuelItem> selectedFuel = new MutableLiveData<>();
    public LiveData<FuelItem> getSelectedFuel() { return selectedFuel; }

    // --- Inputs ---
    public MutableLiveData<String> distance = new MutableLiveData<>("");
    public MutableLiveData<String> customEfficiency = new MutableLiveData<>("");
    
    // --- Computed Results ---
    public MutableLiveData<String> fuelNeededResult = new MutableLiveData<>("");
    public MutableLiveData<String> estimatedCostResult = new MutableLiveData<>("");
    public MutableLiveData<String> calculationDetails = new MutableLiveData<>("");

    // --- Data Lists ---
    public List<VehicleItem> getCars() { return VehicleRepository.getCars(); }
    public List<VehicleItem> getMotorcycles() { return VehicleRepository.getMotorcycles(); }
    public List<VehicleItem> getTrucks() { return VehicleRepository.getTrucks(); }
    public List<VehicleItem> getBuses() { return VehicleRepository.getBuses(); }
    public List<VehicleItem> getCustom() { return VehicleRepository.getCustom(); }
    
    public List<FuelItem> getAvailableFuels() {
        VehicleItem vehicle = selectedVehicle.getValue();
        if (vehicle != null && vehicle.getName().equals("Custom")) {
            return VehicleRepository.getAllFuelTypes(); // Show all for custom
        }
        return VehicleRepository.getFuelsForVehicle(vehicle);
    }

    // --- Navigation ---
    public void startApp() {
        currentStep.setValue(Step.VEHICLE_SELECTION);
    }

    public void selectVehicle(VehicleItem vehicle) {
        selectedVehicle.setValue(vehicle);
        if (vehicle.getName().equals("Custom")) {
            currentStep.setValue(Step.CUSTOM_EFFICIENCY_INPUT);
        } else {
            currentStep.setValue(Step.FUEL_SELECTION);
        }
    }

    public void submitCustomEfficiency() {
        if (customEfficiency.getValue() != null && !customEfficiency.getValue().isEmpty()) {
            currentStep.setValue(Step.FUEL_SELECTION);
        }
    }

    public void selectFuel(FuelItem fuel) {
        selectedFuel.setValue(fuel);
        currentStep.setValue(Step.DISTANCE_INPUT);
    }

    public void goToDistance() {
        currentStep.setValue(Step.DISTANCE_INPUT);
    }

    public void calculate() {
        VehicleItem vehicle = selectedVehicle.getValue();
        FuelItem fuel = selectedFuel.getValue();
        String distStr = distance.getValue();

        if (vehicle == null || fuel == null || distStr == null || distStr.isEmpty()) {
            fuelNeededResult.setValue("Please complete all steps.");
            return;
        }

        try {
            double dist = Double.parseDouble(distStr);
            double eff;
            if (vehicle.getName().equals("Custom")) {
                eff = Double.parseDouble(customEfficiency.getValue());
            } else {
                eff = vehicle.getEfficiency();
            }
            double price = fuel.getPrice();

            if (eff > 0) {
                double fuelNeeded = dist / eff;
                double estimatedCost = fuelNeeded * price;

                // Set the final results
                fuelNeededResult.setValue(String.format("%.2f Liters", fuelNeeded));
                estimatedCostResult.setValue(String.format("RM %.2f", estimatedCost));

                // Build the calculation steps string
                String mathSteps = String.format(
                        "Distance: %.2f km\n" +
                                "Efficiency: %.2f km/L\n" +
                                "Fuel Price: RM %.2f/L\n\n" +
                                "Formula:\n" +
                                "%.2f km ÷ %.2f km/L = %.2f L\n" +
                                "%.2f L × RM %.2f = RM %.2f",
                        dist, eff, price,
                        dist, eff, fuelNeeded,
                        fuelNeeded, price, estimatedCost
                );
                calculationDetails.setValue(mathSteps); // Send to UI

                currentStep.setValue(Step.RESULT);
            }
        } catch (NumberFormatException e) {
            fuelNeededResult.setValue("Invalid distance entered.");
        }
    }

    public void restart() {
        selectedVehicle.setValue(null);
        selectedFuel.setValue(null);
        distance.setValue("");
        calculationDetails.setValue("");
        currentStep.setValue(Step.WELCOME);
    }
    
    public void goBack() {
        Step current = currentStep.getValue();
        if (current == null) return;
        
        switch (current) {
            case VEHICLE_SELECTION: currentStep.setValue(Step.WELCOME); break;
            case CUSTOM_EFFICIENCY_INPUT: currentStep.setValue(Step.VEHICLE_SELECTION); break;
            case FUEL_SELECTION: 
                if (selectedVehicle.getValue() != null && selectedVehicle.getValue().getName().equals("Custom")) {
                    currentStep.setValue(Step.CUSTOM_EFFICIENCY_INPUT);
                } else {
                    currentStep.setValue(Step.VEHICLE_SELECTION);
                }
                break;
            case DISTANCE_INPUT: currentStep.setValue(Step.FUEL_SELECTION); break;
            case RESULT: currentStep.setValue(Step.DISTANCE_INPUT); break;
        }
    }
}
