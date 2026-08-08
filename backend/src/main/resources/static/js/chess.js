let currentChessBoard = [];
let selectedFrom = null;
let legalTargets = new Set();
let moveHistory = [];
let undoStack = [];

window.addEventListener('load', () => {
    initChess();
});

async function initChess() {
    try {
        const response = await fetch('/api/chess/init', { method: 'POST' });
        const state = await response.json();
        currentChessBoard = state.board;
        selectedFrom = null;
        legalTargets.clear();
        moveHistory = [];
        undoStack = [];
        document.getElementById('chessFrom').value = '';
        document.getElementById('chessTo').value = '';
        updateStatus(state.message || '中國象棋棋盤已初始化', '紅方', 'AI 資訊：未啟動');
        renderChessBoard(state);
    } catch (error) {
        updateStatus('初始化失敗，請確認系統已啟動。', '紅方', 'AI 資訊：未啟動');
        console.error(error);
    }
}

async function moveChess() {
    const from = document.getElementById('chessFrom').value;
    const to = document.getElementById('chessTo').value;
    if (!from || !to) {
        updateStatus('請先選取起點和終點。', '紅方', 'AI 資訊：未啟動');
        return;
    }

    const previousBoard = JSON.parse(JSON.stringify(currentChessBoard));
    const response = await fetch('/api/chess/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ from, to, board: currentChessBoard, color: 'red' })
    });
    const state = await response.json();

    if (!response.ok) {
        updateStatus(state.message || '走法失敗。', '紅方', 'AI 資訊：未啟動');
        return;
    }

    undoStack.push({ board: previousBoard, move: `${from} -> ${to}` });
    moveHistory.push(`${from} -> ${to}`);
    currentChessBoard = state.board;
    selectedFrom = null;
    legalTargets.clear();
    document.getElementById('chessFrom').value = '';
    document.getElementById('chessTo').value = '';
    updateStatus(state.message || '走法成功', '黑方', 'AI 資訊：請選擇 AI');
    renderChessBoard(state);
    renderHistory();
}

async function suggestChess() {
    const level = document.getElementById('aiLevel')?.value || 'random';
    const response = await fetch('/api/chess/ai', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ board: currentChessBoard, color: 'red', level })
    });
    const move = await response.json();
    const message = move.from && move.to ? `AI 建議: ${move.from} -> ${move.to}` : '無合法建議';
    updateStatus(message, '紅方', `AI 資訊：${level}`);
}

async function aiChess() {
    const level = document.getElementById('aiLevel')?.value || 'random';
    const response = await fetch('/api/chess/ai', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ board: currentChessBoard, color: 'black', level })
    });
    const move = await response.json();
    if (!move.from || !move.to) {
        updateStatus('AI 無法產生走法', '紅方', `AI 資訊：${level}`);
        return;
    }
    document.getElementById('chessFrom').value = move.from;
    document.getElementById('chessTo').value = move.to;
    updateStatus(`AI 建議: ${move.from} -> ${move.to}`, '黑方', `AI 資訊：${level}`);
}

function renderChessBoard(state) {
    const container = document.getElementById('boardContainer');
    if (!container) return;

    container.innerHTML = '';
    const table = document.createElement('table');
    table.className = 'chess-board';

    state.board.forEach((row, y) => {
        const tr = document.createElement('tr');
        row.forEach((cell, x) => {
            const td = document.createElement('td');
            td.textContent = formatPiece(cell);
            td.className = cell && cell.startsWith('r') ? 'red' : cell && cell.startsWith('b') ? 'black' : '';
            const pos = `${x},${y}`;
            td.addEventListener('click', async () => {
                await handleChessCellClick(pos);
            });
            if (selectedFrom === pos) {
                td.classList.add('selected');
            }
            if (legalTargets.has(pos)) {
                td.classList.add('highlight');
            }
            enableDragHandlers(td, pos, !!cell);
            tr.appendChild(td);
        });
        table.appendChild(tr);
    });

    container.appendChild(table);
}

function formatPiece(piece) {
    if (!piece) return '';
    const map = {
        'R': '車',
        'N': '馬',
        'E': '象',
        'A': '仕',
        'K': '將',
        'C': '炮',
        'P': '兵'
    };
    const color = piece.startsWith('r') ? '紅' : '黑';
    const label = map[piece[1]] || piece[1];
    return `${color}${label}`;
}

