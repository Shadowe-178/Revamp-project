import './config.js';
import './router.js';
import { initGame } from './core/gameManager.js';

window.addEventListener('DOMContentLoaded', () => {
  initGame();
});
