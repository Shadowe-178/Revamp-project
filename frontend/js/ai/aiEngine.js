import { config } from '../config.js';

export async function getAiMove({
  board,
  color,
  level,
  mode,
  revealed
}) {
  const activeMode =
    config.modes[mode] || config.modes.xiangqi;

  const response = await fetch(`${activeMode.apiBase}/ai`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    body: JSON.stringify({
      board,
      color,
      level,
      mode,
      revealed
    })
  });

  if (!response.ok) {
    console.error(
      'AI API 失敗:',
      response.status,
      response.statusText
    );

    return null;
  }

  const state = await response.json();

  console.log('★★★★★ AI API Response ★★★★★', state);

  return state;
}