package roomescape.apitest.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.WaitingDetailResponse;
import roomescape.domain.order.PaymentStatus;
import roomescape.service.PaymentGateway;
import roomescape.service.dto.result.PaymentResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest {
    private final String userName = "브라운";
    private final Long timeId = 1L;
    private final Long themeId = 1L;
    private int initialTotalSize;
    private int initialConfirmedSize;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockBean
    private PaymentGateway paymentGateway;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        initialTotalSize = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        initialConfirmedSize = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE status = 'CONFIRMED'", Integer.class);
    }

    @Test
    @DisplayName("사용자는 예약을 등록, 조회, 삭제할 수 있다.")
    void manageReservation_Success() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        Long generatedId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        String dynamicOrderId = "order-" + generatedId;
        jdbcTemplate.update(
                "INSERT INTO orders (id, amount, reservation_id) VALUES (?, ?, ?)",
                dynamicOrderId, 50000, generatedId
        );

        BDDMockito.given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("mock_payment_key", dynamicOrderId, PaymentStatus.DONE, 50000L));

        RestAssured.given().log().all()
                .queryParam("paymentKey", "test_payment_key")
                .queryParam("orderId", dynamicOrderId)
                .queryParam("amount", 50000)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(200);

        List<Long> allIds = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("id", Long.class);

        assertThat(allIds)
                .hasSize(initialTotalSize + 1)
                .contains(generatedId);

        JsonPath jsonPath = RestAssured.given().log().all()
                .when().get("/reservations?userName=" + userName)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath();

        List<Long> idsByUserName = jsonPath.getList("reservationResponses.id", Long.class);
        List<String> names = jsonPath.getList("reservationResponses.name", String.class);

        assertThat(idsByUserName)
                .hasSize(initialConfirmedSize + 1)
                .contains(generatedId);

        assertThat(names).containsOnly(userName);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + generatedId + "?userName=" + userName)
                .then().log().all()
                .statusCode(204);

        List<Long> remainIds = RestAssured.given().log().all()
                .when().get("/reservations?userName=" + userName)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("reservationResponses.id", Long.class);

        assertThat(remainIds).hasSize(initialConfirmedSize);
        assertThat(remainIds).doesNotContain(generatedId);
    }

    @Test
    @DisplayName("사용자는 예약 시간을 변경할 수 있다.")
    void modifyReservationTime_Success() {
        long id = 24L;
        Long timeId = 2L;
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        Long updatedTimeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().put("/reservations/" + id)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("timeResponse.id");

        assertThat(updatedTimeId).isEqualTo(timeId);
    }

    @Test
    @DisplayName("사용자는 예약 날짜를 변경할 수 있다.")
    void modifyReservationDate_Success() {
        long id = 24L;
        String date = "2026-05-13";
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        String updatedDate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().put("/reservations/" + id)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getString("date");

        assertThat(updatedDate).isEqualTo(date);
    }

    @Test
    @DisplayName("사용자는 예약 내역과 예약 대기 내역을 조회할 수 있다.")
    void getReservationAndWaitList_Success() {
        JsonPath jsonPath = RestAssured.given().log().all()
                .when().get("/reservations?userName=" + "토리")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath();

        List<ReservationResponse> reservationResponses = jsonPath.getList("reservationResponses");
        List<WaitingDetailResponse> waitingDetailResponses = jsonPath.getList("waitingDetailResponses");

        assertThat(reservationResponses).hasSize(0);
        assertThat(waitingDetailResponses).hasSize(1);
    }

    @Test
    @DisplayName("사용자가 예약을 취소하면 다른 대기자의 순번이 줄어든다.")
    void deleteReservationAndPromoteWaiting_Success() {
        int expectedSequence = 1;

        RestAssured.given().log().all()
                .when().delete("/reservations/" + initialConfirmedSize + "?userName=" + userName)
                .then().log().all()
                .statusCode(204);

        JsonPath jsonPath = RestAssured.given().log().all()
                .when().get("/reservations?userName=" + "로운")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath();

        int sequence = jsonPath.getInt("waitingDetailResponses[0].sequence");

        assertThat(sequence).isEqualTo(expectedSequence);
    }

    @Test
    @DisplayName("예약 등록 시, 사용자 이름이 null 이면 400 에러를 반환한다.")
    void registerReservation_WhenUserNameIsNull_Return400() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", FUTURE_DATE);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 등록 시, 예약 날짜가 null 이면 400 에러를 반환한다.")
    void registerReservation_WhenDateIsNull_Return400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 등록 시, 예약 날짜의 형식이 올바르지 않으면 400 에러를 반환한다.")
    void registerReservation_WhenDateFormatIsInvalid_Return400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", "26-01-01");
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 등록 시, 시간 식별자가 null 이면 400 에러를 반환한다.")
    void registerReservation_WhenTimeIdIsNull_Return400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", FUTURE_DATE);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 등록 시, 테마 식별자가 null 이면 400 에러를 반환한다.")
    void registerReservation_WhenThemeIdIsNull_Return400() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", userName);
        params.put("date", FUTURE_DATE);
        params.put("timeId", timeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
