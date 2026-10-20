/**
 * Apex Banking Management System - Web Client JS
 * Supports:
 * 1. Live Java Backend REST API (when running on localhost or Java Web Service)
 * 2. Automatic In-Browser Fallback Engine (when hosted on Render Static Sites / GitHub Pages)
 */

const API_BASE = '';
let currentAccountsList = [];
let demoAccountIndex = 0;
let isOfflineMode = false;

// Default Mock Database for Static Deployments (Render Static Site)
const INITIAL_DEMO_ACCOUNTS = [
  {
    accountNumber: "9553136897",
    holderName: "Rahul Sharma",
    email: "rahul.sharma@example.com",
    phoneNumber: "9876543210",
    pin: "1234",
    balance: 27000.00,
    dailyWithdrawalLimit: 25000.00,
    dailyWithdrawnAmount: 3000.00,
    remainingDailyLimit: 22000.00,
    lastWithdrawalDate: new Date().toISOString().split('T')[0],
    accountType: "SAVINGS",
    status: "ACTIVE"
  },
  {
    accountNumber: "5378017634",
    holderName: "Priya Patel",
    email: "priya.patel@example.com",
    phoneNumber: "9811223344",
    pin: "4321",
    balance: 75000.00,
    dailyWithdrawalLimit: 100000.00,
    dailyWithdrawnAmount: 0.00,
    remainingDailyLimit: 100000.00,
    lastWithdrawalDate: new Date().toISOString().split('T')[0],
    accountType: "CURRENT",
    status: "ACTIVE"
  }
];

const INITIAL_DEMO_TRANSACTIONS = [
  {
    transactionId: 101,
    accountNumber: "9553136897",
    transactionType: "DEPOSIT",
    amount: 25000.00,
    balanceAfter: 25000.00,
    timestamp: "2026-10-20 10:00:00",
    description: "Account opening initial deposit"
  },
  {
    transactionId: 102,
    accountNumber: "9553136897",
    transactionType: "DEPOSIT",
    amount: 5000.00,
    balanceAfter: 30000.00,
    timestamp: "2026-10-20 11:30:00",
    description: "Online Salary Credit"
  },
  {
    transactionId: 103,
    accountNumber: "9553136897",
    transactionType: "WITHDRAWAL",
    amount: 3000.00,
    balanceAfter: 27000.00,
    timestamp: "2026-10-20 14:15:00",
    description: "ATM Cash Withdrawal"
  },
  {
    transactionId: 104,
    accountNumber: "5378017634",
    transactionType: "DEPOSIT",
    amount: 75000.00,
    balanceAfter: 75000.00,
    timestamp: "2026-10-20 09:45:00",
    description: "Account opening initial deposit"
  }
];

// Initialize LocalStorage Data if not present
function initLocalStorageMock() {
  if (!localStorage.getItem('apex_accounts')) {
    localStorage.setItem('apex_accounts', JSON.stringify(INITIAL_DEMO_ACCOUNTS));
  }
  if (!localStorage.getItem('apex_transactions')) {
    localStorage.setItem('apex_transactions', JSON.stringify(INITIAL_DEMO_TRANSACTIONS));
  }
}

function getLocalAccounts() {
  initLocalStorageMock();
  return JSON.parse(localStorage.getItem('apex_accounts'));
}

function saveLocalAccounts(accs) {
  localStorage.setItem('apex_accounts', JSON.stringify(accs));
}

function getLocalTransactions() {
  initLocalStorageMock();
  return JSON.parse(localStorage.getItem('apex_transactions'));
}

function logLocalTransaction(tx) {
  const txs = getLocalTransactions();
  tx.transactionId = txs.length ? txs[txs.length - 1].transactionId + 1 : 1;
  tx.timestamp = new Date().toISOString().replace('T', ' ').substring(0, 19);
  txs.unshift(tx);
  localStorage.setItem('apex_transactions', JSON.stringify(txs));
}

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initLocalStorageMock();
  loadDashboardData();
  setupQuickDemoButton();
});

// Navigation Handling
function initNavigation() {
  const navButtons = document.querySelectorAll('.nav-item');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      switchTab(targetTab);
    });
  });
}

