import { config } from '../config.js';
import { renderBoard } from '../ui/boardUI.js';
import { saveLocal, loadLocal } from '../storage/localStorage.js';
import { getAiMove } from '../ai/aiEngine.js';

let currentBoard = [];
let currentRevealed = [];
let selectedFrom = null;
let legalTargets = new Set();
let history = [];
let undoStack = [];
let currentMode = 'xiangqi';
let currentColor = 'red';
let uiBound = false;
let hasJustUndone = false;
let isGameOver = false;
let winner = null;

export async function initGame() {
  const boardContainer = document.getElementById('boardContainer');
  const statusMessage = document.getElementById('statusMessage');
  const turnInfo = document.getElementById('turnInfo');
  const aiInfo = document.getElementById('aiInfo');

  if (!boardContainer || !statusMessage || !turnInfo || !aiInfo) {
    return;
  }

  if (!uiBound) {
    bindUiControls();
    uiBound = true;
  }

  const activeMode = config.modes[currentMode] || config.modes.xiangqi;
  try {
    const response = await fetch(`${activeMode.apiBase}/init`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) {
      statusMessage.textContent = '未登入或 API 連線失敗';
      turnInfo.textContent = '回合：無';
      aiInfo.textContent = 'AI：未啟動';
      return;
    }

    const state = await response.json();
    currentBoard = state.board || state.pieces || [];
    currentRevealed = state.revealed || [];
    currentColor = state.currentPlayer || 'red';
    history = [];
    undoStack = [];
    selectedFrom = null;
    legalTargets.clear();
    hasJustUndone = false;
    isGameOver = false;
    winner = null;
    renderCurrentBoard(boardContainer);
    setStatus(state.message || `${activeMode.label}棋盤已建立`, currentColor, config.aiLevel || 'random');
  } catch (error) {
    statusMessage.textContent = '無法連線到後端棋盤 API';
    console.error(error);
  }
}

function bindUiControls() {
  const undoBtn = document.getElementById('undoBtn');
  const restartBtn = document.getElementById('restartBtn');
  const saveBtn = document.getElementById('saveBtn');
  const loadBtn = document.getElementById('loadBtn');
  const logoutBtn = document.getElementById('logoutBtn');
  const aiLevel = document.getElementById('aiLevel');

  undoBtn?.addEventListener('click', undoMove);
  restartBtn?.addEventListener('click', initGame);
  saveBtn?.addEventListener('click', saveGame);
  loadBtn?.addEventListener('click', loadGame);
  logoutBtn?.addEventListener('click', () => {
    window.location.href = '/login';
  });

  aiLevel?.addEventListener('change', () => {
    config.aiLevel = aiLevel.value;
    setStatus('AI 難度已更新', currentColor, config.aiLevel);
  });

  document.querySelectorAll('[data-mode]').forEach((button) => {
    button.addEventListener('click', () => {
      const nextMode = button.dataset.mode;
      if (config.modes[nextMode]) {
        currentMode = nextMode;
      } else {
        currentMode = 'xiangqi';
      }
      setStatus(`模式切換：${button.textContent}`, currentColor, config.aiLevel);
      initGame();
    });
  });
}

