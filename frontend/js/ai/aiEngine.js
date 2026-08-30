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

  // mode 只供前端決定使用哪個 API，
  // 不送進後端 AiRequestDto。
  const requestBody = {
    board,
    color,
    level,
    revealed
  };

  console.log('===== AI REQUEST DEBUG =====');
  console.log('URL:', apiUrl);
  console.log('Mode:', mode);
  console.log('Color:', color);
  console.log('Level:', level);
  console.log('Board:', board);
  console.log('Revealed:', revealed);
  console.log('Request Body:', requestBody);

  if (!board) {
    console.error('AI ERROR: board is missing');
    return null;
  }

  if (!color) {
    console.error('AI ERROR: color is missing');
    return null;
  }

  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(requestBody)
    });

    const contentType =
      response.headers.get('content-type') || '';

    const rawText = await response.text();

    console.log('===== AI API DEBUG =====');
    console.log('URL:', apiUrl);
    console.log('Status:', response.status);
    console.log('Status Text:', response.statusText);
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
        'AI API response is not JSON:',
        contentType
      );

      return null;
    }

    let state;

    try {
      state = JSON.parse(rawText);
    } catch (error) {
      console.error(
        'AI JSON parse error:',
        error
      );

      console.error(
        'Raw Response:',
        rawText
      );

      return null;
    }

    console.log('===== AI API PARSED RESPONSE =====');
    console.log('AI Response:', state);
    console.log('AI From:', state?.from);
    console.log('AI To:', state?.to);
    console.log('AI Piece:', state?.piece);
    console.log('AI Captured:', state?.capturedPiece);
    console.log('AI Score:', state?.score);

    if (!state) {
      console.error('AI API returned empty state');
      return null;
    }

    return state;

  } catch (error) {
    console.error('===== AI NETWORK ERROR =====');
    console.error('AI API request error:', error);
    console.error('API URL:', apiUrl);

    return null;
  }
}