import {requestJson} from "../common/http.js";

export function fetchOrders(name) {
    return requestJson(
        `/api/payments/orders?name=${encodeURIComponent(name)}`
    );
}