function switchTab(tabId) {
  document.querySelectorAll('.nav-item').forEach(btn => {
    if (btn.getAttribute('data-tab') === tabId) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });

  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.remove('active');
  });

  const activePane = document.getElementById(tabId);
  if (activePane) {
    activePane.classList.add('active');
  }

  updateHeaderText(tabId);

  if (tabId === 'tab-dashboard') loadDashboardData();
  if (tabId === 'tab-directory') loadAccountsDirectory();
}

function updateHeaderText(tabId) {
  const title = document.getElementById('page-header-title');
  const desc = document.getElementById('page-header-desc');

  switch (tabId) {
    case 'tab-dashboard':
      title.textContent = 'Dashboard Overview';
      desc.textContent = 'Welcome to the Banking Management System prototype';
      break;
    case 'tab-create':
      title.textContent = 'Create an Account';
      desc.textContent = 'Feature 1: Open a new bank account with OOP data validation';
      break;
    case 'tab-deposit':
      title.textContent = 'Deposit Money';
      desc.textContent = 'Feature 2: Credit funds with immediate balance update and JDBC ledger';
      break;
    case 'tab-withdraw':
      title.textContent = 'Withdraw Money';
      desc.textContent = 'Features 3 & 4: Withdraw funds strictly honoring the Daily Withdrawal Limit';
      break;
    case 'tab-balance':
      title.textContent = 'Check the Balance';
      desc.textContent = 'Feature 5: Quick PIN-authenticated balance & remaining daily limit check';
      break;
    case 'tab-info':
      title.textContent = 'Account Information';
      desc.textContent = 'Feature 6: View complete customer profile and passbook statement';
      break;
    case 'tab-directory':
      title.textContent = 'Accounts Directory (DBMS)';
      desc.textContent = 'Live view of all accounts stored in the database';
      break;
  }
}

