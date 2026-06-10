(function () {
    "use strict";

    const THEME_LIST_LIMIT = 500;

    // --- 요소 셀렉터 ---
    const themeCreateForm = document.getElementById("theme-create-form");
    const themeName = document.getElementById("theme-name");
    const themeDesc = document.getElementById("theme-desc");
    const themeImage = document.getElementById("theme-image");
    const themeCreateMsg = document.getElementById("theme-create-msg");
    const themeDeleteSelect = document.getElementById("theme-delete-select");
    const themeDeleteBtn = document.getElementById("theme-delete-btn");
    const themeDeleteMsg = document.getElementById("theme-delete-msg");

    const timeCreateForm = document.getElementById("time-create-form");
    const timeStart = document.getElementById("time-start");
    const timeCreateMsg = document.getElementById("time-create-msg");
    const timeDeleteSelect = document.getElementById("time-delete-select");
    const timeDeleteBtn = document.getElementById("time-delete-btn");
    const timeDeleteMsg = document.getElementById("time-delete-msg");

    const reservationsBody = document.getElementById("reservations-body");
    const refreshReservations = document.getElementById("refresh-reservations");
    const reservationsMsg = document.getElementById("reservations-msg");

    // --- 유틸리티 함수 ---
    function setMsg(el, text, ok) {
        if (!el) return;
        el.textContent = text;
        el.classList.remove("message--ok", "message--err");
        if (text) el.classList.add(ok ? "message--ok" : "message--err");
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        if (!res.ok) {
            const t = await res.text();
            throw new Error(t || res.statusText);
        }
        if (res.status === 204) return null;
        const ct = res.headers.get("content-type") || "";
        if (ct.includes("application/json")) return res.json();
        return null;
    }

    function formatTime(t) {
        if (!t) return "—";
        const parts = String(t).split(":");
        return `${parts[0]}:${parts[1] || "00"}`;
    }

    // --- 테마 관리 로직 (서버 데이터만 사용)[cite: 14] ---
    async function loadThemesIntoDeleteSelect() {
        themeDeleteSelect.innerHTML = "";
        try {
            const apiThemes = await fetchJson(`/themes`);
            const themes = apiThemes || [];
            const sorted = [...themes].sort((a, b) => a.name.localeCompare(b.name, "ko"));

            const empty = document.createElement("option");
            empty.value = "";
            empty.textContent = "테마를 선택하세요";
            themeDeleteSelect.appendChild(empty);

            sorted.forEach((t) => {
                const opt = document.createElement("option");
                opt.value = String(t.id);
                opt.textContent = t.name;
                themeDeleteSelect.appendChild(opt);
            });
        } catch (e) {
            setMsg(themeDeleteMsg, "테마 목록을 불러오지 못했습니다.", false);
        }
    }

    themeCreateForm.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        setMsg(themeCreateMsg, "", true);
        const file = themeImage.files && themeImage.files[0];
        if (!file) {
            setMsg(themeCreateMsg, "이미지를 선택해 주세요.", false);
            return;
        }
        try {
            const formData = new FormData();
            formData.append("name", themeName.value.trim());
            formData.append("description", themeDesc.value.trim());
            formData.append("file", file);

            const res = await fetch("/admin/themes", {method: "POST", body: formData});
            if (!res.ok) throw new Error(await res.text() || "등록 실패");

            setMsg(themeCreateMsg, "테마가 등록되었습니다.", true);
            themeCreateForm.reset();
            await loadThemesIntoDeleteSelect(); // 등록 후 목록 갱신
        } catch (e) {
            setMsg(themeCreateMsg, e.message, false);
        }
    });

    themeDeleteBtn.addEventListener("click", async () => {
        const id = themeDeleteSelect.value;
        if (!id || !confirm("삭제하시겠습니까?")) return;
        try {
            await fetchJson(`/admin/themes/${id}`, {method: "DELETE"});
            setMsg(themeDeleteMsg, "삭제되었습니다.", true);
            await loadThemesIntoDeleteSelect(); // 삭제 후 목록 갱신
        } catch (e) {
            setMsg(themeDeleteMsg, "삭제 실패", false);
        }
    });

    // --- 시간 및 예약 관리 로직 (기존 유지)[cite: 14] ---
    async function loadTimesIntoDeleteSelect() {
        if (!timeDeleteSelect) return;
        timeDeleteSelect.innerHTML = "";
        try {
            const times = await fetchJson("/times");
            const sorted = [...times].sort((a, b) => a.startAt.localeCompare(b.startAt));

            const empty = document.createElement("option");
            empty.value = "";
            empty.textContent = "시간을 선택하세요";
            timeDeleteSelect.appendChild(empty);

            sorted.forEach((t) => {
                const opt = document.createElement("option");
                opt.value = String(t.id);
                opt.textContent = formatTime(t.startAt);
                timeDeleteSelect.appendChild(opt);
            });
        } catch (e) {
            setMsg(timeDeleteMsg, "시간 목록 로드 실패", false);
        }
    }

    timeCreateForm.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        setMsg(timeCreateMsg, "", true);
        let startAt = timeStart.value;
        if (startAt.split(":").length === 2) startAt += ":00";
        try {
            await fetchJson("/admin/times", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({startAt}),
            });
            setMsg(timeCreateMsg, "시간이 등록되었습니다.", true);
            timeCreateForm.reset();
            await loadTimesIntoDeleteSelect();
        } catch (e) {
            setMsg(timeCreateMsg, e.message, false);
        }
    });

    timeDeleteBtn.addEventListener("click", async () => {
        const id = timeDeleteSelect.value;
        if (!id || !confirm("삭제하시겠습니까?")) return;
        try {
            await fetchJson(`/admin/times/${id}`, {method: "DELETE"});
            setMsg(timeDeleteMsg, "삭제되었습니다.", true);
            await loadTimesIntoDeleteSelect();
        } catch (e) {
            // 서버가 JSON 에러 응답을 반환하는 경우 메시지 파싱
            try {
                const parsed = JSON.parse(e.message);
                setMsg(timeDeleteMsg, parsed.message || "삭제 실패", false);
            } catch {
                setMsg(timeDeleteMsg, e.message || "삭제 실패", false);
            }
        }
    });

    async function loadReservations() {
        setMsg(reservationsMsg, "", true);
        reservationsBody.innerHTML = "";
        try {
            const list = await fetchJson("/reservations");
            if (!list || !list.length) {
                reservationsBody.innerHTML = '<tr><td colspan="6">예약이 없습니다.</td></tr>';
                return;
            }
            list.forEach((r) => {
                const tr = document.createElement("tr");
                const timeVal = r.timeResponse?.startAt;
                // order 0 = 예약 확정, 1 이상 = 대기 N번
                const statusText = (r.order ?? 0) > 0 ? `대기 ${r.order}번` : "예약 확정";
                const cells = [r.id, r.name, r.date, formatTime(timeVal), r.themeResponse?.name || "—", statusText];
                cells.forEach(text => {
                    const td = document.createElement("td");
                    td.textContent = text;
                    tr.appendChild(td);
                });
                reservationsBody.appendChild(tr);
            });
        } catch (e) {
            setMsg(reservationsMsg, "로드 실패", false);
        }
    }

    refreshReservations.addEventListener("click", loadReservations);

    loadThemesIntoDeleteSelect();
    loadTimesIntoDeleteSelect();
    loadReservations();
})();
