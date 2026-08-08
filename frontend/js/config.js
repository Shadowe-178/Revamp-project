export const config = {
  gameMode: 'xiangqi',
  aiLevel: 'beginner',
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
  }
};
