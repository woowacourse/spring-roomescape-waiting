import {api, ApiError} from "./api.js";
import {state} from "./state.js";

const PAYMENT_SUCCESS_ROUTE = "/#/payment/processing";
const PAYMENT_FAIL_ROUTE = "/#/payment/fail";
const RETRYABLE_PAYMENT_APPROVAL_STATUS = 503;
const DESKTOP_PAYMENT_QUERY = "(pointer: fine) and (min-width: 768px)";
const processedApprovalKeys = new Set();
const processedFailureOrderIds = new Set();
const processingFailureOrderIds = new Set();
let approvingKey = "";
let approvingPromise = null;

export async function getTossPayments() {
    try {
        const config = await loadPaymentConfig();

        if (typeof window.TossPayments !== "function") {
            return failPaymentSdk("결제 SDK를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }

        if (state.payment.widgets && state.payment.tossClientKey === config.clientKey) {
            return {ok: true, widgets: state.payment.widgets};
        }

        const tossPayments = window.TossPayments(config.clientKey);
        const customerKey = window.TossPayments.ANONYMOUS || "ANONYMOUS";
        const widgets = tossPayments.widgets({customerKey});

        state.payment.tossPayments = tossPayments;
        state.payment.widgets = widgets;
        state.payment.tossClientKey = config.clientKey;
        state.payment.sdkError = "";

        return {ok: true, widgets};
    } catch (error) {
        return failPaymentSdk(error.message || "결제 정보를 준비하지 못했습니다.");
    }
}

export function preloadTossPayments() {
    const preload = () => {
        void getTossPayments();
    };

    if (typeof window.requestIdleCallback === "function") {
        window.requestIdleCallback(preload, {timeout: 2000});
        return;
    }

    window.setTimeout(preload, 300);
}

export function rememberPendingPayment(reservation) {
    if (!isPaymentPendingReservation(reservation)) {
        return;
    }

    state.payment.pendingContext = {
        reservation,
        payment: reservation.payment
    };
    state.payment.result = {
        status: "ready",
        message: "예약이 접수되었습니다. 결제를 완료해 주세요."
    };
}

export function isPaymentPendingReservation(reservation) {
    return reservation?.status === "PAYMENT_PENDING" && Boolean(reservation.payment);
}

export async function requestTossPayment(reservation) {
    const request = buildPaymentRequest(reservation);
    const result = await getTossPayments();

    if (!result.ok) {
        return result;
    }

    if (!canRequestWidgetPayment(result.widgets)) {
        return failPaymentSdk("결제창을 열 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }

    await openPaymentWindow(result.widgets, request);

    return {ok: true, request};
}

export function buildPaymentRequest(reservation) {
    if (!isPaymentPendingReservation(reservation)) {
        throw new Error("결제 대기 중인 예약만 결제할 수 있습니다.");
    }

    const payment = reservation.payment;
    const amount = Number(payment.amount);

    if (!payment.orderId) {
        throw new Error("결제 주문번호를 찾을 수 없습니다.");
    }

    if (!Number.isFinite(amount) || amount <= 0) {
        throw new Error("결제 금액을 확인할 수 없습니다.");
    }

    const request = {
        amount: {
            currency: "KRW",
            value: amount
        },
        paymentRequest: {
            orderId: payment.orderId,
            orderName: buildOrderName(reservation),
            successUrl: `${location.origin}${PAYMENT_SUCCESS_ROUTE}`,
            failUrl: `${location.origin}${PAYMENT_FAIL_ROUTE}`
        }
    };

    if (isDesktopPaymentWindow()) {
        request.paymentRequest.windowTarget = "iframe";
    }

    return request;
}

export function parseTossRedirectParams(search, hash) {
    const params = readTossRedirectSearchParams(search, hash);

    const paymentKey = String(params.get("paymentKey") || "").trim();
    const orderId = String(params.get("orderId") || "").trim();
    const amount = Number(params.get("amount"));

    if (!paymentKey || !orderId || !Number.isFinite(amount) || amount <= 0) {
        return {
            ok: false,
            message: "결제 승인에 필요한 정보가 없습니다. 예약 조회 후 다시 결제해 주세요."
        };
    }

    return {
        ok: true,
        params: {
            paymentKey,
            orderId,
            amount
        }
    };
}

export function parseTossFailRedirectParams(search, hash) {
    const params = readTossRedirectSearchParams(search, hash);
    const code = String(params.get("code") || "").trim();
    const orderId = String(params.get("orderId") || "").trim();
    const message = String(params.get("message") || "").trim() || "결제가 완료되지 않았습니다.";

    return {
        code,
        orderId,
        message
    };
}

export async function cleanupTossFailRedirectPayment(params) {
    const orderId = String(params?.orderId || "").trim();

    if (!orderId) {
        return {ok: true, skipped: true, orderId: ""};
    }

    if (processedFailureOrderIds.has(orderId)) {
        return {ok: true, skipped: true, orderId};
    }

    if (processingFailureOrderIds.has(orderId)) {
        return {ok: true, skipped: true, orderId};
    }

    processingFailureOrderIds.add(orderId);

    try {
        await api("/payments/fail", {
            method: "POST",
            body: {
                code: String(params?.code || "").trim(),
                message: String(params?.message || "").trim(),
                orderId
            }
        });
        processedFailureOrderIds.add(orderId);
    } finally {
        processingFailureOrderIds.delete(orderId);
    }

    return {ok: true, skipped: false, orderId};
}

export async function approveTossRedirectPayment(params) {
    const approvalKey = buildApprovalKey(params);

    if (processedApprovalKeys.has(approvalKey)) {
        return {
            ok: true,
            skipped: true,
            result: state.payment.result?.data || null
        };
    }

    if (approvingKey === approvalKey && approvingPromise) {
        return approvingPromise;
    }

    approvingKey = approvalKey;
    state.payment.processing = {
        status: "approving",
        orderId: params.orderId,
        paymentKey: params.paymentKey,
        amount: params.amount,
        message: "결제 승인을 요청하고 있습니다."
    };
    state.payment.result = null;

    approvingPromise = api("/payments/success", {
        method: "POST",
        body: {
            paymentKey: params.paymentKey,
            orderId: params.orderId,
            amount: params.amount
        }
    })
        .then((result) => {
            processedApprovalKeys.add(approvalKey);
            state.payment.processing = {
                status: "succeeded",
                orderId: params.orderId,
                paymentKey: params.paymentKey,
                amount: params.amount,
                message: "결제가 승인되었습니다."
            };
            state.payment.result = {
                status: "success",
                message: "결제가 완료되었습니다.",
                data: result
            };

            return {ok: true, skipped: false, result};
        })
        .catch((error) => {
            const retryable = isRetryablePaymentApprovalError(error);
            const message = error.message || (retryable
                ? "결제 승인 요청이 지연되고 있습니다. 잠시 후 다시 시도해 주세요."
                : "결제 승인을 완료하지 못했습니다.");
            state.payment.processing = {
                status: retryable ? "retryable-failed" : "failed",
                orderId: params.orderId,
                paymentKey: params.paymentKey,
                amount: params.amount,
                message
            };
            state.payment.result = {
                status: "failed",
                message,
                retryable,
                params: retryable ? {...params} : null
            };
            throw error;
        })
        .finally(() => {
            if (approvingKey === approvalKey) {
                approvingKey = "";
                approvingPromise = null;
            }
        });

    return approvingPromise;
}

function isRetryablePaymentApprovalError(error) {
    return error instanceof ApiError && error.status === RETRYABLE_PAYMENT_APPROVAL_STATUS;
}

export function formatAmount(amount) {
    const value = Number(amount);

    if (!Number.isFinite(value)) {
        return "-";
    }

    return `${value.toLocaleString("ko-KR")}원`;
}

function normalizeQuery(search) {
    const value = String(search || "");

    return value.startsWith("?") ? value.slice(1) : value;
}

function readTossRedirectSearchParams(search, hash) {
    const params = new URLSearchParams(normalizeQuery(search));
    const hashQuery = extractHashQuery(hash);

    if (hashQuery) {
        const hashParams = new URLSearchParams(hashQuery);
        hashParams.forEach((value, key) => {
            params.set(key, value);
        });
    }

    return params;
}

function extractHashQuery(hash) {
    const [, query = ""] = String(hash || "").split("?");

    return query;
}

function buildApprovalKey(params) {
    return `${params.paymentKey}:${params.orderId}:${params.amount}`;
}

function buildOrderName(reservation) {
    const themeName = reservation.theme?.name || "방탈출 예약";
    const date = reservation.date || "";
    const startAt = reservation.time?.startAt || "";
    const schedule = [date, startAt].filter(Boolean).join(" ");

    return schedule ? `${themeName} ${schedule}` : themeName;
}

function isDesktopPaymentWindow() {
    if (typeof window.matchMedia !== "function") {
        return true;
    }

    return window.matchMedia(DESKTOP_PAYMENT_QUERY).matches;
}

function canRequestWidgetPayment(widgets) {
    return typeof widgets?.setAmount === "function"
        && typeof widgets?.renderPaymentWindow === "function"
        && typeof widgets?.requestPayment === "function";
}

async function openPaymentWindow(widgets, request) {
    await closePaymentWindow();
    await widgets.setAmount(request.amount);

    const paymentWindow = await widgets.renderPaymentWindow();
    state.payment.paymentWindow = paymentWindow;
    paymentWindow.on("paymentRequest", async () => {
        await widgets.requestPayment(request.paymentRequest);
    });
}

async function closePaymentWindow() {
    const paymentWindow = state.payment.paymentWindow;

    if (!paymentWindow || typeof paymentWindow.destroy !== "function") {
        state.payment.paymentWindow = null;
        return;
    }

    await paymentWindow.destroy().catch(() => undefined);
    state.payment.paymentWindow = null;
}

async function loadPaymentConfig() {
    if (state.payment.config?.clientKey) {
        return state.payment.config;
    }

    if (state.payment.configPromise) {
        return state.payment.configPromise;
    }

    state.payment.configStatus = "loading";
    state.payment.configError = "";
    state.payment.configPromise = api("/payments/config")
        .then((config) => {
            if (!config?.clientKey) {
                throw new Error("결제 설정을 찾을 수 없습니다.");
            }

            state.payment.config = {clientKey: config.clientKey};
            state.payment.configStatus = "ready";
            return state.payment.config;
        })
        .catch((error) => {
            state.payment.config = null;
            state.payment.configStatus = "failed";
            state.payment.configError = error.message || "결제 설정을 불러오지 못했습니다.";
            throw error;
        })
        .finally(() => {
            state.payment.configPromise = null;
        });

    return state.payment.configPromise;
}

function failPaymentSdk(message) {
    state.payment.sdkError = message;

    return {
        ok: false,
        message
    };
}
