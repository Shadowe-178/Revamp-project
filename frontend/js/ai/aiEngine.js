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

  const apiUrl = `${activeMode.apiBase}/ai`;

  try {
    const response = await fetch(apiUrl, {
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

    const contentType =
      response.headers.get('content-type') || '';

    const rawText = await response.text();

    console.log('===== AI API DEBUG =====');
    console.log('URL:', apiUrl);
    console.log('Status:', response.status);
    console.log('Content-Type:', contentType);
    console.log('Response:', rawText);

    if (!response.ok) {
      console.error(
        'AI API failed:',
        response.status,
        response.statusText,
        rawText
      );

      return null;
    }

    if (!contentType.includes('application/json')) {
      console.error(
        'AI API did not return JSON:',
        contentType
      );

      return null;
    }

    const state = JSON.parse(rawText);

    console.log('AI API parsed response:', state);

    return state;

  } catch (error) {
    console.error('AI API request error:', error);
    return null;
  }
}