function formatCurrency(val) {
  if (val === undefined || val === null || isNaN(val)) return '₹0.00';
  return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function showAlert(message, type = 'success', duration = 5000) {
  const box = document.getElementById('alert-box');
  box.className = `alert ${type}`;
  box.innerHTML = `
    <span>${message}</span>
    <button onclick="this.parentElement.classList.add('hidden')" style="background:none; border:none; cursor:pointer; font-weight:bold; color:inherit;">✕</button>
  `;
  box.classList.remove('hidden');

  if (duration > 0) {
    setTimeout(() => {
      box.classList.add('hidden');
    }, duration);
  }
}

// 1. Dashboard Data
async function loadDashboardData() {
  try {
    const res = await fetch(`${API_BASE}/api/accounts`);
    if (!res.ok) throw new Error("API not found");
    const accounts = await res.json();
    currentAccountsList = accounts;
    isOfflineMode = false;
    updateStatusBadge(true);
  } catch (err) {
    // Fallback to local storage (e.g. Render Static Site)
    currentAccountsList = getLocalAccounts();
    isOfflineMode = true;
    updateStatusBadge(false);
  }

  let totalReserves = 0;
  currentAccountsList.forEach(a => totalReserves += Number(a.balance));

  document.getElementById('stat-total-accounts').textContent = currentAccountsList.length;
  document.getElementById('stat-total-reserves').textContent = formatCurrency(totalReserves);
}

function updateStatusBadge(isOnline) {
  const badge = document.querySelector('.db-status-badge');
  if (badge) {
    if (isOnline) {
      badge.innerHTML = '<span class="pulse-dot"></span><span>JDBC Database: Connected (Java)</span>';
      badge.style.color = '#4ade80';
    } else {
      badge.innerHTML = '<span class="pulse-dot" style="background:#38bdf8; box-shadow:0 0 8px #38bdf8"></span><span>Cloud Mode: Active (Demo Engine)</span>';
      badge.style.color = '#38bdf8';
    }
  }
}

function setupQuickDemoButton() {
  const btn = document.getElementById('quick-demo-btn');
  btn.addEventListener('click', async () => {
    if (!currentAccountsList || currentAccountsList.length === 0) {
      await loadDashboardData();
    }
    if (currentAccountsList.length === 0) {
      showAlert('No accounts in database yet! Create one first.', 'warning');
      return;
    }

    const acc = currentAccountsList[demoAccountIndex % currentAccountsList.length];
    demoAccountIndex++;

    const depAcc = document.getElementById('deposit-acc');
    const withAcc = document.getElementById('withdraw-acc');
    const withPin = document.getElementById('withdraw-pin');
    const balAcc = document.getElementById('balance-acc');
    const balPin = document.getElementById('balance-pin');
    const infoAcc = document.getElementById('info-acc');

    if (depAcc) depAcc.value = acc.accountNumber;
    if (withAcc) withAcc.value = acc.accountNumber;
    if (withPin) withPin.value = acc.pin || '1234';
    if (balAcc) balAcc.value = acc.accountNumber;
    if (balPin) balPin.value = acc.pin || '1234';
    if (infoAcc) infoAcc.value = acc.accountNumber;

    showAlert(`Pre-filled active inputs with Demo Account: <strong>${acc.accountNumber}</strong> (${acc.holderName}, PIN: ${acc.pin || '1234'})`, 'success', 4000);
  });
}

// 2. Feature 1: Create Account
function onAccountTypeChanged() {
  const type = document.getElementById('create-type').value;
  const depositField = document.getElementById('create-deposit');
  const depositHint = document.getElementById('deposit-hint');
  const limitField = document.getElementById('create-limit');
  const limitHint = document.getElementById('limit-hint');

  if (type === 'CURRENT') {
    depositHint.textContent = 'Minimum initial deposit for Current Account: ₹5,000.00 (Overdraft allowed up to ₹25,000)';
    limitHint.textContent = 'Default daily withdrawal limit for Current: ₹100,000.00';
    if (Number(depositField.value) < 5000) depositField.value = 5000;
    depositField.min = 5000;
    limitField.value = 100000;
  } else {
    depositHint.textContent = 'Minimum initial deposit for Savings Account: ₹1,000.00 (4% interest)';
    limitHint.textContent = 'Default daily withdrawal limit for Savings: ₹25,000.00';
    depositField.min = 1000;
    limitField.value = 25000;
  }
}

async function handleCreateAccount(event) {
  event.preventDefault();

  const payload = {
    holderName: document.getElementById('create-name').value.trim(),
    accountType: document.getElementById('create-type').value,
    email: document.getElementById('create-email').value.trim(),
    phoneNumber: document.getElementById('create-phone').value.trim(),
    pin: document.getElementById('create-pin').value.trim(),
    initialDeposit: document.getElementById('create-deposit').value,
    dailyLimit: document.getElementById('create-limit').value
  };

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success) {
        showCreatedSuccess(data.account, payload.pin);
        return;
      } else {
        showAlert(`❌ Error: ${data.message}`, 'error', 7000);
        return;
      }
    } catch (err) {
      // Switch to local mode
      isOfflineMode = true;
    }
  }

  // Local Storage Execution
  const accounts = getLocalAccounts();
  const accNum = String(Math.floor(1000000000 + Math.random() * 9000000000));
  const deposit = Number(payload.initialDeposit);
  const minBal = payload.accountType === 'CURRENT' ? 5000 : 1000;

  if (deposit < minBal) {
    showAlert(`Initial deposit must be at least ₹${minBal} for ${payload.accountType} account.`, 'error');
    return;
  }

  const newAcc = {
    accountNumber: accNum,
    holderName: payload.holderName,
    email: payload.email,
    phoneNumber: payload.phoneNumber,
    pin: payload.pin,
    balance: deposit,
    dailyWithdrawalLimit: Number(payload.dailyLimit),
    dailyWithdrawnAmount: 0.0,
    remainingDailyLimit: Number(payload.dailyLimit),
    lastWithdrawalDate: new Date().toISOString().split('T')[0],
    accountType: payload.accountType,
    status: "ACTIVE"
  };

  accounts.unshift(newAcc);
  saveLocalAccounts(accounts);
  logLocalTransaction({
    accountNumber: accNum,
    transactionType: "DEPOSIT",
    amount: deposit,
    balanceAfter: deposit,
    description: "Account opening initial deposit"
  });

  showCreatedSuccess(newAcc, payload.pin);
}

