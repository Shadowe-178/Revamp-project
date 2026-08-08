export async function getAiMove({ board, color, level, mode, revealed }) {
  const isDark = mode === 'darkChess' || mode === 'darkAi';
  const apiBase = isDark ? '/api/dark' : '/api/chess';
  const response = await fetch(`${apiBase}/ai`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ board, color, level, revealed })
  });

  if (!response.ok) {
    return null;
  }

  return response.json();
}
