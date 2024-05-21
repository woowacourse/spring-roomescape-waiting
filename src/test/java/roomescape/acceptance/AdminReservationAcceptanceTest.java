package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-reservations.sql")
class AdminReservationAcceptanceTest extends AcceptanceTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
    private long guestId;
    private long reservationId;
    private String adminToken;
    private String guestToken;

    @BeforeEach
    void init() {
        date = LocalDate.now().plusDays(1);
        timeId = 1;
        themeId = 1;
        guestId = 2;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guestToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(new AdminReservationRequest(date, guestId, timeId, themeId))
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 취소 성공 테스트")
    @TestFactory
    Stream<DynamicTest> deleteReservationSuccess() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 저장하고, 식별자를 가져온다.", () -> {
                    reservationId = (int) RestAssured.given().contentType(ContentType.JSON)
                            .cookie("token", guestToken)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/admin/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("남은 예약 개수는 총 3개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().body("size()", is(3));
                })
        );
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 사용자, 테마")
    @Test
    void findByMemberAndTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("memberId", 1)
                .queryParam("themeId", 2)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(0));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 시작 날짜")
    @Test
    void findByDateFrom() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("dateFrom", LocalDate.now().plusDays(7).toString())
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 테마")
    @Test
    void findByTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("themeId", 1)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(1));
    }
}
