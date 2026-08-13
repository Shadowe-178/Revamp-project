export const config = {
  // 目前遊戲模式
  gameMode: 'xiangqi',

  // AI 難度
  aiLevel: 'beginner',

  // 是否為 GitHub Pages
  isGitHubPages:
    window.location.hostname === 'shadowe-178.github.io',

  // GitHub Pages 專案名稱
  githubRepo: 'Revamp-project',

  // 各模式設定
  modes: {
    xiangqi: {
      label: '大盤（中國象棋）',
      boardKey: 'xiangqi',
      rows: 10,
      cols: 9,
      apiBase: '/api/chess'
    },

    pvp: {
      label: '雙人對弈',
      boardKey: 'xiangqi_pvp',
      rows: 10,
      cols: 9,
      apiBase: '/api/chess'
    },

    ai: {
      label: 'AI 對戰',
      boardKey: 'xiangqi_ai',
      rows: 10,
      cols: 9,
      apiBase: '/api/chess'
    },

    darkChess: {
      label: '暗棋',
      boardKey: 'darkChess',
      rows: 8,
      cols: 4,
      apiBase: '/api/dark'
    },

    darkAi: {
      label: '暗棋 AI 對戰',
      boardKey: 'darkAi',
      rows: 8,
      cols: 4,
      apiBase: '/api/dark'
    }
  },

  // GitHub Pages Demo JSON 位置
  mockApi: {
    chessInit: './data/api/chess/init.json',
    darkInit: './data/api/dark/init.json'
  }
};