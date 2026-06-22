package roomescape.support;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

public final class PaymentApiSteps {

    private PaymentApiSteps() {
    }

    public static ValidatableResponse 결제_승인_콜백_요청(String paymentKey, String orderId, long amount) {
        return RestAssured.given().redirects().follow(false)
                .when().get("/payments/success?paymentKey=" + paymentKey
                        + "&orderId=" + orderId + "&amount=" + amount)
                .then();
    }
}