async function handleChessCellClick(pos) {
    const [x, y] = pos.split(',').map(Number);
    const piece = currentChessBoard[y]?.[x];

    if (!selectedFrom) {
        if (!piece) {
            updateStatus('請選擇一枚棋子。', '紅方', 'AI 資訊：未啟動');
            return;
        }
        selectedFrom = pos;
        document.getElementById('chessFrom').value = pos;
        await fetchLegalTargets(pos);
        updateStatus(`選取起點 ${pos}`, '紅方', 'AI 資訊：未啟動');
        renderChessBoard({ board: currentChessBoard, message: document.getElementById('statusBar').textContent });
        return;
    }

    if (selectedFrom === pos) {
        selectedFrom = null;
        legalTargets.clear();
        document.getElementById('chessFrom').value = '';
        renderChessBoard({ board: currentChessBoard, message: '已取消選擇' });
        return;
    }

    const [selectedX, selectedY] = selectedFrom.split(',').map(Number);
    const selectedPiece = currentChessBoard[selectedY]?.[selectedX];
    if (piece && selectedPiece && piece.startsWith(selectedPiece[0])) {
        selectedFrom = pos;
        document.getElementById('chessFrom').value = pos;
        await fetchLegalTargets(pos);
        updateStatus(`切換選擇 ${pos}`, '紅方', 'AI 資訊：未啟動');
        renderChessBoard({ board: currentChessBoard, message: `切換選擇 ${pos}` });
        return;
    }

    document.getElementById('chessTo').value = pos;
    await moveChess();
}

async function fetchLegalTargets(fromPos) {
    legalTargets.clear();
    const [sx, sy] = fromPos.split(',');
    const piece = currentChessBoard[parseInt(sy)][parseInt(sx)];
    if (!piece) return;
    const response = await fetch('/api/chess/legal', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ board: currentChessBoard, color: piece.startsWith('r') ? 'red' : 'black' })
    });
    if (!response.ok) return;
    const moves = await response.json();
    moves.forEach(m => legalTargets.add(m.to));
}

function enableDragHandlers(td, pos, hasPiece) {
    td.draggable = !!hasPiece;
    td.addEventListener('dragstart', (e) => {
        e.dataTransfer.setData('text/plain', pos);
    });
    td.addEventListener('dragover', (e) => e.preventDefault());
    td.addEventListener('drop', (e) => {
        e.preventDefault();
        const from = e.dataTransfer.getData('text/plain');
        const to = pos;
        document.getElementById('chessFrom').value = from;
        document.getElementById('chessTo').value = to;
        selectedFrom = null;
        moveChess();
    });
}

function undoChess() {
    const snapshot = undoStack.pop();
    if (!snapshot) {
        updateStatus('沒有可悔的步驟。', '紅方', 'AI 資訊：未啟動');
        return;
    }
    currentChessBoard = snapshot.board;
    moveHistory.pop();
    renderChessBoard({ board: currentChessBoard, message: '悔棋成功' });
    renderHistory();
    updateStatus('悔棋成功', '紅方', 'AI 資訊：未啟動');
}

function saveChessGame() {
    localStorage.setItem('smartchess_board', JSON.stringify(currentChessBoard));
    localStorage.setItem('smartchess_history', JSON.stringify(moveHistory));
    updateStatus('棋局已儲存到 LocalStorage', '紅方', 'AI 資訊：未啟動');
}

function loadChessGame() {
    const savedBoard = localStorage.getItem('smartchess_board');
    const savedHistory = localStorage.getItem('smartchess_history');
    if (!savedBoard) {
        updateStatus('沒有可載入的棋局。', '紅方', 'AI 資訊：未啟動');
        return;
    }
    currentChessBoard = JSON.parse(savedBoard);
    moveHistory = JSON.parse(savedHistory || '[]');
    renderChessBoard({ board: currentChessBoard, message: '棋局已載入' });
    renderHistory();
    updateStatus('棋局已載入', '紅方', 'AI 資訊：未啟動');
}

function renderHistory() {
    const history = document.getElementById('moveHistory');
    if (!history) return;
    history.innerHTML = moveHistory.length ? moveHistory.map((step, index) => `<div>${index + 1}. ${step}</div>`).join('') : '<div>尚無走法紀錄</div>';
}