function renderCurrentBoard(boardContainer) {
  renderBoard({
    board: currentBoard,
    selectedFrom,
    legalTargets,
    container: boardContainer,
    gameMode: currentMode,
    revealed: currentRevealed,
    onCellClick: async ({ x, y, pos, piece }) => {
      if (isGameOver) {
        setStatus(`遊戲已結束！${winner === 'red' ? '紅方' : '黑方'}獲勝`, currentColor, config.aiLevel);
        return;
      }

      if (currentMode === 'darkChess' || currentMode === 'darkAi') {
        await handleDarkCellClick({ x, y, pos, piece });
        return;
      }

      if (!piece) {
        if (selectedFrom) {
          await tryMove(selectedFrom, pos);
        }
        return;
      }

      const isMyPiece = piece && piece.startsWith(currentColor === 'red' ? 'r' : 'b');
      if (piece && !selectedFrom && !isMyPiece) {
        setStatus('請選擇目前回合的棋子', currentColor, config.aiLevel);
        return;
      }

      if (!selectedFrom) {
        selectedFrom = pos;
        const legal = await getLegalMoves(pos);
        legalTargets = new Set(legal.map((move) => move.to));
        renderCurrentBoard(boardContainer);
        setStatus(`選取起點 ${pos}`, currentColor, config.aiLevel);
        return;
      }

      if (selectedFrom === pos) {
        selectedFrom = null;
        legalTargets.clear();
        renderCurrentBoard(boardContainer);
        return;
      }

      if (selectedFrom !== pos && isMyPiece) {
        selectedFrom = pos;
        const legal = await getLegalMoves(pos);
        legalTargets = new Set(legal.map((move) => move.to));
        renderCurrentBoard(boardContainer);
        setStatus(`切換選擇 ${pos}`, currentColor, config.aiLevel);
        return;
      }

      await tryMove(selectedFrom, pos);
    }
  });
}

async function handleDarkCellClick({ x, y, pos, piece }) {
  const isRevealed = Boolean(currentRevealed[y]?.[x]);

  if (!isRevealed) {
    await flipDarkCell(x, y);
    return;
  }

  const isMyPiece = piece && piece.startsWith(currentColor === 'red' ? 'r' : 'b');
  if (piece && !selectedFrom && !isMyPiece) {
    setStatus('請選擇目前回合的棋子', currentColor, config.aiLevel);
    return;
  }

  if (!selectedFrom) {
    selectedFrom = pos;
    const legal = await getLegalMoves(pos);
    legalTargets = new Set(legal.map((move) => move.to));
    renderCurrentBoard(document.getElementById('boardContainer'));
    setStatus(`選取起點 ${pos}`, currentColor, config.aiLevel);
    return;
  }

  if (selectedFrom === pos) {
    selectedFrom = null;
    legalTargets.clear();
    renderCurrentBoard(document.getElementById('boardContainer'));
    return;
  }

  const [selectedX, selectedY] = selectedFrom.split(',').map(Number);
  const selectedPiece = currentBoard[selectedY]?.[selectedX];
  const selectedSide = selectedPiece?.startsWith('r') ? 'r' : 'b';
  if (piece && selectedPiece && piece.startsWith(selectedSide)) {
    selectedFrom = pos;
    const legal = await getLegalMoves(pos);
    legalTargets = new Set(legal.map((move) => move.to));
    renderCurrentBoard(document.getElementById('boardContainer'));
    setStatus(`切換選擇 ${pos}`, currentColor, config.aiLevel);
    return;
  }

  await tryMove(selectedFrom, pos);
}

async function flipDarkCell(x, y) {
  const body = {
    pieces: currentBoard,
    revealed: currentRevealed,
    currentPlayer: currentColor,
    x,
    y
  };

  const response = await fetch('/api/dark/flip', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body)
  });

  const state = await response.json();
  if (!response.ok) {
    setStatus(state.message || '翻棋失敗', currentColor, config.aiLevel);
    return;
  }

  currentBoard = state.pieces;
  currentRevealed = state.revealed;
  currentColor = state.currentPlayer || currentColor;
  renderCurrentBoard(document.getElementById('boardContainer'));
  setStatus(state.message || '翻棋成功', currentColor, config.aiLevel);
  if (currentMode === 'darkAi' && currentColor !== 'red') {
    await runAiTurn('/api/dark');
  }
}