function showCreatedSuccess(account, pin) {
  showAlert(`🎉 Account Created! Account Number: <strong>${account.accountNumber}</strong> for <strong>${account.holderName}</strong>. PIN: <strong>${pin}</strong>.`, 'success', 10000);
  document.getElementById('create-account-form').reset();
  onAccountTypeChanged();
  loadDashboardData();
}

// 3. Feature 2: Deposit Money
async function quickVerifyAccount(accInputId) {
  const accNum = document.getElementById(accInputId).value.trim();
  if (!accNum) {
    showAlert('Please enter an account number first.', 'warning');
    return;
  }

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/info`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum })
      });
      const data = await res.json();
      if (data.success) {
        renderPreviewBox(data.account);
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  const acc = getLocalAccounts().find(a => a.accountNumber === accNum);
  if (acc) {
    renderPreviewBox(acc);
  } else {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
  }
}

function renderPreviewBox(acc) {
  const box = document.getElementById('deposit-account-preview');
  box.classList.remove('hidden');
  box.innerHTML = `
    <strong>Account Holder:</strong> ${acc.holderName} (${acc.accountType})<br>
    <strong>Current Balance:</strong> ${formatCurrency(acc.balance)} • <strong>Daily Limit:</strong> ${formatCurrency(acc.dailyWithdrawalLimit)}
  `;
}

async function handleDeposit(event) {
  event.preventDefault();
  const accNum = document.getElementById('deposit-acc').value.trim();
  const amount = Number(document.getElementById('deposit-amount').value);
  const desc = document.getElementById('deposit-desc').value.trim();

  if (amount <= 0) {
    showAlert('Deposit amount must be strictly greater than zero.', 'error');
    return;
  }

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/deposit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum, amount, description: desc })
      });
      const data = await res.json();
      if (data.success) {
        showDepositSuccess(data.account, amount);
        return;
      } else {
        showAlert(`❌ Deposit Failed: ${data.message}`, 'error', 7000);
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  // Local Storage Execution
  const accounts = getLocalAccounts();
  const acc = accounts.find(a => a.accountNumber === accNum);
  if (!acc) {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
    return;
  }

  acc.balance += amount;
  saveLocalAccounts(accounts);
  logLocalTransaction({
    accountNumber: accNum,
    transactionType: "DEPOSIT",
    amount: amount,
    balanceAfter: acc.balance,
    description: desc || "Online Cash Deposit"
  });

  showDepositSuccess(acc, amount);
}

function showDepositSuccess(account, amount) {
  showAlert(`✅ Deposit of ${formatCurrency(amount)} successful! New Balance: <strong>${formatCurrency(account.balance)}</strong> for ${account.holderName}.`, 'success', 8000);
  document.getElementById('deposit-amount').value = '';
  quickVerifyAccount('deposit-acc');
  loadDashboardData();
}

// 4. Features 3 & 4: Withdraw Money & Honor Daily Limit
async function checkWithdrawalStatus() {
  const accNum = document.getElementById('withdraw-acc').value.trim();
  const pin = document.getElementById('withdraw-pin').value.trim();

  if (!accNum || !pin) {
    showAlert('Please provide both Account Number and PIN to verify limits.', 'warning');
    return;
  }

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/info`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum, pin })
      });
      const data = await res.json();
      if (data.success) {
        renderLimitGauge(data.account);
        return;
      } else {
        showAlert(data.message, 'error');
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  const acc = getLocalAccounts().find(a => a.accountNumber === accNum);
  if (!acc) {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
    return;
  }
  if (acc.pin && acc.pin !== pin) {
    showAlert('Invalid 4-digit PIN for account ' + accNum, 'error');
    return;
  }

  // Daily reset check
  const today = new Date().toISOString().split('T')[0];
  if (acc.lastWithdrawalDate !== today) {
    acc.dailyWithdrawnAmount = 0.0;
    acc.lastWithdrawalDate = today;
  }
  acc.remainingDailyLimit = Math.max(0, acc.dailyWithdrawalLimit - acc.dailyWithdrawnAmount);

  renderLimitGauge(acc);
}

