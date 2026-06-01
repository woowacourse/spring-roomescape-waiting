async function parseError(response) {
    const text = await response.text();

    if (text) {
        throw new Error(text);
    }

    throw new Error("요청 처리에 실패했습니다.");
}

export async function requestJson(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        await parseError(response);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}
