package com.cbs.fuelest;

/**
 * Data model representing a single fuel type option.
 * Stores the fuel's display information and price per liter.
 */
public class FuelItem {

    private final String name;
    private final double price; // RM per liter
    private final int drawableResId;
    private final boolean isDiesel;

    public FuelItem(String name, double price, int drawableResId, boolean isDiesel) {
        this.name = name;
        this.price = price;
        this.drawableResId = drawableResId;
        this.isDiesel = isDiesel;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public boolean isDiesel() {
        return isDiesel;
    }

    /**
     * Returns a formatted price string like "RM 1.99/L".
     */
    public String getFormattedPrice() {
        return String.format("RM %.2f/L", price);
    }
}