function renderLimitGauge(a) {
  const gauge = document.getElementById('withdraw-limit-gauge');
  gauge.classList.remove('hidden');

  document.getElementById('gauge-holder-name').textContent = a.holderName;
  document.getElementById('gauge-acc-type').textContent = a.accountType;
  document.getElementById('gauge-current-balance').textContent = `Balance: ${formatCurrency(a.balance)}`;
  document.getElementById('gauge-limit-val').textContent = formatCurrency(a.dailyWithdrawalLimit);
  document.getElementById('gauge-used-val').textContent = formatCurrency(a.dailyWithdrawnAmount);
  document.getElementById('gauge-remain-val').textContent = formatCurrency(a.remainingDailyLimit);

  const pct = (a.dailyWithdrawalLimit > 0)
    ? Math.min(100, (Number(a.dailyWithdrawnAmount) / Number(a.dailyWithdrawalLimit)) * 100)
    : 0;

  document.getElementById('gauge-progress-bar').style.width = `${pct}%`;
  document.getElementById('gauge-usage-text').textContent =
    `${pct.toFixed(1)}% of today's limit used (${formatCurrency(a.dailyWithdrawnAmount)} of ${formatCurrency(a.dailyWithdrawalLimit)})`;
}

async function handleWithdraw(event) {
  event.preventDefault();
  const accNum = document.getElementById('withdraw-acc').value.trim();
  const pin = document.getElementById('withdraw-pin').value.trim();
  const amount = Number(document.getElementById('withdraw-amount').value);
  const desc = document.getElementById('withdraw-desc').value.trim();

  if (amount <= 0) {
    showAlert('Withdrawal amount must be strictly greater than zero.', 'error');
    return;
  }

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/withdraw`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum, pin, amount, description: desc })
      });
      const data = await res.json();
      if (data.success) {
        showWithdrawSuccess(data.account, amount);
        return;
      } else {
        if (data.errorType === 'DAILY_LIMIT_EXCEEDED') {
          showAlert(`⚠️ <strong>TRANSACTION REJECTED: DAILY WITHDRAWAL LIMIT HONORED!</strong><br>${data.message}`, 'warning', 10000);
        } else {
          showAlert(`❌ Withdrawal Denied: ${data.message}`, 'error', 7000);
        }
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  // Local Storage Execution
  const accounts = getLocalAccounts();
  const acc = accounts.find(a => a.accountNumber === accNum);
  if (!acc) {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
    return;
  }
  if (acc.pin && acc.pin !== pin) {
    showAlert('Invalid 4-digit PIN for account ' + accNum, 'error');
    return;
  }

  // Honor Daily Limit
  const today = new Date().toISOString().split('T')[0];
  if (acc.lastWithdrawalDate !== today) {
    acc.dailyWithdrawnAmount = 0.0;
    acc.lastWithdrawalDate = today;
  }
  const remaining = Math.max(0, acc.dailyWithdrawalLimit - acc.dailyWithdrawnAmount);

  if (amount > remaining) {
    const err = `Daily withdrawal limit exceeded! Daily Limit: ${formatCurrency(acc.dailyWithdrawalLimit)}, Already Withdrawn Today: ${formatCurrency(acc.dailyWithdrawnAmount)}, Attempted: ${formatCurrency(amount)}. Available Limit Remaining Today: ${formatCurrency(remaining)}`;
    showAlert(`⚠️ <strong>TRANSACTION REJECTED: DAILY WITHDRAWAL LIMIT HONORED!</strong><br>${err}`, 'warning', 10000);
    return;
  }

  // Balance Check
  const minBal = acc.accountType === 'CURRENT' ? -25000 : 1000;
  if ((acc.balance - amount) < minBal) {
    showAlert(`Withdrawal denied! Maintaining required balance threshold. Maximum withdrawable right now: ${formatCurrency(Math.max(0, acc.balance - minBal))}`, 'error');
    return;
  }

  // Execute
  acc.balance -= amount;
  acc.dailyWithdrawnAmount += amount;
  acc.remainingDailyLimit = Math.max(0, acc.dailyWithdrawalLimit - acc.dailyWithdrawnAmount);
  saveLocalAccounts(accounts);

  logLocalTransaction({
    accountNumber: accNum,
    transactionType: "WITHDRAWAL",
    amount: amount,
    balanceAfter: acc.balance,
    description: desc || "ATM Cash Withdrawal"
  });

  showWithdrawSuccess(acc, amount);
}

function showWithdrawSuccess(account, amount) {
  showAlert(`✅ Withdrawal of ${formatCurrency(amount)} Authorized! Remaining Balance: <strong>${formatCurrency(account.balance)}</strong>. Today's Available Limit Left: <strong>${formatCurrency(account.remainingDailyLimit)}</strong>.`, 'success', 8000);
  document.getElementById('withdraw-amount').value = '';
  checkWithdrawalStatus();
  loadDashboardData();
}

