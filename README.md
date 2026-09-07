# Petrol Nice (PBL1) 🚗⛽

**Petrol Nice** is a sleek, native Android application engineered to help Malaysian motorists accurately forecast their travel expenses and fuel consumption. Built with a modern dark and teal Material Design interface, the app transforms a standard calculator into a dynamic, multi-step interactive wizard.

## Features

* **Multi-Step Interactive Wizard:** A seamless, state-driven user flow managed entirely within a single screen (Welcome ➔ Vehicle ➔ Fuel ➔ Distance ➔ Result).
* **Comprehensive Vehicle Catalog:** Select from horizontal carousels of Cars, Motorcycles, Trucks, and Buses.
* **Custom Efficiency Support:** Manually input your exact vehicle fuel consumption (km/L) if it isn't listed in the presets.
* **Smart Fuel Filtering:** Automatically filters fuel choices based on vehicle type (e.g., commercial trucks are only shown Diesel options).
* **Localized Pricing (Malaysia):** Includes current retail rates and targeted subsidy frameworks (BUDI Madani RON95/Diesel, RON95, RON97, Diesel).
* **Calculation Breakdown:** Transparently displays the exact mathematical formula and working steps used to generate the final estimated cost and volume.
* **Flexible Navigation:** Features a dynamic "Back" button to reverse steps safely and a "Start Over" function for quick resets.

## Tech Stack & Architecture

* **Language:** Java
* **Build System:** Gradle (Kotlin DSL)
* **Architecture:** strict MVVM (Model-View-ViewModel)
* **Android Jetpack & UI Components:**
  * `ViewModel` & Enum-based State Machine for robust state retention.
  * `LiveData` for reactive UI updates.
  * `DataBinding` for two-way binding and cleaner XML layouts.
  * `RecyclerView` with custom adapters for efficient list rendering.
  * **Material 3 Components** (`MaterialCardView`, `TextInputLayout`, floating action buttons).

## App Flow

1. **Welcome Screen:** Initiates the calculator.
2. **Vehicle Selection:** Choose a predefined vehicle or select "Custom".
3. **Custom Efficiency (Conditional):** Enter km/L if "Custom" was selected.
4. **Fuel Selection:** Pick the relevant fuel tier based on your vehicle.
5. **Distance Input:** Enter your total travel distance in kilometers.
6. **Result Screen:** View the total fuel needed, estimated cost, and a detailed mathematical breakdown.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/rotittelur/Android-programming-task/tree/PBL1.git