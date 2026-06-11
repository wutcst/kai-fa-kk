import httpClient from './httpClient';

const DEFAULT_API_ERROR_MESSAGE = '请求失败，请稍后重试';

/**
 * Extracts a user-readable message from an API or network error.
 *
 * @param {unknown} error API or network error.
 * @returns {string} User-readable error message.
 */
export function getApiErrorMessage(error) {
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

  return DEFAULT_API_ERROR_MESSAGE;
}

function unwrapAuthResponse(response) {
  const data = response?.data;

  if (data?.success === false) {
    throw new Error(data.message || DEFAULT_API_ERROR_MESSAGE);
  }

  return data;
}

async function requestAuth(path, username, password) {
  try {
    const response = await httpClient.post(path, null, {
      params: { username, password },
    });

    return unwrapAuthResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

/**
 * Logs in an existing account.
 *
 * @param {string} username Account username.
 * @param {string} password Account password.
 * @returns {Promise<object>} Authentication result containing the login token.
 */
export function login(username, password) {
  return requestAuth('/auth/login', username, password);
}

/**
 * Registers a new account.
 *
 * @param {string} username Account username.
 * @param {string} password Account password.
 * @returns {Promise<object>} Registration result.
 */
export function register(username, password) {
  return requestAuth('/auth/register', username, password);
}
