package com.cbs.fuelest;

public class VehicleItem {

    public enum Category {
        CAR,
        MOTORCYCLE,
        TRUCK,
        BUS
    }

    private final String name;
    private final double efficiency; // km/L
    private final String notes;
    private final int drawableResId;
    private final Category category;

    public VehicleItem(String name, double efficiency, String notes,
                       int drawableResId, Category category) {
        this.name = name;
        this.efficiency = efficiency;
        this.notes = notes;
        this.drawableResId = drawableResId;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public String getNotes() {
        return notes;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public Category getCategory() {
        return category;
    }

    /**
     * Returns true if this vehicle is a commercial vehicle (Truck or Bus).
     */
    public boolean isCommercial() {
        return category == Category.TRUCK || category == Category.BUS;
    }
}
