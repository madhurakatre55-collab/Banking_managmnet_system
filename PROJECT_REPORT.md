# B.TECH MINI PROJECT REPORT

**Course:** Object Oriented Programming in Java  
**Degree:** Bachelor of Technology (B.Tech)  
**Department:** Computer Science and Engineering  
**Project Title:** Apex Banking Management System (with JDBC and Daily Limit Protection)  

---

# 1. ACKNOWLEDGEMENT

We express our sincere gratitude and indebtedness to our project guide, faculty members, and the Department of Computer Science and Engineering for providing their valuable guidance, continuous support, and constructive criticism throughout the development of this mini-project. 

We would also like to thank our college institution and the laboratory staff for providing the necessary computational facilities, Java development environment, and software infrastructure. 

Finally, we extend our heartfelt gratitude to our parents, teammates, and friends who directly or indirectly helped us in the successful completion of this project within the stipulated timeframe.

---

# 2. PROBLEM STATEMENT

In traditional manual banking and poorly synchronized transaction processing systems, handling core customer account operations is prone to manual errors, security oversights, and data inconsistencies. 

The objective of this project is to design, develop, and implement a robust, reliable, and secure **Banking Management System** using Object-Oriented Programming (OOP) principles in Java coupled with a relational database via Java Database Connectivity (JDBC).

The system must automate the following mandatory core banking operations:
1. **Create an account:** Register applicants with personal credentials, account type selection (`SAVINGS` or `CURRENT`), initial deposit validation against statutory minimum balances, and customized daily withdrawal limits.
2. **Deposit money:** Credit funds into specified accounts with instant ledger reflection and transactional database recording.
3. **Withdraw money:** Authenticate withdrawals using a secure 4-digit PIN, verify balance boundaries (minimum balance for savings, overdraft allowance for current accounts), and update account balances atomically.
4. **Honor daily withdrawal limit:** Strictly monitor cumulative withdrawals per calendar day. The system must prevent unauthorized transactions exceeding the daily quota and throw descriptive alerts detailing used and remaining allowances, with automatic quota rollover at midnight.
5. **Check the balance:** Real-time query of the current account balance, account standing, and the available withdrawal limit remaining for the day.
6. **Display account information:** Retrieve comprehensive account metadata (Holder Name, Account Number, Type, Contact, Status) alongside an itemized passbook audit log of historical transactions.

---

# 3. SOFTWARE AND HARDWARE REQUIREMENTS

### 3.1 Software Requirements
- **Operating System:** Windows 10/11, Linux (Ubuntu/Debian), or macOS
- **Programming Language:** Java Standard Edition (Java SE 17 LTS or higher)
- **Database Engine:** SQLite (Embedded zero-configuration) or MySQL Server 8.0+
- **JDBC Driver:** `sqlite-jdbc-3.45.2.0.jar` / `mysql-connector-j-8.3.0.jar`
- **UI Frameworks:** Java Swing (Desktop GUI with FlatLaf modern Look & Feel), HTML5/CSS3/JavaScript (Web prototype)
- **Build & Execution Tools:** `javac`, `java`, batch scripts (`run.bat`, `compile.bat`)
- **Version Control:** Git & GitHub

### 3.2 Hardware Requirements
- **Processor:** Intel Core i3 / AMD Ryzen 3 or higher (2.0 GHz minimum)
- **RAM:** Minimum 4 GB (8 GB recommended for simultaneous GUI & Web execution)
- **Hard Disk Space:** Minimum 200 MB free disk space for JDK runtime, libraries, and SQLite database file
- **Display Resolution:** 1280 x 720 pixels or higher

---

# 4. PROPOSED METHODOLOGY

The project adopts the **3-Tier / N-Tier Layered Software Architecture**, guaranteeing separation of concerns, high cohesion, low coupling, and maintainability:

1. **Presentation / UI Layer:**
   - **Desktop GUI:** Built using Java Swing (`JFrame`, `CardLayout`, `JPanel`, `JTable`) with modern UI components and FlatLaf skinning.
   - **Web Interface:** Responsive Single Page Application (SPA) utilizing HTML5, modern CSS, and JavaScript communicating via asynchronous REST API calls.

