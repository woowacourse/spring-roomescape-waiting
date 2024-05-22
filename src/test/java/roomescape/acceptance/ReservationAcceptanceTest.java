package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

class ReservationAcceptanceTest extends AcceptanceFixture {

    @Test
    @DisplayName("예약 생성 API")
    void member_generate_API() {
        // given
        themeRepository.save(new Theme("name", "desc", "thumb"));
        memberRepository.save(new Member("fram", "aa@aa.aa", "aa"));
        timeRepository.save(new ReservationTime(LocalTime.of(1, 0)));

        Map<String, String> reservationBody = Map.of("date", LocalDate.now().plusDays(1).toString(), "themeId", "1",
                "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        // when & then
        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));
    }

    @Test
    @DisplayName("예약 조회 API")
    void reservation_inquiry_API() {
        // given
        themeRepository.save(new Theme("name", "desc", "thumb"));
        memberRepository.save(new Member("fram", "aa@aa.aa", "aa"));
        timeRepository.save(new ReservationTime(LocalTime.of(1, 0)));

        Map<String, String> reservationBody1 = Map.of("date", "2024-12-11", "themeId", "1", "memberId", "1", "timeId",
                "1");
        Map<String, String> reservationBody2 = Map.of("date", "2024-12-12", "themeId", "1", "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody1).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody2).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2));

        // when & then
        RestAssured
                .given()
                .when().get("/reservations")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 삭제 API")
    void reservation_remove_API() {
        // given
        themeRepository.save(new Theme("name", "desc", "thumb"));
        memberRepository.save(new Member("fram", "aa@aa.aa", "aa"));
        timeRepository.save(new ReservationTime(LocalTime.of(1, 0)));
        Map<String, String> reservationBody1 = Map.of("date", "2024-12-11", "themeId", "1", "memberId", "1", "timeId",
                "1");
        Map<String, String> reservationBody2 = Map.of("date", "2024-12-12", "themeId", "1", "memberId", "1", "timeId",
                "1");
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody1).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody2).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2));
        // when
        RestAssured
                .given()
                .when().delete("/reservations/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // when & then
        RestAssured
                .given()
                .when().get("/reservations")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(1))
                .body("[0].id", is(2));
    }
}
