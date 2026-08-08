export function createDarkBoard() {
  return Array.from({ length: 8 }, () => Array(4).fill(null));
}
