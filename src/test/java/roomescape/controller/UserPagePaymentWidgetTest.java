package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UserPagePaymentWidgetTest {

    private static final Path USER_PAGE = Path.of("src/main/resources/static/user/index.html");

    @Test
    void userPageUsesPaymentWidgetSdkForWidgetClientKey() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("https://js.tosspayments.com/v2/standard");
        assertThat(page).contains("widgets({customerKey: 'ANONYMOUS'})");
        assertThat(page).contains("renderPaymentMethods");
        assertThat(page).contains("renderAgreement");
        assertThat(page).contains("paymentWidgets.requestPayment");
        assertThat(page).doesNotContain("https://js.tosspayments.com/v1/payment");
        assertThat(page).doesNotContain("requestPayment('카드'");
    }

    @Test
    void userPageDoesNotTreatEveryUnavailableTimeAsWaitable() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("/admin/reservations");
        assertThat(page).contains("예약 불가");
        assertThat(page).doesNotContain("booked: !availableIds.has(t.id)");
    }

    @Test
    void userPageTreatsPendingPaymentTimeAsWaitable() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("if (reservation?.status === '결제대기') return 'waiting';");
        assertThat(page).doesNotContain("if (reservation?.status === '결제대기') return 'pending';");
    }

    @Test
    void userPageRendersPendingPaymentStatusSeparately() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("item.status === '결제대기'");
        assertThat(page).contains("결제대기");
        assertThat(page).contains("payment.status");
        assertThat(page).contains("payment.orderId");
        assertThat(page).contains("payment.paymentKey");
        assertThat(page).contains("payment.amount");
        assertThat(page).contains("resumePendingPayment");
        assertThat(page).contains("cancelPendingPayment");
        assertThat(page).contains("/payment?name=");
    }

    @Test
    void userPageCanCancelPendingPaymentFromMyReservations() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("onclick=\"cancelPendingPayment('${item.payment?.orderId ?? ''}')\"");
        assertThat(page).contains("async function cancelPendingPayment(orderId)");
        assertThat(page).contains("await cancelPendingPaymentOrder(orderId, 'USER_CANCEL', '사용자가 결제를 취소했습니다.');");
        assertThat(page).contains("결제대기 예약이 취소되었습니다.");
        assertThat(page).contains("await loadMyList();");
    }

    @Test
    void userPageCanReplaceExistingPendingPaymentBeforeNewReservation() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("async function handleExistingPendingPaymentConflict(name, date, reserveBtn)");
        assertThat(page).contains("이미 결제 대기 중인 예약이 있습니다");
        assertThat(page).contains("기존 결제대기를 취소하고 새 예약을 진행할까요?");
        assertThat(page).contains("async function findPendingPaymentItem(name)");
        assertThat(page).contains("pending.payment.orderId,");
        assertThat(page).contains("'REPLACE_PENDING_PAYMENT',");
        assertThat(page).contains("await startReservationCheckout(name, date, reserveBtn, true);");
    }

    @Test
    void userPageNotifiesServerAndResetsCheckoutWhenPaymentWidgetFails() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("notifyPaymentFailure");
        assertThat(page).contains("fetch(`/payments/cancel?${params.toString()}`, {method: 'POST'});");
        assertThat(page).contains("resetCheckout();");
        assertThat(page).contains("await loadTimes({preserveSelection: true});");
        assertThat(page).contains("reserveBtn.disabled = false;");
        assertThat(page).contains("waitingBtn.disabled = false;");
        assertThat(page).contains("결제가 취소되었습니다. 시간 상태를 다시 확인해주세요.");
        assertThat(page).doesNotContain("결제가 취소되었습니다. 같은 시간으로 다시 결제할 수 있습니다.");
    }

    @Test
    void userPageCancelsPaymentWhenReturningFromPaymentWindow() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("let paymentRequestInProgress = false;");
        assertThat(page).contains("let paymentWindowWasOpened = false;");
        assertThat(page).contains("function markPaymentWindowOpened()");
        assertThat(page).contains("async function cancelPaymentIfReturnedFromPaymentWindow()");
        assertThat(page).contains("PAYMENT_WINDOW_RETURNED");
        assertThat(page).contains("window.addEventListener('pagehide', markPaymentWindowOpened);");
        assertThat(page).contains("window.addEventListener('pageshow', cancelPaymentIfReturnedFromPaymentWindow);");
        assertThat(page).contains("document.addEventListener('visibilitychange'");
    }
}
