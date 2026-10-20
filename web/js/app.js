/**
 * Apex Banking Management System - Web Client JS
 * Communicates with Java Backend REST API endpoints
 */

const API_BASE = '';
let currentAccountsList = [];
let demoAccountIndex = 0;

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
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
  // Update nav buttons
  document.querySelectorAll('.nav-item').forEach(btn => {
    if (btn.getAttribute('data-tab') === tabId) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });

  // Update tab panes
  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.remove('active');
  });

  const activePane = document.getElementById(tabId);
  if (activePane) {
    activePane.classList.add('active');
  }

  // Update header text
  updateHeaderText(tabId);

  // Trigger data loads
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

// Format Currency
function formatCurrency(val) {
  if (val === undefined || val === null || isNaN(val)) return '₹0.00';
  return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Alert Notifications
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
    const accounts = await res.json();
    currentAccountsList = accounts;

    let totalReserves = 0;
    accounts.forEach(a => totalReserves += Number(a.balance));

    document.getElementById('stat-total-accounts').textContent = accounts.length;
    document.getElementById('stat-total-reserves').textContent = formatCurrency(totalReserves);
  } catch (err) {
    console.error('Error fetching dashboard data:', err);
  }
}

// Setup Quick Demo Fill Button
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

    // Pre-fill active forms
    const depAcc = document.getElementById('deposit-acc');
    const withAcc = document.getElementById('withdraw-acc');
    const withPin = document.getElementById('withdraw-pin');
    const balAcc = document.getElementById('balance-acc');
    const balPin = document.getElementById('balance-pin');
    const infoAcc = document.getElementById('info-acc');

    if (depAcc) depAcc.value = acc.accountNumber;
    if (withAcc) withAcc.value = acc.accountNumber;
    if (withPin) withPin.value = '1234'; // Default demo PIN
    if (balAcc) balAcc.value = acc.accountNumber;
    if (balPin) balPin.value = '1234';
    if (infoAcc) infoAcc.value = acc.accountNumber;

    showAlert(`Pre-filled active inputs with Demo Account: ${acc.accountNumber} (${acc.holderName}, PIN: 1234 or 4321)`, 'success', 4000);
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

  try {
    const res = await fetch(`${API_BASE}/api/accounts/create`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (data.success) {
      showAlert(`🎉 Account Created! Account Number: <strong>${data.account.accountNumber}</strong> for <strong>${data.account.holderName}</strong>. PIN: <strong>${payload.pin}</strong>.`, 'success', 10000);
      document.getElementById('create-account-form').reset();
      onAccountTypeChanged();
      loadDashboardData();
    } else {
      showAlert(`❌ Error: ${data.message}`, 'error', 7000);
    }
  } catch (err) {
    showAlert('Server communication error: ' + err.message, 'error');
  }
}

// 3. Feature 2: Deposit Money
async function quickVerifyAccount(accInputId) {
  const accNum = document.getElementById(accInputId).value.trim();
  if (!accNum) {
    showAlert('Please enter an account number first.', 'warning');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/accounts/info`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum })
    });
    const data = await res.json();
    if (data.success) {
      const box = document.getElementById('deposit-account-preview');
      box.classList.remove('hidden');
      box.innerHTML = `
        <strong>Account Holder:</strong> ${data.account.holderName} (${data.account.accountType})<br>
        <strong>Current Balance:</strong> ${formatCurrency(data.account.balance)} • <strong>Daily Limit:</strong> ${formatCurrency(data.account.dailyWithdrawalLimit)}
      `;
    } else {
      showAlert(data.message, 'error');
    }
  } catch (err) {
    showAlert('Error verifying account: ' + err.message, 'error');
  }
}

async function handleDeposit(event) {
  event.preventDefault();

  const accNum = document.getElementById('deposit-acc').value.trim();
  const amount = document.getElementById('deposit-amount').value;
  const desc = document.getElementById('deposit-desc').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/accounts/deposit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum, amount, description: desc })
    });

    const data = await res.json();
    if (data.success) {
      showAlert(`✅ Deposit of ${formatCurrency(amount)} successful! New Balance: <strong>${formatCurrency(data.account.balance)}</strong> for ${data.account.holderName}.`, 'success', 8000);
      document.getElementById('deposit-amount').value = '';
      quickVerifyAccount('deposit-acc');
      loadDashboardData();
    } else {
      showAlert(`❌ Deposit Failed: ${data.message}`, 'error', 7000);
    }
  } catch (err) {
    showAlert('Error: ' + err.message, 'error');
  }
}

// 4. Features 3 & 4: Withdraw Money & Honor Daily Limit
async function checkWithdrawalStatus() {
  const accNum = document.getElementById('withdraw-acc').value.trim();
  const pin = document.getElementById('withdraw-pin').value.trim();

  if (!accNum || !pin) {
    showAlert('Please provide both Account Number and PIN to verify limits.', 'warning');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/accounts/info`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum, pin })
    });

    const data = await res.json();
    if (data.success) {
      const a = data.account;
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
    } else {
      showAlert(data.message, 'error');
    }
  } catch (err) {
    showAlert('Error inspecting status: ' + err.message, 'error');
  }
}

