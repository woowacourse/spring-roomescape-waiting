const Roomescape = (() => {
    const TOKEN_KEY = 'roomescape.auth.token';
    const LEGACY_MEMBER_TOKEN_KEY = 'roomescape.member.token';
    const LEGACY_MANAGER_TOKEN_KEY = 'roomescape.manager.token';

    function byId(id) {
        return document.getElementById(id);
    }

    function clear(node) {
        node.replaceChildren();
    }

    function append(parent, ...children) {
        children.flat().filter(child => child !== null && child !== undefined).forEach(child => {
            parent.appendChild(typeof child === 'string' || typeof child === 'number'
                ? document.createTextNode(String(child))
                : child);
        });
        return parent;
    }

    function h(tag, options = {}, children = []) {
        const node = document.createElement(tag);
        if (options.className) node.className = options.className;
        if (options.text !== undefined) node.textContent = String(options.text);
        if (options.title !== undefined) node.title = String(options.title);
        if (options.type) node.type = options.type;
        if (options.value !== undefined) node.value = String(options.value);
        if (options.href) node.href = options.href;
        if (options.id) node.id = options.id;
        if (options.colSpan) node.colSpan = options.colSpan;
        if (options.disabled) node.disabled = true;
        if (options.style) Object.assign(node.style, options.style);
        if (options.attrs) {
            Object.entries(options.attrs).forEach(([key, value]) => {
                if (value !== null && value !== undefined) node.setAttribute(key, String(value));
            });
        }
        if (options.on) {
            Object.entries(options.on).forEach(([event, handler]) => node.addEventListener(event, handler));
        }
        append(node, children);
        return node;
    }

    function button(className, text, onClick, options = {}) {
        return h('button', {className, type: 'button', text, on: {click: onClick}, ...options});
    }

    function badge(text, color = 'gray') {
        return h('span', {className: `badge badge-${color}`, text});
    }

    function loadingState() {
        return h('div', {className: 'empty-state'}, [h('div', {className: 'spinner'})]);
    }

    function emptyState(message, icon) {
        return h('div', {className: 'empty-state'}, [
            icon ? h('div', {className: 'empty-icon', text: icon}) : null,
            h('p', {text: message}),
        ]);
    }

    function errorState(message) {
        return h('div', {className: 'empty-state'}, [
            h('p', {text: message, style: {color: 'var(--danger)'}}),
        ]);
    }

    function toast(message, type = 'info') {
        const container = byId('toast-container');
        const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
        const el = h('div', {className: `toast toast-${type}`}, [
            h('span', {text: icon}),
            h('span', {text: message}),
        ]);
        container.appendChild(el);
        setTimeout(() => el.remove(), 3200);
    }

    async function api(method, url, options = {}) {
        const headers = {};
        if (options.body !== undefined) headers['Content-Type'] = 'application/json';
        if (options.token) headers.Authorization = `Bearer ${options.token}`;

        const res = await fetch(url, {
            method,
            headers,
            body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
        });
        if (!res.ok) {
            const text = await res.text().catch(() => '');
            let message = `오류가 발생했습니다. (HTTP ${res.status})`;
            try {
                const json = JSON.parse(text);
                if (json.message) message = json.message;
            } catch (_) {
                if (text) message = text;
            }
            const error = new Error(message);
            error.status = res.status;
            throw error;
        }
        const ct = res.headers.get('Content-Type') || '';
        return ct.includes('json') ? res.json() : null;
    }

    function decodeToken(token) {
        if (!token) return null;
        try {
            const rawPayload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            const payload = rawPayload.padEnd(rawPayload.length + (4 - rawPayload.length % 4) % 4, '=');
            return JSON.parse(decodeURIComponent(escape(atob(payload))));
        } catch (_) {
            return null;
        }
    }

    function setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.removeItem(LEGACY_MEMBER_TOKEN_KEY);
        localStorage.removeItem(LEGACY_MANAGER_TOKEN_KEY);
    }

    function getToken() {
        const token = localStorage.getItem(TOKEN_KEY)
            || localStorage.getItem(LEGACY_MANAGER_TOKEN_KEY)
            || localStorage.getItem(LEGACY_MEMBER_TOKEN_KEY)
            || '';
        if (token && !localStorage.getItem(TOKEN_KEY)) {
            setToken(token);
        }
        return token;
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(LEGACY_MEMBER_TOKEN_KEY);
        localStorage.removeItem(LEGACY_MANAGER_TOKEN_KEY);
    }

    async function login(username, password) {
        const data = await api('POST', '/login', {body: {username, password}});
        return data.accessToken;
    }

    function setOptions(select, items, placeholder, labelOf = item => item.name, valueOf = item => item.id) {
        clear(select);
        select.appendChild(new Option(placeholder, ''));
        items.forEach(item => select.appendChild(new Option(labelOf(item), String(valueOf(item)))));
    }

    function imageOrFallback(src, alt, options = {}) {
        const fallback = h('div', {
            className: options.fallbackClassName || '',
            text: options.fallbackText || '🎭',
            style: {
                display: src ? 'none' : 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: 'var(--bg-input)',
                ...options.fallbackStyle,
            },
        });
        if (!src) return fallback;
        const img = h('img', {
            attrs: {alt: alt || ''},
            style: options.imgStyle,
            on: {
                error: () => {
                    img.style.display = 'none';
                    fallback.style.display = 'flex';
                },
            },
        });
        img.src = src;
        return [img, fallback];
    }

    function showAdminLogin() {
        byId('admin-auth-panel').style.display = '';
        byId('admin-content').style.display = 'none';
    }

    function showAdminContent() {
        byId('admin-auth-panel').style.display = 'none';
        byId('admin-content').style.display = '';
    }

    function setAdminMessage(message) {
        const el = byId('admin-auth-message');
        if (el) el.textContent = message || '';
    }

    async function verifyManagerToken(token) {
        await api('GET', '/admin/reservations?page=0&size=1', {token});
    }

    function setupAdminAuth(onReady) {
        const form = byId('admin-login-form');
        const logout = byId('admin-logout-btn');

        form.addEventListener('submit', async event => {
            event.preventDefault();
            setAdminMessage('');
            const username = byId('admin-username').value.trim();
            const password = byId('admin-password').value;
            if (!username || !password) {
                setAdminMessage('아이디와 비밀번호를 입력해주세요.');
                return;
            }
            try {
                const token = await login(username, password);
                await verifyManagerToken(token);
                setToken(token);
                byId('admin-password').value = '';
                showAdminContent();
                await onReady();
            } catch (e) {
                clearToken();
                showAdminLogin();
                setAdminMessage(e.status === 403
                    ? '접근 권한이 없습니다. 관리자 계정으로 로그인해주세요.'
                    : e.message);
            }
        });

        logout.addEventListener('click', () => {
            clearToken();
            showAdminLogin();
            setAdminMessage('로그아웃되었습니다.');
        });

        const token = getToken();
        if (!token) {
            showAdminLogin();
            return;
        }

        verifyManagerToken(token)
            .then(async () => {
                showAdminContent();
                await onReady();
            })
            .catch(e => {
                clearToken();
                showAdminLogin();
                setAdminMessage(e.status === 403
                    ? '관리자 권한이 필요합니다. 다시 로그인해주세요.'
                    : '관리자 로그인이 필요합니다.');
            });
    }

    function managerToken() {
        return getToken();
    }

    function authToken() {
        return getToken();
    }

    return {
        TOKEN_KEY,
        byId,
        clear,
        append,
        h,
        button,
        badge,
        loadingState,
        emptyState,
        errorState,
        toast,
        api,
        login,
        decodeToken,
        setOptions,
        imageOrFallback,
        setupAdminAuth,
        memberToken: authToken,
        managerToken,
        auth: {
            setToken,
            getToken,
            clearToken,
            setMemberToken: setToken,
            getMemberToken: authToken,
            clearMemberToken: clearToken,
            setManagerToken: setToken,
            getManagerToken: managerToken,
            clearManagerToken: clearToken,
        },
    };
})();
