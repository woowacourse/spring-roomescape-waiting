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
    void userPageRendersPendingPaymentStatusSeparately() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("item.status === '결제대기'");
        assertThat(page).contains("결제대기");
        assertThat(page).contains("payment.status");
        assertThat(page).contains("payment.orderId");
        assertThat(page).contains("payment.paymentKey");
        assertThat(page).contains("payment.amount");
        assertThat(page).contains("resumePendingPayment");
        assertThat(page).contains("/payment?name=");
    }

    @Test
    void userPageNotifiesServerAndResetsCheckoutWhenPaymentWidgetFails() throws IOException {
        String page = Files.readString(USER_PAGE);

        assertThat(page).contains("notifyPaymentFailure");
        assertThat(page).contains("/payments/fail?");
        assertThat(page).contains("resetCheckout();");
        assertThat(page).contains("await loadTimes();");
        assertThat(page).contains("결제가 취소되었습니다. 다시 시간을 선택해주세요.");
    }
}
