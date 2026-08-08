let currentDarkBoard = [];
let currentRevealed = [];
let selectedDarkFrom = null;
let legalDarkTargets = new Set();
let undoDarkStack = [];
let currentDarkTurn = 'red';

function updateDarkTurnInfo(turn) {
  const turnInfo = document.getElementById('turnInfo');
  const leftTurnInfo = document.getElementById('leftTurnInfo');
  const label = turn === 'black' ? '黑方' : '紅方';
  if (turnInfo) turnInfo.textContent = `回合：${label}`;
  if (leftTurnInfo) leftTurnInfo.textContent = `輪到：${label}`;
}

initDark();

async function initDark() {
  const response = await fetch('/api/dark/init', { method: 'POST' });
  const state = await response.json();
  currentDarkBoard = state.pieces;
  currentRevealed = state.revealed;
  currentDarkTurn = state.currentPlayer || 'red';
  updateDarkTurnInfo(currentDarkTurn);
  selectedDarkFrom = null;
  legalDarkTargets.clear();
  undoDarkStack = [];
  document.getElementById('darkFromX').value = '';
  document.getElementById('darkFromY').value = '';
  document.getElementById('darkToX').value = '';
  document.getElementById('darkToY').value = '';
  renderDarkBoard(state);
}

async function flipDark() {
  const x = parseInt(document.getElementById('darkFlipX').value, 10);
  const y = parseInt(document.getElementById('darkFlipY').value, 10);
  const response = await fetch('/api/dark/flip', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pieces: currentDarkBoard, revealed: currentRevealed, currentPlayer: currentDarkTurn, x, y })
  });
  const state = await response.json();
  if (!response.ok) {
    document.getElementById('message').textContent = state.message;
    return;
  }
  currentDarkBoard = state.pieces;
  currentRevealed = state.revealed;
  currentDarkTurn = state.currentPlayer || currentDarkTurn;
  updateDarkTurnInfo(currentDarkTurn);
  renderDarkBoard({ ...state, message: state.message });
}

async function moveDark(fromXOverride, fromYOverride, toXOverride, toYOverride) {
  const fromX = Number.isInteger(fromXOverride) ? fromXOverride : parseInt(document.getElementById('darkFromX').value, 10);
  const fromY = Number.isInteger(fromYOverride) ? fromYOverride : parseInt(document.getElementById('darkFromY').value, 10);
  const toX = Number.isInteger(toXOverride) ? toXOverride : parseInt(document.getElementById('darkToX').value, 10);
  const toY = Number.isInteger(toYOverride) ? toYOverride : parseInt(document.getElementById('darkToY').value, 10);
  if (isNaN(fromX) || isNaN(fromY) || isNaN(toX) || isNaN(toY)) {
    document.getElementById('message').textContent = '請先選取起點與終點。';
    return;
  }

  const previousBoard = JSON.parse(JSON.stringify(currentDarkBoard));
  const previousRevealed = JSON.parse(JSON.stringify(currentRevealed));
  const response = await fetch('/api/dark/move', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ pieces: currentDarkBoard, revealed: currentRevealed, currentPlayer: currentDarkTurn, fromX, fromY, toX, toY })
  });
  const state = await response.json();
  if (!response.ok) {
    document.getElementById('message').textContent = state.message;
    return;
  }
  undoDarkStack.push({ board: previousBoard, revealed: previousRevealed });
  currentDarkBoard = state.pieces;
  currentRevealed = state.revealed;
  currentDarkTurn = state.currentPlayer || currentDarkTurn;
  updateDarkTurnInfo(currentDarkTurn);
  selectedDarkFrom = null;
  legalDarkTargets.clear();
  document.getElementById('darkFromX').value = '';
  document.getElementById('darkFromY').value = '';
  document.getElementById('darkToX').value = '';
  document.getElementById('darkToY').value = '';
  renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: state.message });
}

async function aiDark() {
  const response = await fetch('/api/dark/ai', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ board: currentDarkBoard, color: 'red', level: 'random' })
  });
  const move = await response.json();
  if (!move.from || !move.to) {
    document.getElementById('message').textContent = 'AI 無法產生建議';
    return;
  }
  document.getElementById('darkFromX').value = move.from.split(',')[0];
  document.getElementById('darkFromY').value = move.from.split(',')[1];
  document.getElementById('darkToX').value = move.to.split(',')[0];
  document.getElementById('darkToY').value = move.to.split(',')[1];
  document.getElementById('message').textContent = `AI 建議: ${move.from} -> ${move.to}`;
}

