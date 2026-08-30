const API_BASE =
  window.location.hostname === 'shadowe-178.github.io'
    ? 'https://smartchess-5oeu.onrender.com'
    : '';

export async function login(username, password) {
  const body = new URLSearchParams();

  body.set('username', username);
  body.set('password', password);

  const response = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    credentials: 'include',
    body: body.toString()
  });

  return response;
}