# Apex Banking Management System
**Course:** Object Oriented Programming in Java (BTech CSE / IT / AI / DS)  
**Project Type:** Full-Stack Desktop & Web Mini Project with JDBC Database

---

## 🌐 Instant Access Links & Launch Scripts

The web prototype server is **currently live and running**:
- **Runnable Web Application Link:** [http://localhost:8080](http://localhost:8080)
- deployed link - [https://banking-managmnet-system.onrender.com](https://banking-managmnet-system.onrender.com)

### One-Click Launchers (In Project Folder):
1. **`run.bat`**: Compiles and launches both Desktop Swing GUI and Web browser interface.
2. **`run_web.bat`**: Launches the web application server and opens `http://localhost:8080`.
3. **`run_gui.bat`**: Launches the native Java Swing desktop GUI.
4. **`compile.bat`**: Recompiles all Java source code into `bin/`.

Alternatively, via terminal:
```bash
# Run with Standalone Executable JAR
java -jar BankingSystem.jar
```

---

## 📌 Project Overview & Screenshot Feature Mapping

This mini project implements all operations specified in the syllabus assignment:

| # | Required Operation | Implementation in Project |
|---|---|---|
| **1** | **Create an Account** | Supports `SAVINGS` and `CURRENT` accounts, auto-generates 10-digit account numbers, validates minimum balance requirements, sets 4-digit security PIN, and configures daily withdrawal limits. |
| **2** | **Deposit Money** | Credits funds to active accounts with real-time balance calculations and logs transaction details into the database audit ledger. |
| **3** | **Withdraw Money** | Authorizes withdrawals via PIN verification and enforces balance thresholds (minimum balance for Savings, overdraft allowance for Current). |
| **4** | **Honor Daily Withdrawal Limit** | Date-aware tracking of cumulative daily withdrawals. Automatically resets counters when a new calendar day arrives. Strictly rejects withdrawals exceeding the daily limit with a custom `DailyLimitExceededException` indicating the exact remaining allowance. |
| **5** | **Check the Balance** | PIN-authenticated balance inquiry returning available balance, configured limit, and remaining daily withdrawal limit. |
| **6** | **Display Account Information** | Displays complete customer profile (Holder Name, Account Number, Type, Status, Contact, Limits) and full passbook transaction ledger table with timestamps. |

---

## 🏛️ Object-Oriented Programming (OOP) Architecture

The project is structured according to industry-standard N-Tier layered architecture:

```
c:\Users\madhura\OneDrive\Desktop\Banking_Management/
├── src/com/bank/
│   ├── model/                  # Entities & Domain Models
│   │   ├── Account.java        # [ABSTRACTION & ENCAPSULATION] Abstract base class
│   │   ├── SavingsAccount.java # [INHERITANCE & POLYMORPHISM] Min balance & interest
│   │   ├── CurrentAccount.java # [INHERITANCE & POLYMORPHISM] Overdraft facility
│   │   └── Transaction.java    # Audit trail entity
│   ├── exception/              # Custom Checked Domain Exceptions
│   │   ├── BankingException.java
│   │   ├── AccountNotFoundException.java
│   │   ├── InsufficientBalanceException.java
│   │   ├── DailyLimitExceededException.java  # Honors daily withdrawal limit
│   │   ├── InvalidAmountException.java
│   │   └── AuthenticationException.java
│   ├── dao/                    # Data Access Object Layer (JDBC)
│   │   ├── DBConnection.java   # Factory & Singleton JDBC Manager (SQLite / MySQL)
│   │   ├── AccountDAO.java     # DAO Interface
│   │   ├── AccountDAOImpl.java # PreparedStatement JDBC CRUD
│   │   ├── TransactionDAO.java # DAO Interface
│   │   └── TransactionDAOImpl.java
│   ├── service/                # Business Logic & Rules
│   │   ├── BankingService.java
│   │   └── BankingServiceImpl.java # Enforces daily limit logic & date rollover
│   ├── gui/                    # Java Swing Desktop Front-End
│   │   ├── MainFrame.java      # Dashboard with Sidebar Navigation
│   │   ├── CreateAccountPanel.java
│   │   ├── DepositPanel.java
│   │   ├── WithdrawPanel.java  # Visual Daily Limit Gauge
│   │   ├── BalancePanel.java
│   │   ├── AccountInfoPanel.java
│   │   ├── AllAccountsPanel.java
│   │   └── DashboardPanel.java
│   ├── web/                    # Embedded HTTP REST Server
│   │   └── BankingWebServer.java
│   └── Main.java               # Application Entry Point
├── web/                        # Web Front-End
│   ├── index.html              # Responsive HTML5 SPA
│   ├── css/style.css           # Modern Fintech Styling
│   └── js/app.js               # REST Client Logic
├── lib/                        # Pre-bundled JAR Drivers
│   ├── sqlite-jdbc.jar         # Zero-config embedded database
│   ├── mysql-connector-j.jar   # MySQL JDBC Driver
│   ├── flatlaf.jar             # Modern Flat Look and Feel
│   ├── slf4j-api.jar
│   └── slf4j-simple.jar
├── database/
│   └── schema.sql              # SQL DDL script for reference
└── db.properties               # Database switch (sqlite or mysql)
```

---

## 🎯 Key OOP Concepts Demonstrated for Viva / Presentation

### 1. Abstraction
- Defined in `Account.java` through abstract methods:
  - `public abstract String getAccountType();`
  - `public abstract double getMinimumBalance();`
  - `public abstract double getOverdraftLimit();`
  - `public abstract double getDefaultDailyLimit();`
- Hides internal calculations from outside callers while enforcing uniform contracts.

### 2. Encapsulation
- All instance variables (`balance`, `pin`, `dailyWithdrawalLimit`, `dailyWithdrawnAmount`, `lastWithdrawalDate`) are declared `private`.
- Access and mutations occur strictly through validated getters, setters, and service methods.

### 3. Inheritance
- `SavingsAccount` and `CurrentAccount` extend the abstract base class `Account`.
- They inherit common attributes (`holderName`, `balance`, `pin`, etc.) while defining distinct banking behaviors.

### 4. Polymorphism
- Dynamic method dispatch via overridden methods:
  - Minimum balance check: `SavingsAccount` requires maintaining ₹1,000, whereas `CurrentAccount` permits overdraft down to -₹25,000.
  - Polymorphic DB row instantiation: Database rows dynamically instantiate either a `SavingsAccount` or `CurrentAccount` based on the stored `account_type`.

### 5. Exception Handling
- Domain-specific checked exceptions hierarchy rooted at `BankingException`:
  - `DailyLimitExceededException`
  - `InsufficientBalanceException`
  - `AccountNotFoundException`
  - `AuthenticationException`
  - `InvalidAmountException`

---

## 🗄️ Database & JDBC Management

- **Default Zero-Configuration Database:** SQLite (`banking.db`) runs locally inside the project without needing external database installation or service start.
- **MySQL Support:** You can seamlessly switch to MySQL Server by editing `db.properties`:
  ```properties
  db.type=mysql
  mysql.url=jdbc:mysql://localhost:3306/banking_db?createDatabaseIfNotExist=true
  mysql.user=root
  mysql.password=your_password
  ```
- **PreparedStatements:** All SQL executions use `PreparedStatement` with bind parameters (`?`), preventing SQL injection and improving execution performance.

---

## 🧪 Pre-Seeded Demo Accounts for Instant Testing

When the application starts for the first time, two accounts are automatically seeded for demonstration:

1. **Rahul Sharma (Savings Account)**
   - **Account Number:** `9553136897`
   - **PIN:** `1234`
   - **Initial Balance:** `₹27,000.00`
   - **Configured Daily Limit:** `₹25,000.00`
   - **Already Withdrawn Today:** `₹3,000.00`
   - **Remaining Limit for Today:** `₹22,000.00`

2. **Priya Patel (Current Account)**
   - **Account Number:** `5378017634`
   - **PIN:** `4321`
   - **Initial Balance:** `₹75,000.00`
   - **Configured Daily Limit:** `₹100,000.00`
   - **Remaining Limit for Today:** `₹100,000.00`

> In the Web UI, you can click the **⚡ Quick Fill Demo Account** button in the top right header to instantly load account credentials into any active form!
