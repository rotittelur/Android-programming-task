package com.cbs.fuelest;

import java.util.ArrayList;
import java.util.List;

/**
 * Static data repository providing hardcoded vehicle presets and fuel types
 * for the Malaysian market. Contains 10 cars, 10 motorcycles, and 4 commercial
 * vehicles with their respective fuel efficiency ratings.
 */
public final class VehicleRepository {

    private VehicleRepository() {
        // Utility class — prevent instantiation
    }

    // ======================== CARS (10) ========================

    public static List<VehicleItem> getCars() {
        List<VehicleItem> cars = new ArrayList<>();
        cars.add(new VehicleItem("Perodua Axia", 22.5,
                "1.0L (Highly efficient city driving)",
                R.drawable.ic_vehicle_axia, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Perodua Bezza", 21.0,
                "1.0L / 1.3L average",
                R.drawable.ic_vehicle_bezza, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Perodua Myvi", 18.0,
                "1.3L / 1.5L average",
                R.drawable.ic_vehicle_myvi, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Nissan Almera", 18.0,
                "1.0L Turbo",
                R.drawable.ic_vehicle_almera, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Honda City", 17.5,
                "1.5L VTEC (Non-Hybrid)",
                R.drawable.ic_vehicle_city, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Toyota Vios", 17.5,
                "1.5L Dual VVT-i",
                R.drawable.ic_vehicle_vios, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Honda Civic", 15.5,
                "1.5L VTEC Turbo",
                R.drawable.ic_vehicle_civic, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Proton Saga", 14.5,
                "1.3L VVT",
                R.drawable.ic_vehicle_saga, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Proton Persona", 14.0,
                "1.6L VVT",
                R.drawable.ic_vehicle_persona, VehicleItem.Category.CAR));
        cars.add(new VehicleItem("Proton X50", 13.5,
                "1.5L Turbo (Standard/Flagship average)",
                R.drawable.ic_vehicle_x50, VehicleItem.Category.CAR));
        return cars;
    }

    // ======================== MOTORCYCLES (10) ========================