// 5. Feature 5: Check Balance
async function handleCheckBalance(event) {
  event.preventDefault();
  const accNum = document.getElementById('balance-acc').value.trim();
  const pin = document.getElementById('balance-pin').value.trim();

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/balance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum, pin })
      });
      const data = await res.json();
      if (data.success) {
        renderBalanceResult(data.account);
        return;
      } else {
        showAlert(data.message, 'error');
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  const acc = getLocalAccounts().find(a => a.accountNumber === accNum);
  if (!acc) {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
    return;
  }
  if (acc.pin && acc.pin !== pin) {
    showAlert('Invalid 4-digit PIN for account ' + accNum, 'error');
    return;
  }

  const today = new Date().toISOString().split('T')[0];
  if (acc.lastWithdrawalDate !== today) {
    acc.dailyWithdrawnAmount = 0.0;
    acc.lastWithdrawalDate = today;
  }
  acc.remainingDailyLimit = Math.max(0, acc.dailyWithdrawalLimit - acc.dailyWithdrawnAmount);
  renderBalanceResult(acc);
}

function renderBalanceResult(a) {
  const card = document.getElementById('balance-result-card');
  card.classList.remove('hidden');

  document.getElementById('balance-result-amount').textContent = formatCurrency(a.balance);
  document.getElementById('bal-holder').textContent = a.holderName;
  document.getElementById('bal-acc-num').textContent = a.accountNumber;
  document.getElementById('bal-type').textContent = a.accountType;
  document.getElementById('bal-status').textContent = a.status;
  document.getElementById('bal-limit').textContent = formatCurrency(a.dailyWithdrawalLimit);
  document.getElementById('bal-remain').textContent = formatCurrency(a.remainingDailyLimit);
}

