export function renderBoard({ board, selectedFrom, legalTargets, onCellClick, container, gameMode, revealed = [] }) {
  if (gameMode === 'darkChess' || gameMode === 'darkAi') {
    renderDarkChessBoard({ board, selectedFrom, legalTargets, onCellClick, container, revealed });
    return;
  }

  renderXiangqiBoard({ board, selectedFrom, legalTargets, onCellClick, container });
}

function renderXiangqiBoard({ board, selectedFrom, legalTargets, onCellClick, container }) {
  container.innerHTML = '';

  const boardFrame = document.createElement('div');
  boardFrame.className = 'xiangqi-board-frame';

  const boardSurface = document.createElement('div');
  boardSurface.className = 'xiangqi-board-surface';

  const palaceTop = document.createElement('div');
  palaceTop.className = 'xiangqi-palace-box xiangqi-palace-box-top';
  const palaceBottom = document.createElement('div');
  palaceBottom.className = 'xiangqi-palace-box xiangqi-palace-box-bottom';

  const intersections = [];
  for (let y = 0; y < board.length; y += 1) {
    for (let x = 0; x < board[y].length; x += 1) {
      const pos = `${x},${y}`;
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'intersection-point';
      btn.style.left = `${(x / 8) * 100}%`;
      btn.style.top = `${(y / 9) * 100}%`;
      btn.dataset.pos = pos;
      btn.addEventListener('click', () => onCellClick({ x, y, pos, piece: board[y][x] }));

      if (selectedFrom === pos) {
        btn.classList.add('selected');
      }
      if (legalTargets.has(pos)) {
        btn.classList.add('highlight');
      }

      if (board[y][x]) {
        const piece = document.createElement('span');
        piece.textContent = formatPiece(board[y][x]);
        piece.className = 'piece-token';
        if (board[y][x].startsWith('r')) {
          piece.classList.add('red-piece');
        } else if (board[y][x].startsWith('b')) {
          piece.classList.add('black-piece');
        }
        btn.appendChild(piece);
      }
      intersections.push(btn);
    }
  }

  const topGrid = document.createElement('div');
  topGrid.className = 'xiangqi-grid xiangqi-grid-top';
  const bottomGrid = document.createElement('div');
  bottomGrid.className = 'xiangqi-grid xiangqi-grid-bottom';

  boardSurface.append(topGrid, bottomGrid, palaceTop, palaceBottom);
  intersections.forEach((btn) => boardSurface.appendChild(btn));
  boardFrame.appendChild(boardSurface);
  container.appendChild(boardFrame);
}

function renderDarkChessBoard({ board, selectedFrom, legalTargets, onCellClick, container, revealed }) {
  container.innerHTML = '';
  const table = document.createElement('table');
  table.className = 'dark-board';

  board.forEach((row, y) => {
    const tr = document.createElement('tr');
    row.forEach((cell, x) => {
      const td = document.createElement('td');
      const pos = `${x},${y}`;
      const isRevealed = revealed[y]?.[x] === true;
      td.className = isRevealed ? 'revealed-cell' : 'hidden-cell';

      if (selectedFrom === pos) {
        td.classList.add('selected');
      }
      if (legalTargets.has(pos)) {
        td.classList.add('highlight');
      }

      if (isRevealed && cell) {
        const span = document.createElement('span');
        span.textContent = formatPiece(cell);
        span.className = 'dark-piece-token';
        
        // 根据棋子颜色添加类
        if (cell.startsWith('r')) {
          span.classList.add('red-piece');
        } else if (cell.startsWith('b')) {
          span.classList.add('black-piece');
        }
        
        td.appendChild(span);
      } else if (!isRevealed) {
        const hiddenToken = document.createElement('span');
        hiddenToken.className = 'hidden-piece-token';
        td.appendChild(hiddenToken);
      }
      td.addEventListener('click', () => onCellClick({ x, y, pos, piece: cell }));

      tr.appendChild(td);
    });
    table.appendChild(tr);
  });

  container.appendChild(table);
}

function formatPiece(piece) {
  if (!piece) {
    return '';
  }

  const labels = {
    r: { R: '\u4fe5', N: '\u508c', E: '\u76f8', A: '\u4ed5', K: '\u5e25', C: '\u7832', P: '\u5175' },
    b: { R: '\u8eca', N: '\u99ac', E: '\u8c61', A: '\u58eb', K: '\u5c07', C: '\u5305', P: '\u5352' }
  };

  return labels[piece[0]]?.[piece[1]] || piece[1];
}