function updateStatus(message, turn, aiText) {
    const statusBar = document.getElementById('statusBar');
    const turnInfo = document.getElementById('turnInfo');
    const aiInfo = document.getElementById('aiInfo');
    if (statusBar) statusBar.textContent = message;
    if (turnInfo) turnInfo.textContent = `回合：${turn}`;
    if (aiInfo) aiInfo.textContent = `AI 資訊：${aiText}`;
}

function formatPiece(piece) {
    if (!piece) return '';
    const labels = {
        r: { R: '\u4fe5', N: '\u508c', E: '\u76f8', A: '\u4ed5', K: '\u5e25', C: '\u7832', P: '\u5175' },
        b: { R: '\u8eca', N: '\u99ac', E: '\u8c61', A: '\u58eb', K: '\u5c07', C: '\u5305', P: '\u5352' }
    };
    return labels[piece[0]]?.[piece[1]] || piece[1];
}

function renderChessBoard(state) {
    const container = document.getElementById('boardContainer');
    if (!container) return;
    container.innerHTML = '';

    const frame = document.createElement('div');
    frame.className = 'xiangqi-board-frame';
    const surface = document.createElement('div');
    surface.className = 'xiangqi-board-surface';
    const topGrid = document.createElement('div');
    topGrid.className = 'xiangqi-grid xiangqi-grid-top';
    const bottomGrid = document.createElement('div');
    bottomGrid.className = 'xiangqi-grid xiangqi-grid-bottom';
    surface.append(topGrid, bottomGrid);

    state.board.forEach((row, y) => row.forEach((piece, x) => {
        const pos = `${x},${y}`;
        const point = document.createElement('button');
        point.type = 'button';
        point.className = 'intersection-point';
        point.style.left = `${(x / 8) * 100}%`;
        point.style.top = `${(y / 9) * 100}%`;
        point.addEventListener('click', () => handleChessCellClick(pos));
        if (selectedFrom === pos) point.classList.add('selected');
        if (legalTargets.has(pos)) point.classList.add('highlight');
        if (piece) {
            const token = document.createElement('span');
            token.className = `piece-token ${piece.startsWith('r') ? 'red' : 'black'}`;
            token.textContent = formatPiece(piece);
            point.appendChild(token);
        }
        surface.appendChild(point);
    }));

    frame.appendChild(surface);
    container.appendChild(frame);
}

async function moveChess() {
    const from = document.getElementById('chessFrom').value;
    const to = document.getElementById('chessTo').value;
    if (!from || !to) return;
    const previousBoard = JSON.parse(JSON.stringify(currentChessBoard));
    const response = await fetch('/api/chess/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ from, to, board: currentChessBoard, color: 'red' })
    });
    const state = await response.json();
    if (!response.ok) {
        updateStatus(state.message || 'Move rejected', 'red', 'AI ready');
        return;
    }
    undoStack.push({ board: previousBoard, move: `${from} -> ${to}` });
    moveHistory.push(`${from} -> ${to}`);
    currentChessBoard = state.board;
    selectedFrom = null;
    legalTargets.clear();
    document.getElementById('chessFrom').value = '';
    document.getElementById('chessTo').value = '';
    renderChessBoard(state);
    renderHistory();
    await playAiTurn();
}

async function playAiTurn() {
    const level = document.getElementById('aiLevel')?.value || 'random';
    const moveResponse = await fetch('/api/chess/ai', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ board: currentChessBoard, color: 'black', level })
    });
    const move = await moveResponse.json();
    if (!move.from || !move.to) {
        updateStatus('AI has no legal move', 'red', `AI: ${level}`);
        return;
    }
    const applyResponse = await fetch('/api/chess/move', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ from: move.from, to: move.to, board: currentChessBoard, color: 'black' })
    });
    const state = await applyResponse.json();
    if (!applyResponse.ok) {
        updateStatus(state.message || 'AI move rejected', 'red', `AI: ${level}`);
        return;
    }
    currentChessBoard = state.board;
    moveHistory.push(`${move.from} -> ${move.to}`);
    renderChessBoard(state);
    renderHistory();
    updateStatus(`AI: ${move.from} -> ${move.to}`, 'red', `AI: ${level}`);
}

async function aiChess() {
    await playAiTurn();
}
