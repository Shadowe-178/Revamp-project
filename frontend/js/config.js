const IS_GITHUB_PAGES =
  window.location.hostname === 'shadowe-178.github.io';

const API_BASE =
  IS_GITHUB_PAGES
    ? 'https://smartchess-5oeu.onrender.com'
    : '';

export const config = {
  gameMode: 'xiangqi',

  aiLevel: 'beginner',

  isGitHubPages: IS_GITHUB_PAGES,

  githubRepo: 'Revamp-project',

  modes: {
    xiangqi: {
      label: '中國象棋',
      boardKey: 'xiangqi',
      rows: 10,
      cols: 9,
      apiBase: `${API_BASE}/api/chess`
    },

    pvp: {
      label: '雙人對弈',
      boardKey: 'xiangqi_pvp',
      rows: 10,
      cols: 9,
      apiBase: `${API_BASE}/api/chess`
    },

    ai: {
      label: 'AI 對戰',
      boardKey: 'xiangqi_ai',
      rows: 10,
      cols: 9,
      apiBase: `${API_BASE}/api/chess`
    },

    darkChess: {
      label: '暗棋',
      boardKey: 'darkChess',
      rows: 8,
      cols: 4,
      apiBase: `${API_BASE}/api/dark`
    },

    darkAi: {
      label: '暗棋 AI 對戰',
      boardKey: 'darkAi',
      rows: 8,
      cols: 4,
      apiBase: `${API_BASE}/api/dark`
    }
  },

  mockApi: {
    chessInit: './data/api/chess/init.json',
    darkInit: './data/api/dark/init.json'
  }
};