async function tryMove(from, to) {
  const boardContainer = document.getElementById('boardContainer');
  const previousBoard = JSON.parse(JSON.stringify(currentBoard));
  const activeMode = config.modes[currentMode] || config.modes.xiangqi;

  if (currentMode === 'darkChess' || currentMode === 'darkAi') {
    const [fromX, fromY] = from.split(',').map(Number);
    const [toX, toY] = to.split(',').map(Number);
    const response = await fetch(`${activeMode.apiBase}/move`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        pieces: currentBoard,
        revealed: currentRevealed,
        currentPlayer: currentColor,
        fromX,
        fromY,
        toX,
        toY
      })
    });

    const state = await response.json();
    if (!response.ok) {
      setStatus(state.message || '暗棋走法失敗', currentColor, config.aiLevel);
      selectedFrom = null;
      legalTargets.clear();
      renderCurrentBoard(boardContainer);
      return;
    }

    undoStack.push({ board: previousBoard, revealed: JSON.parse(JSON.stringify(currentRevealed)), move: `${from} -> ${to}` });
    history.push(`${from} -> ${to}`);
    currentBoard = state.pieces;
    currentRevealed = state.revealed;
    selectedFrom = null;
    legalTargets.clear();
    hasJustUndone = false;
    currentColor = state.currentPlayer || currentColor;
    renderCurrentBoard(boardContainer);
    renderHistory();
    setStatus(state.message || '暗棋走法成功', currentColor, config.aiLevel);
    if (currentMode === 'darkAi') {
      await runAiTurn(activeMode.apiBase);
    }
    return;
  }

  const response = await fetch(`${activeMode.apiBase}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      from,
      to,
      board: currentBoard,
      color: currentColor
    })
  });

  const state = await response.json();

  if (!response.ok) {
    setStatus(state.message || '走法失敗', currentColor, config.aiLevel);
    selectedFrom = null;
    legalTargets.clear();
    renderCurrentBoard(boardContainer);
    return;
  }

  undoStack.push({ board: previousBoard, revealed: JSON.parse(JSON.stringify(currentRevealed)), move: `${from} -> ${to}` });
  history.push(`${from} -> ${to}`);
  currentBoard = state.board;
  selectedFrom = null;
  legalTargets.clear();
  hasJustUndone = false;
  currentColor = state.currentPlayer || currentColor;

  // 檢查遊戲是否結束
  if (state.gameOver && state.winner) {
    isGameOver = true;
    winner = state.winner;
  }

  renderCurrentBoard(boardContainer);
  renderHistory();

  if (state.gameOver && state.winner) {
    setStatus(`遊戲結束！${state.winner === 'red' ? '紅方' : '黑方'}獲勝！`, currentColor, config.aiLevel);
  } else {
    setStatus(state.message || '走法成功', currentColor, config.aiLevel);
  }

  if (currentMode === 'ai' || currentMode === 'darkAi') {
    await runAiTurn(activeMode.apiBase);
  }
}

async function runAiTurn(apiBase) {
  const aiMove = await getAiMove({
    board: currentBoard,
    color: currentColor,
    level: config.aiLevel,
    mode: currentMode,
    revealed: currentRevealed
  });

  if (!aiMove?.from || !aiMove?.to) {
    return;
  }

  const aiResponse = await fetch(`${apiBase}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(
      currentMode === 'darkAi'
        ? {
            pieces: currentBoard,
            revealed: currentRevealed,
            currentPlayer: currentColor,
            fromX: Number(aiMove.from.split(',')[0]),
            fromY: Number(aiMove.from.split(',')[1]),
            toX: Number(aiMove.to.split(',')[0]),
            toY: Number(aiMove.to.split(',')[1])
          }
        : {
            from: aiMove.from,
            to: aiMove.to,
            board: currentBoard,
            color: currentColor
          }
    )
  });

  const aiState = await aiResponse.json();
  if (!aiResponse.ok) {
    return;
  }

  if (currentMode === 'darkAi') {
    currentBoard = aiState.pieces;
    currentRevealed = aiState.revealed;
    currentColor = aiState.currentPlayer || currentColor;
    history.push(`${aiMove.from} -> ${aiMove.to}`);
    renderCurrentBoard(document.getElementById('boardContainer'));
    renderHistory();
    setStatus('AI 已完成回合', currentColor, config.aiLevel);
    return;
  }

  currentBoard = aiState.board;
  currentColor = aiState.currentPlayer || currentColor;
  history.push(`${aiMove.from} -> ${aiMove.to}`);
  renderCurrentBoard(document.getElementById('boardContainer'));
  renderHistory();
  setStatus('AI 已完成回合', currentColor, config.aiLevel);
}