    public static List<VehicleItem> getMotorcycles() {
        List<VehicleItem> bikes = new ArrayList<>();
        bikes.add(new VehicleItem("Honda Wave Alpha", 65.0,
                "110cc Kapcai",
                R.drawable.ic_vehicle_wave_alpha, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Honda EX5", 58.0,
                "110cc Classic Kapcai",
                R.drawable.ic_vehicle_ex5, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Honda Dash 125", 55.0,
                "125cc Kapcai",
                R.drawable.ic_vehicle_dash125, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Modenas Kriss 110", 50.0,
                "110cc Kapcai",
                R.drawable.ic_vehicle_kriss110, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Yamaha LC135", 45.0,
                "135cc Kapcai",
                R.drawable.ic_vehicle_lc135, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Yamaha Y16ZR", 45.0,
                "155cc Super Moped",
                R.drawable.ic_vehicle_y16zr, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Honda RS150R / RS-X", 43.0,
                "150cc Super Moped",
                R.drawable.ic_vehicle_rs150r, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Yamaha Y15ZR (Ysuku)", 43.0,
                "150cc Super Moped",
                R.drawable.ic_vehicle_y15zr, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("Yamaha NVX", 40.0,
                "155cc Maxi-Scooter",
                R.drawable.ic_vehicle_nvx, VehicleItem.Category.MOTORCYCLE));
        bikes.add(new VehicleItem("SYM VF3i", 35.0,
                "185cc Super Moped (Largest displacement)",
                R.drawable.ic_vehicle_vf3i, VehicleItem.Category.MOTORCYCLE));
        return bikes;
    }

    // ======================== TRUCKS (2) ========================

    public static List<VehicleItem> getTrucks() {
        List<VehicleItem> trucks = new ArrayList<>();
        trucks.add(new VehicleItem("Light Truck", 6.0,
                "3-5 ton general cargo",
                R.drawable.ic_vehicle_truck_light, VehicleItem.Category.TRUCK));
        trucks.add(new VehicleItem("Heavy Truck", 3.5,
                "10+ ton long-haul freight",
                R.drawable.ic_vehicle_truck_heavy, VehicleItem.Category.TRUCK));
        return trucks;
    }

    // ======================== BUSES (2) ========================

    public static List<VehicleItem> getBuses() {
        List<VehicleItem> buses = new ArrayList<>();
        buses.add(new VehicleItem("City Bus", 3.0,
                "Urban transit, frequent stops",
                R.drawable.ic_vehicle_bus_city, VehicleItem.Category.BUS));
        buses.add(new VehicleItem("Express Bus", 4.5,
                "Interstate highway express",
                R.drawable.ic_vehicle_bus_express, VehicleItem.Category.BUS));
        return buses;
    }

    // ======================== CUSTOM (1) ========================

    public static List<VehicleItem> getCustom() {
        List<VehicleItem> custom = new ArrayList<>();
        custom.add(new VehicleItem("Custom", 0.0,
                "Enter own consumption",
                android.R.drawable.ic_menu_edit, VehicleItem.Category.CAR));
        return custom;
    }

    // ======================== COMBINED GETTERS ========================

    /**
     * Returns all "Daily Commute" vehicles (Cars + Motorcycles).
     */
    public static List<VehicleItem> getDailyCommuteVehicles() {
        List<VehicleItem> vehicles = new ArrayList<>();
        vehicles.addAll(getCars());
        vehicles.addAll(getMotorcycles());
        return vehicles;
    }

    /**
     * Returns all "Commercial" vehicles (Trucks + Buses).
     */
    public static List<VehicleItem> getCommercialVehicles() {
        List<VehicleItem> vehicles = new ArrayList<>();
        vehicles.addAll(getTrucks());
        vehicles.addAll(getBuses());
        return vehicles;
    }

    // ======================== FUEL TYPES ========================

    /**
     * Returns all available fuel types.
     */
    public static List<FuelItem> getAllFuelTypes() {
        List<FuelItem> fuels = new ArrayList<>();
        fuels.add(new FuelItem("BUDI Madani RON95", 1.99,
                R.drawable.ic_fuel_budi_ron95, false));
        fuels.add(new FuelItem("BUDI Diesel", 2.10,
                R.drawable.ic_fuel_budi_diesel, true));
        fuels.add(new FuelItem("RON95 Retail", 3.77,
                R.drawable.ic_fuel_ron95, false));
        fuels.add(new FuelItem("RON97 Retail", 4.25,
                R.drawable.ic_fuel_ron97, false));
        fuels.add(new FuelItem("Diesel Retail", 4.67,
                R.drawable.ic_fuel_diesel, true));
        return fuels;
    }

    /**
     * Returns fuel types filtered for the given vehicle.
     * Commercial vehicles (Trucks/Buses) can only use Diesel fuels.
     * Cars and Motorcycles can ONLY use petrol (RON95/RON97).
     */
    public static List<FuelItem> getFuelsForVehicle(VehicleItem vehicle) {
        if (vehicle == null) return getAllFuelTypes();

        List<FuelItem> filtered = new ArrayList<>();
        boolean needsDiesel = vehicle.isCommercial();

        for (FuelItem fuel : getAllFuelTypes()) {
            if (needsDiesel == fuel.isDiesel()) {
                filtered.add(fuel);
            }
        }
        return filtered;
    }

    /**
     * Returns only Diesel fuel types.
     */
    public static List<FuelItem> getDieselFuels() {
        List<FuelItem> dieselFuels = new ArrayList<>();
        for (FuelItem fuel : getAllFuelTypes()) {
            if (fuel.isDiesel()) {
                dieselFuels.add(fuel);
            }
        }
        return dieselFuels;
    }
}