2. **Business Logic / Service Layer:**
   - Encapsulates domain validation, PIN verification, date-aware calculations for daily withdrawal limit rollover, and business invariants (e.g., minimum balance checks).

3. **Data Access Layer (DAO) & Persistence Layer:**
   - Employs the **DAO (Data Access Object) Pattern** and **Singleton Connection Factory** (`DBConnection`).
   - Uses parameterized `java.sql.PreparedStatement` to ensure SQL injection protection and ACID compliance.

### 4.1 Mapping of OOP Principles
- **Abstraction:** The abstract class `Account` defines essential banking operations (`getMinimumBalance()`, `getOverdraftLimit()`, `getDefaultDailyLimit()`), concealing complex internal rules from consumers.
- **Encapsulation:** All sensitive member variables (`balance`, `pin`, `dailyWithdrawalLimit`, `dailyWithdrawnAmount`) are declared `private` with guarded accessor and mutator methods.
- **Inheritance:** `SavingsAccount` and `CurrentAccount` extend the base `Account` class, inheriting core properties while introducing specialized account behaviors.
- **Polymorphism:** Method overriding dynamic binding ensures that withdrawal limits and minimum balance rules execute based on the runtime instance type.
- **Exception Handling:** A structured hierarchy of custom checked exceptions derived from `BankingException` handles error conditions gracefully (`DailyLimitExceededException`, `InsufficientBalanceException`, `AuthenticationException`).

---

# 5. ALGORITHMS

### 5.1 Algorithm for Account Creation
- **Step 1:** Start.
- **Step 2:** Accept `holderName`, `email`, `phone`, `pin`, `accountType`, `initialDeposit`, and optional `customDailyLimit`.
- **Step 3:** Validate inputs: check that name is non-empty and PIN has at least 4 characters.
- **Step 4:** Check `accountType`:
  - If `SAVINGS`: Ensure `initialDeposit >= ₹1,000.00`. Set default daily limit to ₹25,000.00.
  - If `CURRENT`: Ensure `initialDeposit >= ₹5,000.00`. Set default daily limit to ₹100,000.00.
- **Step 5:** Generate a unique 10-digit account number. Verify non-existence in the database.
- **Step 6:** Instantiate the corresponding polymorphic subclass (`SavingsAccount` or `CurrentAccount`).
- **Step 7:** Execute SQL `INSERT` into `accounts` table via JDBC `PreparedStatement`.
- **Step 8:** Log the opening deposit transaction in `transactions` table.
- **Step 9:** Return the newly created `Account` object and display confirmation.
- **Step 10:** Stop.

### 5.2 Algorithm for Withdrawal with Daily Limit Enforcement
- **Step 1:** Start.
- **Step 2:** Accept `accountNumber`, `pin`, `amount`, and optional `description`.
- **Step 3:** Validate that `amount > 0`. If not, throw `InvalidAmountException`.
- **Step 4:** Query database for the account matching `accountNumber`. If not found, throw `AccountNotFoundException`.
- **Step 5:** Authenticate entered `pin` against stored `account.pin`. If mismatch, throw `AuthenticationException`.
- **Step 6:** Verify account status is `ACTIVE`.
- **Step 7 [Daily Limit Check]:**
  - Obtain system current date (`today = LocalDate.now()`).
  - If `account.lastWithdrawalDate != today`, reset `account.dailyWithdrawnAmount = 0.0` and update `lastWithdrawalDate = today`.
  - Calculate `remainingDailyLimit = account.dailyWithdrawalLimit - account.dailyWithdrawnAmount`.
  - If `amount > remainingDailyLimit`, throw `DailyLimitExceededException` containing limit, already used, and remaining quota. Transaction aborts.
- **Step 8 [Balance Threshold Check]:**
  - If `SavingsAccount`: Check if `(currentBalance - amount) < minimumBalance (₹1,000)`. If true, throw `InsufficientBalanceException`.
  - If `CurrentAccount`: Check if `(currentBalance - amount) < -overdraftLimit (-₹25,000)`. If true, throw `InsufficientBalanceException`.
- **Step 9 [Execution]:**
  - `newBalance = currentBalance - amount`.
  - `newDailyWithdrawn = account.dailyWithdrawnAmount + amount`.
  - Update `accounts` table with `newBalance`, `newDailyWithdrawn`, and `today`.
  - Insert audit log into `transactions` table with type `WITHDRAWAL`.
