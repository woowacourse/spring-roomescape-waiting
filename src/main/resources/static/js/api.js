async function apiFetch(url, options = {}) {
  const res = await fetch(url, options);
  if (res.status === 204) return null;
  const text = await res.text();
  const body = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const message = body && body.message ? body.message : `요청에 실패했습니다 (${res.status}).`;
    throw new Error(message);
  }
  return body;
}

async function pollJobUntilDone(statusUrl) {
  for (let i = 0; i < 50; i++) {
    await new Promise(resolve => setTimeout(resolve, 100));
    const result = await apiFetch(statusUrl);
    if (result.status !== 'PENDING') return result;
  }
  throw new Error('처리 시간이 초과되었습니다.');
}

function showError(err) {
  alert(err && err.message ? err.message : '알 수 없는 오류가 발생했습니다.');
}
