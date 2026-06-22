package roomescape.payment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * POST /reservations(주문 생성) → POST /payments/confirm(승인) → GET /payments/history(내역)
 * 전 구간을 실제 HTTP 로 검증한다. 토스는 MockWebServer 로 대체한다.
 * confirm 결과(성공/거절/read timeout)가 내역의 상태(확정/실패/확인 필요)로 영속화되는지 본다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentFlowIntegrationTest {

    @LocalServerPort
    private int port;

    static MockWebServer mockWebServer;

    static {
        mockWebServer = new MockWebServer();
        try {
            mockWebServer.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        // 별도 컨텍스트가 공유 DB(jdbc:h2:mem:database)와 충돌하지 않도록 고유 인메모리 DB 사용
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:payment-flow-test");
        registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        // RestAssured.port 는 전역 상태라, 8080(기본)을 쓰는 다른 테스트로 새지 않도록 복원한다.
        RestAssured.port = RestAssured.DEFAULT_PORT;
    }

    @Test
    void 정상_승인되면_내역에_확정으로_paymentKey와_함께_표시된다() {
        String orderId = "order-confirm";
        createReservation("확정유저", orderId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"paymentKey": "pk_real_1", "orderId": "%s", "status": "DONE", "totalAmount": 10000}
                        """.formatted(orderId)));

        confirm("pk_real_1", orderId, 10000L)
                .then().log().all()
                .statusCode(200)
                .body("status", equalTo("DONE"));

        RestAssured.given().log().all()
                .when().get("/payments/history?name=확정유저")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", equalTo("CONFIRMED"))
                .body("[0].statusLabel", equalTo("확정"))
                .body("[0].orderId", equalTo(orderId))
                .body("[0].paymentKey", equalTo("pk_real_1"))
                .body("[0].approvedAmount", is(10000));
    }

    @Test
    void 토스가_거절하면_내역에_실패로_표시된다() {
        String orderId = "order-reject";
        createReservation("거절유저", orderId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"code": "REJECT_CARD_COMPANY", "message": "카드사에서 결제를 거절했습니다."}
                        """));

        confirm("pk_x", orderId, 10000L)
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("TOSS_PAYMENT_ERROR"));

        RestAssured.given().log().all()
                .when().get("/payments/history?name=거절유저")
                .then().log().all()
                .statusCode(200)
                .body("[0].status", equalTo("FAILED"))
                .body("[0].statusLabel", equalTo("실패"))
                .body("[0].paymentKey", is(nullValue()));
    }

    @Test
    void read_timeout이면_실패가_아니라_확인필요로_표시되고_예약은_유지된다() {
        String orderId = "order-unknown";
        int reservationId = createReservation("확인유저", orderId);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"paymentKey": "pk_slow", "orderId": "%s", "status": "DONE", "totalAmount": 10000}
                        """.formatted(orderId))
                .setHeadersDelay(2, TimeUnit.SECONDS));

        confirm("pk_slow", orderId, 10000L)
                .then().log().all()
                .statusCode(504)
                .body("code", equalTo("PAYMENT_RESULT_UNKNOWN"));

        // 예약이 지워지지 않고 남아 있어야 한다(read timeout 은 "실패"가 아니다).
        RestAssured.given().log().all()
                .when().get("/reservations/" + reservationId)
                .then().statusCode(200);

        RestAssured.given().log().all()
                .when().get("/payments/history?name=확인유저")
                .then().log().all()
                .statusCode(200)
                .body("[0].status", equalTo("UNKNOWN"))
                .body("[0].statusLabel", equalTo("확인 필요"));
    }

    private int createReservation(String name, String orderId) {
        Map<String, Object> params = Map.of(
                "name", name,
                "themeId", 1L,
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 1L,
                "orderId", orderId,
                "amount", 10000L
        );
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().path("reservationId");
    }

    private io.restassured.response.Response confirm(String paymentKey, String orderId, Long amount) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                .when().post("/payments/confirm");
    }
}
