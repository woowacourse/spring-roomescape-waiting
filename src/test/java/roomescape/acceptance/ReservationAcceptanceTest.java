package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.domain.Member;
import roomescape.service.security.JwtProvider;

class ReservationAcceptanceTest extends AcceptanceTest {
    @Nested
    @DisplayName("예약 목록 조회 API")
    class FindAllReservations {
        @Test
        void 예약_목록을_조회할_수_있다() {
            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("예약 추가 API")
    class SaveReservation {
        Map<String, String> params = new HashMap<>();

        @BeforeEach
        void setUp() {
            params.put("name", "브라운");
            params.put("themeId", "1");
            params.put("timeId", "1");
        }


        @Test
        void 필드가_빈_값이면_예약을_추가할_수_없다() {
            params.put("date", null);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 날짜의_형식이_다르면_예약을_추가할_수_없다() {
            params.put("date", "2023-13-05");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        void 시간대와_테마가_똑같은_중복된_예약은_추가할_수_없다() {
            params.put("date", "2024-08-05");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("token", new JwtProvider().encode(new Member(1L, "a", "b", "c")))
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        void 지나간_날짜와_시간에_대한_예약은_추가할_수_없다() {
            params.put("date", "1998-12-11");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 삭제 API")
    class DeleteReservation {

        @Test
        void 예약을_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
            assertThat(countAfterDelete).isZero();
        }

        @Test
        void 존재하지_않는_예약은_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/10")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
