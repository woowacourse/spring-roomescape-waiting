package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-admin-and-guest.sql")
class ReservationTimeAcceptanceTest extends AcceptanceTest {
    private String adminToken;
    private String guestToken;

    @BeforeEach
    void init() {
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

    @DisplayName("어드민이 시간 정보를 추가한다.")
    @Test
    void createReservationTimeByAdmin() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest(LocalTime.of(10, 0)))
                .when().post("/times")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("일반 사용자가 시간 정보를 추가하려고 하면 예외가 발생한다.")
    @Test
    void createReservationTimeByGuest() {
        RestAssured.given().log().all()
                .cookie("token", guestToken)
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest(LocalTime.of(10, 0)))
                .when().post("/times")
                .then().log().all()
                .assertThat().statusCode(403)
                .body("message", is("권한이 없습니다. 관리자에게 문의해주세요."));
    }

    @DisplayName("시간 추가 실패 테스트 - 중복 시간 오류")
    @TestFactory
    Stream<DynamicTest> createDuplicateTime() {
        LocalTime time = LocalTime.of(10, 0);
        return Stream.of(
                DynamicTest.dynamicTest("시간을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON)
                            .body(new ReservationTimeCreateRequest(time))
                            .when().post("/times");
                }),
                DynamicTest.dynamicTest("같은 시간을 추가하려고 시도하면 400 응답을 반환한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON)
                            .body(new ReservationTimeCreateRequest(time))
                            .when().post("/times")
                            .then().log().all().statusCode(400).body("message", is("이미 같은 시간이 존재합니다."));
                })
        );
    }

    @DisplayName("등록된 시간 내역을 조회한다.")
    @TestFactory
    Stream<DynamicTest> findAllReservationTime() {
        return Stream.of(
                DynamicTest.dynamicTest("시간을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON)
                            .body(new ReservationTimeCreateRequest(LocalTime.of(10, 0)))
                            .when().post("/times");
                }),
                DynamicTest.dynamicTest("모든 시간 내역을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/times")
                            .then().log().all().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("어드민이 시간 정보를 id로 삭제한다.")
    @TestFactory
    Stream<DynamicTest> deleteReservationTimeById() {
        AtomicLong timeId = new AtomicLong();
        return Stream.of(
                DynamicTest.dynamicTest("시간을 추가한다", () -> {
                    timeId.set((int) RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON)
                            .body(new ReservationTimeCreateRequest(LocalTime.of(10, 0)))
                            .when().post("/times")
                            .then().log().all().extract().response().jsonPath().get("id"));
                    ;
                }),
                DynamicTest.dynamicTest("시간을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/times/" + timeId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("모든 시간 내역을 조회하면 남은 시간은 0개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/times")
                            .then().log().all().statusCode(200).body("size()", is(0));
                })
        );
    }

    @DisplayName("일반 사용자는 시간 정보를 삭제할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteReservationTimeByIdByGuest() {
        AtomicLong timeId = new AtomicLong();
        return Stream.of(
                DynamicTest.dynamicTest("시간을 추가한다", () -> {
                    timeId.set((int) RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON)
                            .body(new ReservationTimeCreateRequest(LocalTime.of(10, 0)))
                            .when().post("/times")
                            .then().log().all().extract().response().jsonPath().get("id"));
                    ;
                }),
                DynamicTest.dynamicTest("시간을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guestToken)
                            .when().delete("/times/" + timeId)
                            .then().log().all()
                            .assertThat().statusCode(403).body("message", is("권한이 없습니다. 관리자에게 문의해주세요."));
                })
        );
    }

    @DisplayName("시간 삭제 실패 테스트 - 이미 예약이 존재하는 시간(timeId = 1) 삭제 시도 오류")
    @Test
    @Sql("/insert-time-with-reservation.sql")
    Stream<DynamicTest> cannotDeleteReservationTime() {
        //given
        int timeId = 1;
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    adminToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("예약이 존재하는 시간을 삭제하려고 하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/times/" + timeId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("해당 시간에 예약(대기)이 존재해서 삭제할 수 없습니다."));
                })
        );
    }

    @DisplayName("예약 가능한 시간 조회 테스트 - 10:00: 예약 존재, (11:00,12:00): 예약 미존재.")
    @Test
    @Sql({"/truncate.sql", "/insert-time-with-reservation.sql"})
    void findAvailableTime() {
        //given
        long themeId = 1;
        String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        //when&then
        RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().log().all().statusCode(200).body("size()", is(3));
    }
}
