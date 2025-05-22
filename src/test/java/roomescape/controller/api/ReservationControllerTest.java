package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.util.JwtTokenProvider;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS) //딱 1회만?
@Sql(scripts = {"/test-data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS) //모든 class마다?
@Transactional
class ReservationControllerTest {

//    @Nested
//    @DisplayName("예약 조회, 생성 및 삭제")
//    class ReservationPostTest {

    String loginToken;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        loginToken = jwtTokenProvider.createToken(
                new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password"));
    }

    @DisplayName("Reservation 목록 내용 갯수를 검사한다")
    @Test
    void reservationTest() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("Reservation을 생성한다")
    @Test
    void addReservationTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2030-08-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .cookie("token", loginToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(2));

        RestAssured.given().log().all()
                .cookie("token", loginToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("예약이 존재하면 삭제할 수 있다")
    @Test
    void deleteReservationTest() {
        RestAssured.given().log().all()
                .cookie("token", loginToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", loginToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @DisplayName("존재하지 않는 예약을 삭제할 수 없다")
    @Test
    void invalidReservationIdDeleteTest() {
        RestAssured.given().cookie("token", loginToken).log().all()
                .when().delete("/reservations/5")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("자신의 예약 정보를 불러올 수 있다")
    @Test
    void myReservationTest() {
        RestAssured.given().log().all()
                .cookie("token", loginToken)
                .when().get("/reservations/me")
                .then().log().all().statusCode(200);
    }
//}
}
