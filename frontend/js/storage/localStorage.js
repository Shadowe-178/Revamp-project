export function saveLocal(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

export function loadLocal(key) {
  const raw = localStorage.getItem(key);
  return raw ? JSON.parse(raw) : null;
}