// 6. Feature 6: Display Account Information & Statement
async function handleFetchAccountInfo(event) {
  event.preventDefault();
  const accNum = document.getElementById('info-acc').value.trim();
  const pin = document.getElementById('info-pin').value.trim();

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts/info`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: accNum, pin })
      });
      const data = await res.json();
      if (data.success) {
        renderAccountInfo(data.account, data.transactions || []);
        return;
      } else {
        showAlert(data.message, 'error');
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  const acc = getLocalAccounts().find(a => a.accountNumber === accNum);
  if (!acc) {
    showAlert('Account not found with Account Number: ' + accNum, 'error');
    return;
  }
  if (pin && acc.pin && acc.pin !== pin) {
    showAlert('Incorrect PIN for account ' + accNum, 'error');
    return;
  }

  const txs = getLocalTransactions().filter(t => t.accountNumber === accNum);
  renderAccountInfo(acc, txs);
}

function renderAccountInfo(a, txs) {
  const disp = document.getElementById('account-info-display');
  disp.classList.remove('hidden');

  const initials = a.holderName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  document.getElementById('profile-avatar').textContent = initials || 'AB';

  document.getElementById('profile-name').textContent = a.holderName;
  document.getElementById('profile-type-num').textContent = `${a.accountType} ACCOUNT • #${a.accountNumber}`;
  document.getElementById('profile-balance').textContent = formatCurrency(a.balance);
  document.getElementById('profile-email').textContent = a.email || 'N/A';
  document.getElementById('profile-phone').textContent = a.phoneNumber || 'N/A';
  document.getElementById('profile-limit').textContent = formatCurrency(a.dailyWithdrawalLimit);
  document.getElementById('profile-withdrawn-today').textContent = formatCurrency(a.dailyWithdrawnAmount);
  document.getElementById('profile-remaining-today').textContent = formatCurrency(a.remainingDailyLimit);
  document.getElementById('profile-status').textContent = a.status;

  const tbody = document.getElementById('transaction-rows');
  tbody.innerHTML = '';
  if (txs.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">No transaction records found.</td></tr>';
  } else {
    txs.forEach(t => {
      const typeBadge = t.transactionType === 'DEPOSIT'
        ? '<span class="badge badge-deposit">DEPOSIT</span>'
        : '<span class="badge badge-withdraw">WITHDRAWAL</span>';

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="font-mono">#${t.transactionId}</td>
        <td>${t.timestamp}</td>
        <td>${typeBadge}</td>
        <td style="font-weight:600; color:${t.transactionType === 'DEPOSIT' ? '#16a34a' : '#dc2626'}">
          ${t.transactionType === 'DEPOSIT' ? '+' : '-'}${formatCurrency(t.amount)}
        </td>
        <td style="font-weight:600;">${formatCurrency(t.balanceAfter)}</td>
        <td>${t.description}</td>
      `;
      tbody.appendChild(tr);
    });
  }
}

function printStatement() {
  window.print();
}

// 7. Accounts Directory
async function loadAccountsDirectory() {
  const tbody = document.getElementById('directory-rows');
  tbody.innerHTML = '<tr><td colspan="9" class="text-center">Querying Accounts Directory...</td></tr>';

  if (!isOfflineMode) {
    try {
      const res = await fetch(`${API_BASE}/api/accounts`);
      if (res.ok) {
        const accounts = await res.json();
        currentAccountsList = accounts;
        renderAccountsDirectory(accounts);
        return;
      }
    } catch (err) {
      isOfflineMode = true;
    }
  }

  currentAccountsList = getLocalAccounts();
  renderAccountsDirectory(currentAccountsList);
}

function renderAccountsDirectory(accounts) {
  const tbody = document.getElementById('directory-rows');
  tbody.innerHTML = '';

  if (accounts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" class="text-center">No accounts registered yet.</td></tr>';
    return;
  }

  accounts.forEach(a => {
    const typeBadge = a.accountType === 'SAVINGS'
      ? '<span class="badge badge-savings">SAVINGS</span>'
      : '<span class="badge badge-current">CURRENT</span>';

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td class="font-mono"><strong>${a.accountNumber}</strong></td>
      <td><strong>${a.holderName}</strong></td>
      <td>${typeBadge}</td>
      <td style="font-weight:700; color:#166534">${formatCurrency(a.balance)}</td>
      <td>${formatCurrency(a.dailyWithdrawalLimit)}</td>
      <td>${formatCurrency(a.dailyWithdrawnAmount)}</td>
      <td style="font-weight:600;" class="highlight-amber">${formatCurrency(a.remainingDailyLimit)}</td>
      <td><span class="badge badge-active">${a.status}</span></td>
      <td>
        <button class="btn-sm" onclick="selectAccountForAction('${a.accountNumber}')">View &rarr;</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function filterAccountsDirectory() {
  const q = document.getElementById('dir-search').value.toLowerCase().trim();
  if (!q) {
    renderAccountsDirectory(currentAccountsList);
    return;
  }

  const filtered = currentAccountsList.filter(a =>
    a.accountNumber.toLowerCase().includes(q) ||
    a.holderName.toLowerCase().includes(q) ||
    a.accountType.toLowerCase().includes(q)
  );
  renderAccountsDirectory(filtered);
}

function selectAccountForAction(accNum) {
  document.getElementById('info-acc').value = accNum;
  switchTab('tab-info');
  document.getElementById('info-form').dispatchEvent(new Event('submit'));
}