function undoDark() {
  const snapshot = undoDarkStack.pop();
  if (!snapshot) {
    document.getElementById('message').textContent = '沒有可悔的步驟';
    return;
  }
  currentDarkBoard = snapshot.board;
  currentRevealed = snapshot.revealed;
  selectedDarkFrom = null;
  legalDarkTargets.clear();
  renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: '悔棋成功' });
}

function renderDarkBoard(state) {
  const message = state.message || '暗棋已更新';
  document.getElementById('message').textContent = message;
  const container = document.getElementById('boardContainer');
  container.innerHTML = '';
  const table = document.createElement('table');
  table.className = 'dark-board';

  state.pieces.forEach((row, y) => {
    const tr = document.createElement('tr');
    row.forEach((cell, x) => {
      const td = document.createElement('td');
      const revealed = state.revealed[y][x];
      const pos = `${x},${y}`;
      td.classList.add(revealed ? 'revealed' : 'hidden');
      td.addEventListener('click', async () => {
        await handleDarkCellClick(x, y, revealed);
      });
      if (selectedDarkFrom === pos) {
        td.classList.add('selected');
      }
      if (legalDarkTargets.has(pos)) {
        td.classList.add('highlight');
      }
      if (revealed) {
        td.textContent = formatDarkPiece(cell);
      } else {
        td.innerHTML = '';
      }
      tr.appendChild(td);
    });
    table.appendChild(tr);
  });

  container.appendChild(table);
}

async function handleDarkCellClick(x, y, revealed) {
  const pos = `${x},${y}`;
  const selectedX = selectedDarkFrom ? Number(selectedDarkFrom.split(',')[0]) : null;
  const selectedY = selectedDarkFrom ? Number(selectedDarkFrom.split(',')[1]) : null;
  const selectedPiece = selectedDarkFrom ? currentDarkBoard[selectedY]?.[selectedX] : null;
  const clickedPiece = currentDarkBoard[y][x];

  if (!revealed) {
    await flipDarkCell(x, y);
    selectedDarkFrom = null;
    legalDarkTargets.clear();
    return;
  }

  if (!selectedDarkFrom) {
    selectedDarkFrom = pos;
    document.getElementById('darkFromX').value = x;
    document.getElementById('darkFromY').value = y;
    await fetchDarkLegalTargets(pos);
    renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: `選取起點 ${pos}` });
    return;
  }

  if (selectedDarkFrom === pos) {
    selectedDarkFrom = null;
    legalDarkTargets.clear();
    document.getElementById('darkFromX').value = '';
    document.getElementById('darkFromY').value = '';
    renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: '已取消選擇' });
    return;
  }

  if (clickedPiece && selectedPiece && clickedPiece.charAt(0) === selectedPiece.charAt(0)) {
    selectedDarkFrom = pos;
    document.getElementById('darkFromX').value = x;
    document.getElementById('darkFromY').value = y;
    await fetchDarkLegalTargets(pos);
    renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: `切換選擇 ${pos}` });
    return;
  }

  document.getElementById('darkFromX').value = selectedX;
  document.getElementById('darkFromY').value = selectedY;
  document.getElementById('darkToX').value = x;
  document.getElementById('darkToY').value = y;
  await moveDark(selectedX, selectedY, x, y);
}

async function fetchDarkLegalTargets(fromPos) {
  legalDarkTargets.clear();
  const [sx, sy] = fromPos.split(',').map(Number);
  const piece = currentDarkBoard[sy]?.[sx];
  if (!piece) return;

  const response = await fetch('/api/dark/legal', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      board: currentDarkBoard,
      color: piece.startsWith('r') ? 'red' : 'black',
      revealed: currentRevealed
    })
  });
  if (!response.ok) return;
  const moves = await response.json();
  moves.forEach((move) => legalDarkTargets.add(move.to));
}

async function flipDarkCell(x, y) {
  const response = await fetch('/api/dark/flip', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pieces: currentDarkBoard,
      revealed: currentRevealed,
      currentPlayer: null,
      x,
      y
    })
  });

  const state = await response.json();
  if (!response.ok) {
    document.getElementById('message').textContent = state.message || '翻棋失敗';
    return;
  }

  currentDarkBoard = state.pieces;
  currentRevealed = state.revealed;
  renderDarkBoard({ pieces: currentDarkBoard, revealed: currentRevealed, message: state.message || '翻棋成功' });
}

function formatDarkPiece(piece) {
  if (!piece) return '';
  const labels = {
    r: { R: '\u4fe5', N: '\u508c', E: '\u76f8', A: '\u4ed5', K: '\u5e25', C: '\u7832', P: '\u5175' },
    b: { R: '\u8eca', N: '\u99ac', E: '\u8c61', A: '\u58eb', K: '\u5c07', C: '\u5305', P: '\u5352' }
  };
  return labels[piece[0]]?.[piece[1]] || piece[1];
}
