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
        assertThat(page).contains("resumePendingPayment");
        assertThat(page).contains("/payment?name=");
    }
}