async function getLegalMoves(fromPos) {
  const [sx, sy] = fromPos.split(',').map(Number);
  const piece = currentBoard[sy][sx];
  if (!piece) {
    return [];
  }

  const activeMode = config.modes[currentMode] || config.modes.xiangqi;
  const response = await fetch(`${activeMode.apiBase}/legal`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      board: currentBoard,
      color: piece.startsWith('r') ? 'red' : 'black',
      revealed: currentRevealed
    })
  });

  if (!response.ok) {
    return [];
  }

  return response.json();
}

function undoMove() {
  if (hasJustUndone) {
    setStatus('必須先走棋，才能再次悔棋', currentColor, config.aiLevel);
    return;
  }

  const snapshot = undoStack.pop();
  if (!snapshot) {
    setStatus('沒有可悔的步驟', currentColor, config.aiLevel);
    return;
  }

  currentBoard = snapshot.board;
  currentRevealed = snapshot.revealed || [];
  history.pop();
  selectedFrom = null;
  legalTargets.clear();
  hasJustUndone = true;
  renderCurrentBoard(document.getElementById('boardContainer'));
  renderHistory();
  setStatus('悔棋成功（必須先走棋才能再悔）', currentColor, config.aiLevel);
}

function saveGame() {
  saveLocal(`${config.modes[currentMode].boardKey}_board`, currentBoard);
  saveLocal(`${config.modes[currentMode].boardKey}_history`, history);
  saveLocal(`${config.modes[currentMode].boardKey}_revealed`, currentRevealed);
  setStatus('棋局已儲存到 LocalStorage', currentColor, config.aiLevel);
}

function loadGame() {
  const savedBoard = loadLocal(`${config.modes[currentMode].boardKey}_board`);
  const savedHistory = loadLocal(`${config.modes[currentMode].boardKey}_history`);
  const savedRevealed = loadLocal(`${config.modes[currentMode].boardKey}_revealed`);

  if (!savedBoard) {
    setStatus('沒有可載入的棋局', currentColor, config.aiLevel);
    return;
  }

  currentBoard = savedBoard;
  history = savedHistory || [];
  currentRevealed = savedRevealed || [];
  selectedFrom = null;
  legalTargets.clear();
  undoStack = [];
  hasJustUndone = false;
  renderCurrentBoard(document.getElementById('boardContainer'));
  renderHistory();
  setStatus('棋局已載入', currentColor, config.aiLevel);
}

function renderHistory() {
  const historyList = document.getElementById('historyList');
  if (!historyList) return;

  historyList.innerHTML = history.length
    ? history.map((step, index) => `<div>${index + 1}. ${step}</div>`).join('')
    : '尚無紀錄';
}

function toTurnLabel(value) {
  if (value === 'black') return '黑方';
  if (value === 'red') return '紅方';
  return value || '無';
}

function toAiLabel(level) {
  if (level === 'minimax') return '中級';
  if (level === 'alphabeta') return '高級';
  return '初級';
}

function getModeDisplayName(mode) {
  switch (mode) {
    case 'xiangqi':
      return '中國象棋';
    case 'pvp':
      return '雙人對弈';
    case 'ai':
      return '雙人對弈 (AI 對戰)';
    case 'darkChess':
      return '暗棋';
    case 'darkAi':
      return '暗棋 (AI 對戰)';
    default:
      return config.modes[mode]?.label || '未知模式';
  }
}

function setStatus(message, turn, aiText) {
  const statusMessage = document.getElementById('statusMessage');
  const modeInfo = document.getElementById('modeInfo');
  const turnInfo = document.getElementById('turnInfo');
  const leftTurnInfo = document.getElementById('leftTurnInfo');
  const aiInfo = document.getElementById('aiInfo');

  if (statusMessage) statusMessage.textContent = message;
  if (modeInfo) modeInfo.textContent = `模式：${getModeDisplayName(currentMode)}`;
  if (turnInfo) turnInfo.textContent = `回合：${toTurnLabel(turn)}`;
  if (leftTurnInfo) leftTurnInfo.textContent = `輪到：${toTurnLabel(turn)}`;
  if (aiInfo) aiInfo.textContent = `AI：${toAiLabel(aiText)}`;
}