- **Step 10:** Display success receipt with updated balance and remaining daily limit.
- **Step 11:** Stop.

---

# 6. FLOWCHARTS

### 6.1 Daily Withdrawal Limit & Balance Validation Flowchart

```
       +------------------------------------+
       |  Start: Withdrawal Request (Amt)   |
       +------------------------------------+
                         |
                         v
       +------------------------------------+
       |   Validate Account & PIN Match     |
       +------------------------------------+
                         |
           +-------------+-------------+
           | Valid?                    | Invalid
           v                           v
+-----------------------+     +------------------------+
| Date Rollover Check:  |     |  Throw Authentication  |
| Is lastDate == Today? |     |       Exception        |
+-----------------------+     +------------------------+
     |             |
     | No          | Yes
     v             v
+--------------+   |
| Reset Daily  |   |
| Withdrawn=0  |   |
+--------------+   |
     |             |
     +------+------+
            |
            v
+------------------------------------+
| Calculate Remaining Limit:         |
| RemLimit = DailyLimit - Withdrawn  |
+------------------------------------+
            |
            v
     +--------------+
     | Amt <= Rem?  |-------- No -------> +------------------------+
     +--------------+                     |  Throw Daily Limit     |
            | Yes                         |  Exceeded Exception    |
            v                             +------------------------+
+-----------------------+
| Check Min Balance /   |
| Overdraft Constraint  |
+-----------------------+
            |
     +--------------+
     | Funds OK?    |-------- No -------> +------------------------+
     +--------------+                     |  Throw Insufficient    |
            | Yes                         |   Balance Exception    |
            v                             +------------------------+
+------------------------------------+
| Deduct Balance & Update DB         |
| Increment Daily Withdrawn Total    |
| Insert Audit Entry in Ledger Table |
+------------------------------------+
            |
            v
+------------------------------------+
|  Display Success Receipt & Limits  |
+------------------------------------+
            |
            v
       +----------+
       |   End    |
       +----------+
```

---

# 7. CODING / IMPLEMENTATION HIGHLIGHTS

### 7.1 Abstract Base Class (`Account.java`) - Abstraction & Encapsulation
```java
package com.bank.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Account {
    private String accountNumber;
    private String holderName;
    private String pin;
    private double balance;
    private double dailyWithdrawalLimit;
    private double dailyWithdrawnAmount;
    private LocalDate lastWithdrawalDate;
    private String status;

    public abstract String getAccountType();
    public abstract double getMinimumBalance();
    public abstract double getOverdraftLimit();
    public abstract double getDefaultDailyLimit();

    public void resetDailyWithdrawnIfNeeded(LocalDate today) {
        if (this.lastWithdrawalDate == null || !this.lastWithdrawalDate.equals(today)) {
            this.dailyWithdrawnAmount = 0.0;
            this.lastWithdrawalDate = today;
        }
    }

    public double getRemainingDailyLimit(LocalDate today) {
        resetDailyWithdrawnIfNeeded(today);
        return Math.max(0.0, this.dailyWithdrawalLimit - this.dailyWithdrawnAmount);
    }

    // Getters and Setters omitted for brevity
}
```

### 7.2 Polymorphic Subclasses (`SavingsAccount.java`) - Inheritance
```java
package com.bank.model;

public class SavingsAccount extends Account {
    public static final double MINIMUM_BALANCE = 1000.00;
    public static final double DEFAULT_DAILY_LIMIT = 25000.00;

    @Override
    public String getAccountType() { return "SAVINGS"; }

    @Override
    public double getMinimumBalance() { return MINIMUM_BALANCE; }

    @Override
    public double getOverdraftLimit() { return 0.00; }

    @Override
    public double getDefaultDailyLimit() { return DEFAULT_DAILY_LIMIT; }
}
```

