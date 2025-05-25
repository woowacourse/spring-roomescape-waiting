package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
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
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.util.HashMap;
import java.util.Map;
import roomescape.util.JwtTokenProvider;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Sql(scripts = {"/test-data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class ReservationControllerTest {

    String loginToken;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        loginToken = jwtTokenProvider.createToken(
                new Member(1L, "가이온", "hello@woowa.com", Role.ADMIN, "password"));
    }

    @Nested
    @DisplayName("예약 조회, 생성 및 삭제")
    class ReservationPostTest {

        @DisplayName("Reservation 목록 내용 갯수를 검사한다")
        @Test
        void reservationTest() {
            RestAssured.given().log().all()
                    .cookie("token", loginToken)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("자신의 예약 정보를 불러올 수 있다")
        @Test
        void myReservationTest() {
            RestAssured.given().log().all()
                    .cookie("token", loginToken)
                    .when().get("/reservations/me")
                    .then().log().all().statusCode(200);
        }
    }

    @Nested
    class ReservationCreateTest {
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
        }
    }
}
