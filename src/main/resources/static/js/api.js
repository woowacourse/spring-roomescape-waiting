export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function api(path, options = {}) {
  const init = {
    method: options.method || "GET",
    headers: {
      Accept: "application/json"
    }
  };

  if (options.body !== undefined) {
    init.headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(options.body);
  }

  let response;
  try {
    response = await fetch(path, init);
  } catch (error) {
    throw new Error("서버에 연결할 수 없습니다.");
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  const data = readJson(text);

  if (!response.ok) {
    throw new ApiError(resolveError(data, text, response.status), response.status);
  }

  return data;
}

function readJson(text) {
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch (error) {
    return null;
  }
}

function resolveError(data, text, status) {
  if (data?.errorMessage) {
    return data.errorMessage;
  }

  if (data?.message) {
    return data.message;
  }

  if (text) {
    return text;
  }

  if (status === 404) {
    return "요청한 데이터를 찾을 수 없습니다.";
  }

  return "요청을 처리하지 못했습니다.";
}