### 7.3 Daily Limit Enforcement (`BankingServiceImpl.java`)
```java
// Extract from BankingServiceImpl.java
public Account withdraw(String accNum, String pin, double amount, String desc) 
        throws BankingException {
    Account account = accountDAO.findByAccountNumber(accNum);
    if (account == null) throw new AccountNotFoundException(accNum);
    if (!account.getPin().equals(pin)) throw new AuthenticationException("Invalid PIN");

    LocalDate today = LocalDate.now();
    account.resetDailyWithdrawnIfNeeded(today);
    double remaining = account.getRemainingDailyLimit(today);

    // 1. Strict Daily Withdrawal Limit Check
    if (amount > remaining) {
        throw new DailyLimitExceededException(
            account.getDailyWithdrawalLimit(),
            account.getDailyWithdrawnAmount(),
            amount,
            remaining
        );
    }

    // 2. Minimum Balance Check
    if (account instanceof SavingsAccount) {
        if ((account.getBalance() - amount) < account.getMinimumBalance()) {
            throw new InsufficientBalanceException("Minimum balance violated", 
                account.getBalance(), amount);
        }
    }

    // Update state and persist to database
    double newBalance = account.getBalance() - amount;
    double newDailyWithdrawn = account.getDailyWithdrawnAmount() + amount;
    accountDAO.updateBalanceAndDailyWithdrawn(accNum, newBalance, newDailyWithdrawn, today);

    transactionDAO.logTransaction(new Transaction(accNum, "WITHDRAWAL", amount, newBalance, desc));
    return account;
}
```

---

# 8. OUTPUT AND TEST CASES

### 8.1 Test Case Matrix

| Test Case ID | Operation | Input Data | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **TC-01** | Create Account | Name: "Ramesh Rao", Type: SAVINGS, Dep: ₹5,000, Limit: ₹25,000 | Account created, 10-digit ID assigned, DB record inserted | Account created with initial balance ₹5,000.00 | **PASS** |
| **TC-02** | Deposit Money | Acc: 9553136897, Amount: ₹5,000.00 | Balance increases from ₹25,000 to ₹30,000; Transaction logged | Balance updated to ₹30,000.00 | **PASS** |
| **TC-03** | Valid Withdrawal | Acc: 9553136897, PIN: 1234, Amount: ₹3,000.00 | Balance becomes ₹27,000; Used today becomes ₹3,000; Remaining limit: ₹22,000 | Withdrawal successful; Limits recorded | **PASS** |
| **TC-04** | Exceed Daily Limit | Acc: 9553136897, PIN: 1234, Amount: ₹25,000.00 | Transaction rejected; Error shows limit ₹25k, used ₹3k, remaining ₹22k | System rejects transaction with `DailyLimitExceededException` | **PASS** |
| **TC-05** | Check Balance | Acc: 9553136897, PIN: 1234 | Returns current balance and today's remaining limit | Balance ₹27,000.00 and limit details displayed | **PASS** |
| **TC-06** | Account Info & Passbook | Acc: 9553136897, PIN: 1234 | Shows complete profile and itemized transaction list | Profile and statement table with timestamps rendered | **PASS** |

### 8.2 Sample Console Output
```text
==================================================================
  BANKING MANAGEMENT SYSTEM - BTECH OOP IN JAVA MINI PROJECT
==================================================================
-> Initializing Database (SQLITE via JDBC)...
Database successfully connected and initialized (SQLITE)
-> Creating demo accounts for initial testing...
-> Seeded Demo Account 1: Number = 9553136897 | PIN = 1234
-> Seeded Demo Account 2: Number = 5378017634 | PIN = 4321
=======================================================
  BANKING WEB SERVER RUNNING SUCCESSFULLY!
  Access Link: http://localhost:8080
=======================================================
```

---

# 9. CONCLUSION

The **Apex Banking Management System** was successfully conceptualized, designed, and implemented to satisfy all curricular and practical requirements of the B.Tech Object-Oriented Programming in Java course.

### Key Achievements:
1. **Full OOP Conformance:** Efficiently implements Abstraction, Encapsulation, Inheritance, and Dynamic Polymorphism through clear separation of `SavingsAccount` and `CurrentAccount`.
2. **Automated Daily Limit Protection:** Demonstrates practical real-world business constraints by protecting customer accounts from excessive daily cash drain and resetting quotas seamlessly using system calendar date tracking.
3. **Robust Data Persistence:** Leverages JDBC with `PreparedStatement` to ensure data integrity, atomicity, and resilience against SQL injection attacks.
4. **Dual Interfaces:** Delivers both an intuitive Java Swing desktop application and a modern browser-based web application with responsive UI.
