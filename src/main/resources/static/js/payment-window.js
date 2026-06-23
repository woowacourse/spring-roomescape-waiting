window.openPaymentWindow = async function (payment, name) {
    const tossPayments = TossPayments(window.paymentClientKey);
    const widgets = tossPayments.widgets({customerKey: TossPayments.ANONYMOUS});
    await widgets.setAmount({currency: "KRW", value: payment.amount});

    const paymentWindow = await widgets.renderPaymentWindow({
        variantKey: {
            paymentMethod: "DEFAULT",
            agreement: "AGREEMENT"
        }
    });

    paymentWindow.on("paymentRequest", async () => {
        try {
            const successUrl = new URL("/payments/success", window.location.origin);
            successUrl.searchParams.set("name", name);
            const failUrl = new URL("/payments/fail", window.location.origin);
            failUrl.searchParams.set("paymentId", payment.paymentId);
            failUrl.searchParams.set("name", name);

            await widgets.requestPayment({
                orderId: payment.orderId,
                orderName: "방탈출 예약",
                successUrl: successUrl.toString(),
                failUrl: failUrl.toString()
            });
        } catch (error) {
            if (error.code !== "USER_CANCEL") {
                window.showPaymentError?.(error.message || "결제 요청에 실패했습니다.");
            }
        }
    });
};