async function handleWithdraw(event) {
  event.preventDefault();

  const accNum = document.getElementById('withdraw-acc').value.trim();
  const pin = document.getElementById('withdraw-pin').value.trim();
  const amount = document.getElementById('withdraw-amount').value;
  const desc = document.getElementById('withdraw-desc').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/accounts/withdraw`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum, pin, amount, description: desc })
    });

    const data = await res.json();
    if (data.success) {
      showAlert(`✅ Withdrawal of ${formatCurrency(amount)} Authorized! Remaining Balance: <strong>${formatCurrency(data.account.balance)}</strong>. Today's Available Limit Left: <strong>${formatCurrency(data.account.remainingDailyLimit)}</strong>.`, 'success', 8000);
      document.getElementById('withdraw-amount').value = '';
      checkWithdrawalStatus();
      loadDashboardData();
    } else {
      if (data.errorType === 'DAILY_LIMIT_EXCEEDED') {
        showAlert(`⚠️ <strong>TRANSACTION REJECTED: DAILY WITHDRAWAL LIMIT HONORED!</strong><br>${data.message}`, 'warning', 10000);
      } else {
        showAlert(`❌ Withdrawal Denied: ${data.message}`, 'error', 7000);
      }
    }
  } catch (err) {
    showAlert('Error: ' + err.message, 'error');
  }
}

// 5. Feature 5: Check Balance
async function handleCheckBalance(event) {
  event.preventDefault();

  const accNum = document.getElementById('balance-acc').value.trim();
  const pin = document.getElementById('balance-pin').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/accounts/balance`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum, pin })
    });

    const data = await res.json();
    if (data.success) {
      const a = data.account;
      const card = document.getElementById('balance-result-card');
      card.classList.remove('hidden');

      document.getElementById('balance-result-amount').textContent = formatCurrency(a.balance);
      document.getElementById('bal-holder').textContent = a.holderName;
      document.getElementById('bal-acc-num').textContent = a.accountNumber;
      document.getElementById('bal-type').textContent = a.accountType;
      document.getElementById('bal-status').textContent = a.status;
      document.getElementById('bal-limit').textContent = formatCurrency(a.dailyWithdrawalLimit);
      document.getElementById('bal-remain').textContent = formatCurrency(a.remainingDailyLimit);
    } else {
      showAlert(data.message, 'error');
    }
  } catch (err) {
    showAlert('Error querying balance: ' + err.message, 'error');
  }
}

// 6. Feature 6: Display Account Information & Statement
async function handleFetchAccountInfo(event) {
  event.preventDefault();

  const accNum = document.getElementById('info-acc').value.trim();
  const pin = document.getElementById('info-pin').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/accounts/info`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber: accNum, pin })
    });

    const data = await res.json();
    if (data.success) {
      const a = data.account;
      const txs = data.transactions || [];
      const disp = document.getElementById('account-info-display');
      disp.classList.remove('hidden');

      // Initials Avatar
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

      // Populate Transactions
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
    } else {
      showAlert(data.message, 'error');
    }
  } catch (err) {
    showAlert('Error fetching account details: ' + err.message, 'error');
  }
}

function printStatement() {
  window.print();
}

// 7. Accounts Directory
async function loadAccountsDirectory() {
  const tbody = document.getElementById('directory-rows');
  tbody.innerHTML = '<tr><td colspan="9" class="text-center">Querying JDBC Database...</td></tr>';

  try {
    const res = await fetch(`${API_BASE}/api/accounts`);
    const accounts = await res.json();
    currentAccountsList = accounts;
    renderAccountsDirectory(accounts);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="9" class="text-center" style="color:red">Error: ${err.message}</td></tr>`;
  }
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
