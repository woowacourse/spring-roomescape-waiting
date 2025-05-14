package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.dto.reservation.AdminReservationCreateRequestDto;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest {

    @Nested
    class AdminAddReservationTest {

        @Autowired
        MemberRepository memberRepository;

        String loginToken;

        @BeforeEach
        void setUp() {
            LocalTime reservationTime = LocalTime.of(15, 30);
            ReservationTimeCreateRequestDto requestTime = new ReservationTimeCreateRequestDto(reservationTime);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201);

            ThemeCreateRequestDto themeCreateRequestDto = new ThemeCreateRequestDto("테마1", "설명1", "url");
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(themeCreateRequestDto)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201);

            Member admin = new Member(1L, "가이온", "hello@woowa.com", Role.ADMIN, "password");
            memberRepository.save(admin);

            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            Map<String, String> cookies = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();

            loginToken = cookies.get("token");
        }

        @DisplayName("어드민 예약 추가 테스트")
        @Test
        void addReservationTest() {
            AdminReservationCreateRequestDto dto = new AdminReservationCreateRequestDto(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all().statusCode(201);
        }

        @DisplayName("themeId가 존재하지 않는 경우 예약을 추가할 수 없다.")
        @Test
        void invalidThemeIdReservationTest() {
            AdminReservationCreateRequestDto dto = new AdminReservationCreateRequestDto(
                    LocalDate.now().plusDays(1), 2L, 1L, 1L);
            RestAssured.given().cookie(loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all().statusCode(401);
        }

        @DisplayName("timeId가 존재하지 않는 경우 예약을 추가할 수 없다.")
        @Test
        void invalidTimeIdReservationTest() {
            AdminReservationCreateRequestDto dto = new AdminReservationCreateRequestDto(
                    LocalDate.now().plusDays(1), 1L, 2L, 1L);
            RestAssured.given().cookie(loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all().statusCode(401);
        }

        @DisplayName("memberId가 존재하지 않는 경우 예약을 추가할 수 없다.")
        @Test
        void invalidMemberIdReservationTest() {
            AdminReservationCreateRequestDto dto = new AdminReservationCreateRequestDto(
                    LocalDate.now().plusDays(1), 1L, 1L, 2L);
            RestAssured.given().cookie(loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all().statusCode(401);
        }
    }

    @Nested
    class searchAdminReservationTest {

        @Autowired
        MemberRepository memberRepository;

        String loginToken;

        @BeforeEach
        void setUp() {
            LocalTime reservationTime = LocalTime.of(15, 30);
            ReservationTimeCreateRequestDto requestTime = new ReservationTimeCreateRequestDto(reservationTime);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(requestTime)
                    .when().post("/times")
                    .then()
                    .statusCode(201);

            ThemeCreateRequestDto themeCreateRequestDto = new ThemeCreateRequestDto("테마1", "설명1", "url");
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(themeCreateRequestDto)
                    .when().post("/themes")
                    .then()
                    .statusCode(201);

            Member admin = new Member(1L, "가이온", "hello@woowa.com", Role.ADMIN, "password");
            memberRepository.save(admin);

            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");
            Map<String, String> cookies = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();
            loginToken = cookies.get("token");

            for (int day = 1; day < 5; day++) {
                AdminReservationCreateRequestDto dto1 = new AdminReservationCreateRequestDto(
                        LocalDate.now().plusDays(day), 1L, 1L, 1L);
                RestAssured.given().cookie("token", loginToken)
                        .contentType(ContentType.JSON)
                        .body(dto1)
                        .when().post("/admin/reservations")
                        .then()
                        .statusCode(201);
            }
        }

        @DisplayName("특정 기간 내 예약을 조회할 수 있다")
        @Test
        void searchAdminReservationTest() {
            Map<String, Object> params = Map.of(
                    "themeId", 1L,
                    "memberId", 1L,
                    "dateFrom", "2025-05-01",
                    "dateTo", "2025-05-30");

            RestAssured.given().cookie("token", loginToken).log().all()
                    .queryParams(params)
                    .when().get("/admin/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(4));
        }
    }
}
