
# Bank Account Management System

# Java Mini Project

## Overview

This is a lightweight Java desktop application that simulates a simple banking system. It provides a modern Swing‑based graphical user interface (`BankGUI`) for managing customers, accounts, and transactions. All data is persisted locally in a binary file (`bankdata.dat`).

## Features

- Create, update, and delete **customers** and **beneficiaries**
- Open multiple **bank accounts** per customer
- Perform **deposits**, **withdrawals**, and **transfers**
- View transaction history with colour‑coded amounts
- Responsive, glass‑morphism inspired UI components
- Data persistence using Java serialization

## Architecture Overview

The application follows a straightforward MVC‑like pattern:

- **View** – `BankGUI` (Swing components, custom renderers, modern UI widgets)
- **Controller / Service** – `DataManager` (coordinates UI actions, calls business logic)
- **Model** – `BankAccount`, `Customer`, `Beneficiary`, `Transaction`
- **Business Logic** – `BankSystem` (validation, calculation, business rules)
- **Persistence** – `DataManager` serialises and deserialises the model objects to `bankdata.dat`

A detailed flowchart can be imported into **Excalidraw** from the file `flowchart.excalidraw` included in this repository.

## Getting Started

### Prerequisites

- Java JDK 17 or later
- Maven (optional, for building the project) or any IDE that supports Java projects

### Build & Run

```bash
# Clone the repository (if you haven't already)
git clone <repository‑url>
cd JavaMiniProject-main

# Compile the source files
javac -d out src/*.java

# Run the application
java -cp out BankGUI
```

If you prefer Maven, you can generate a `pom.xml` and run `mvn package` followed by `java -jar target/JavaMiniProject.jar`.

## Usage

1. Launch the application – the main window displays a navigation bar on the left.
2. Use the **Accounts** tab to create new accounts or view balances.
3. The **Transactions** tab allows you to deposit, withdraw, or transfer funds.
4. Customer and beneficiary management is available under the **People** tab.
5. All changes are automatically saved to `bankdata.dat` when the application exits.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/YourFeature`).
3. Make your changes and ensure the project still compiles.
4. Open a pull request with a clear description of the changes.

## License

This project is licensed under the MIT License – see the `LICENSE` file for details.


