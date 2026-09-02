package com.cbs.fuelest;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class FuelViewModel extends ViewModel {

    public MutableLiveData<String> distance = new MutableLiveData<>("");
    public MutableLiveData<String> efficiency = new MutableLiveData<>("");
    public MutableLiveData<String> fuelPrice = new MutableLiveData<>("");

    public MutableLiveData<Boolean> isCustomPriceVisible = new MutableLiveData<>(false);

    public MutableLiveData<String> fuelNeededResult = new MutableLiveData<>("");
    public MutableLiveData<String> estimatedCostResult = new MutableLiveData<>("");

    public void fueltype(int type){
        switch (type){
            case 0: // BUDI RON95
                fuelPrice.setValue("1.99");
                isCustomPriceVisible.setValue(false);
                break;
            case 1: //BUDI Diesel
                fuelPrice.setValue("2.10");
                isCustomPriceVisible.setValue(false);
                break;
            case 2: // RON95 full
                fuelPrice.setValue("3.77");
                isCustomPriceVisible.setValue(false);
                break;
            case 3:// RON97 full
                fuelPrice.setValue("4.25");
                isCustomPriceVisible.setValue(false);
                break;
            case 4: // Diesel full
                fuelPrice.setValue("4.67");
                isCustomPriceVisible.setValue(false);
                break;
            case 5: // custom
                fuelPrice.setValue("");
                isCustomPriceVisible.setValue(true);
                break;
        }
    }


    public void calculate() {
        try {
            double dist = Double.parseDouble(distance.getValue());
            double eff = Double.parseDouble(efficiency.getValue());
            double price = Double.parseDouble(fuelPrice.getValue());

            if (eff > 0){
                double fuelNeeded = dist / eff;
                double estimatedCost = fuelNeeded * price;

                fuelNeededResult.setValue(String.format("Fuel Needed:%.2f Liters", fuelNeeded));
                estimatedCostResult.setValue(String.format("Estimated Cost: RM %.2f", estimatedCost));
            } else{
                fuelNeededResult.setValue("Error: Efficiency must be greater than 0");
                estimatedCostResult.setValue("");
            }
        } catch (NumberFormatException e) {
            fuelNeededResult.setValue("Please enter valid numbers in all fields.");
            estimatedCostResult.setValue("");
        }
    }
}
