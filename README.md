# Virtuoso

A Java Swing desktop application featuring a secure password vault and an algorithm visualizer, backed by a MySQL database. 

## Features
* **User Authentication:** Login and registration system.
* **Password Vault:** Securely generate and store application-specific passwords.
* **Algorithm Visualizer:** Execute and track sorting algorithms (Bubble Sort, Selection Sort) and save the input/output history.

## Local Setup
1. Clone the repository.
2. Start Apache and MySQL via the XAMPP Control Panel.
3. Open phpMyAdmin and create a database named `virtuso`.
4. Import the included `database.sql` file.
5. Open the project in your IDE, ensure `mysql-connector-j-9.7.0.jar` is referenced, and run `MainApp.java`.