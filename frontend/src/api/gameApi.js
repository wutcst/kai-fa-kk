import httpClient from './httpClient';
import { adaptGameState } from './gameStateAdapter';

const DEFAULT_GAME_API_ERROR_MESSAGE = '游戏请求失败，请稍后重试';

/**
 * Normalizes direct GameState responses and unified API response wrappers.
 *
 * @param {unknown} responseData Response body returned by the backend.
 * @returns {unknown} Normalized game response data.
 */
export function normalizeGameResponse(responseData) {
  if (responseData?.success === false) {
    throw new Error(responseData.message || DEFAULT_GAME_API_ERROR_MESSAGE);
  }

  if (responseData?.data !== undefined && responseData?.data !== null) {
    return responseData.data;
  }

  return responseData;
}

/**
 * Extracts a user-readable message from a game API or network error.
 *
 * @param {unknown} error API or network error.
 * @returns {string} User-readable error message.
 */
export function getGameApiErrorMessage(error) {
  const responseMessage = error?.response?.data?.message;
  if (typeof responseMessage === 'string' && responseMessage) {
    return responseMessage;
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  if (typeof error === 'string' && error) {
    return error;
  }

  return DEFAULT_GAME_API_ERROR_MESSAGE;
}

async function requestGame(method, path, params, adaptState = false) {
  try {
    const response = await httpClient.request({
      method,
      url: path,
      data: method === 'post' ? null : undefined,
      params,
    });

    const responseData = normalizeGameResponse(response.data);
    return adaptState ? adaptGameState(responseData) : responseData;
  } catch (error) {
    throw new Error(getGameApiErrorMessage(error));
  }
}

/**
 * Starts a new game using an authenticated account token.
 *
 * @param {string} token Authentication token.
 * @returns {Promise<object>} Initial game state.
 */
export function startGame(token) {
  return requestGame('post', '/game/start', { token }, true);
}

/**
 * Gets the latest state for a game session.
 *
 * @param {string} sessionId Game session identifier.
 * @returns {Promise<object>} Latest game state.
 */
export function getGameState(sessionId) {
  return requestGame('get', '/game/state', { sessionId }, true);
}

/**
 * Moves the player in a direction.
 *
 * @param {string} sessionId Game session identifier.
 * @param {string} direction Direction accepted by the backend.
 * @returns {Promise<object>} Updated game state.
 */
export function move(sessionId, direction) {
  return requestGame('post', '/game/move', { sessionId, direction }, true);
}

/**
 * Takes an item from the current room.
 *
 * @param {string} sessionId Game session identifier.
 * @param {string} itemId Item identifier.
 * @returns {Promise<object>} Updated game state.
 */
export function takeItem(sessionId, itemId) {
  return requestGame('post', '/game/take', { sessionId, itemId });
}

/**
 * Drops an item from the player's inventory.
 *
 * @param {string} sessionId Game session identifier.
 * @param {string} itemId Item identifier.
 * @returns {Promise<object>} Updated game state.
 */
export function dropItem(sessionId, itemId) {
  return requestGame('post', '/game/drop', { sessionId, itemId });
}

/**
 * Uses an item from the player's inventory.
 *
 * @param {string} sessionId Game session identifier.
 * @param {string} itemId Item identifier.
 * @returns {Promise<object>} Updated game state.
 */
export function useItem(sessionId, itemId) {
  return requestGame('post', '/game/use', { sessionId, itemId });
}

/**
 * Submits a password for the current game session.
 *
 * @param {string} sessionId Game session identifier.
 * @param {string} password Password attempt.
 * @returns {Promise<object>} Updated game state.
 */
export function submitPassword(sessionId, password) {
  return requestGame('post', '/game/password', { sessionId, password });
}

/**
 * Saves the current game session.
 *
 * @param {string} token Authentication token.
 * @param {string} sessionId Game session identifier.
 * @param {string} saveName Save display name.
 * @returns {Promise<object>} Updated game state.
 */
export function saveGame(token, sessionId, saveName = '自动存档') {
  return requestGame('post', '/game/save', { token, sessionId, saveName }, true);
}

/**
 * Loads the most recently updated save for an authenticated account.
 *
 * @param {string} token Authentication token.
 * @returns {Promise<object>} Loaded game state.
 */
export function loadLatestSave(token) {
  return requestGame('post', '/game/load', { token }, true);
}

/**
 * Loads a specific save for an authenticated account.
 *
 * @param {string} token Authentication token.
 * @param {number|string} saveId Save identifier.
 * @returns {Promise<object>} Loaded game state.
 */
export function loadSave(token, saveId) {
  return requestGame('post', '/game/load', { token, saveId }, true);
}

/**
 * Lists save summaries for an authenticated account.
 *
 * @param {string} token Authentication token.
 * @returns {Promise<object[]>} Save summary list.
 */
export async function listGameSaves(token) {
  const saves = await requestGame('get', '/game/saves', { token });
  return Array.isArray(saves) ? saves : [];
}
