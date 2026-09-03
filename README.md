# Vehicle Fuel Consumption Estimator (PBL1)

An interactive Android application designed to estimate trip fuel requirements and travel costs using modern Android architecture patterns.

## Features

- **Fuel Cost & Volume Calculation**: Computes total fuel needed (L) and overall trip cost (RM) using user-defined travel distance and vehicle efficiency.
- **Preset Fuel Options**: Integrated dropdown menu featuring current Malaysian fuel price points:
  - BUDI Madani RON95 (RM 1.99)
  - BUDI Diesel (RM 2.10)
  - RON95 Retail (RM 3.77)
  - RON97 Retail (RM 4.25)
  - Diesel Retail (RM 4.67)
- **Custom Pricing Support**: Includes an dynamic input field for manual price entry when selecting "Other (Custom Price)".
- **Input Sanitization**: Handles decimal commas, extra spaces, and invalid formats to prevent runtime crashes.

## Tech Stack & Architecture

- **Language**: Java
- **Build System**: Gradle (Kotlin DSL)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Android Jetpack Components**:
  - `ViewModel`: Handles calculation logic and state persistence.
  - `LiveData`: Drives reactive UI state updates.
  - `DataBinding`: Provides two-way UI-data binding to streamline layout management.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/rotittelur/Android-programming-task/tree/PBL1.git