package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import static io.restassured.http.ContentType.JSON;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

class ThemeAcceptanceTest extends AcceptanceFixture {

    @Test
    @DisplayName("예약 테마 생성 API")
    void theme_generation_API() {
        // given
        Map<String, String> requestBody = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        // when & then
        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED).body("id", is(1));
    }

    @Test
    @DisplayName("예약 테마 목록 조회 API")
    void theme_inquiry_API() {
        // given
        Map<String, String> requestBody = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);
        RestAssured
                .given().contentType(JSON).body(requestBody)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when & then
        RestAssured
                .given()
                .when().get("/themes")
                .then().statusCode(200).body("size()", is(2));
    }

    @Test
    @DisplayName("예약 테마 삭제 API")
    void theme_remove_API() {
        // given
        Map<String, String> requestBody1 = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");
        Map<String, String> requestBody2 = Map.of("name", "theme-1", "description", "this is a new theme",
                "thumbnail", "fun");

        RestAssured
                .given().contentType(JSON).body(requestBody1)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);
        RestAssured
                .given().contentType(JSON).body(requestBody2)
                .when().post("/themes")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when
        RestAssured
                .when().delete("/themes/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given().log().all()
                .when().log().all().get("/themes")
                .then().statusCode(HttpStatus.SC_OK).body("size()", is(1))
                .log().all();
    }

    @Test
    @DisplayName("테마 랭크 조회 API")
    void theme_ranking_inquiry_API() {
        // given
        memberRepository.save(new Member("aa", "aa@aa.aa", "aa", Role.ADMIN));
        Member member = memberRepository.save(new Member("aa", "aa@aa.aa", "aa", Role.ADMIN));
        ReservationTime time = timeRepository.save(new ReservationTime(LocalTime.of(1, 0)));
        List<Theme> themes = setThemes(11);
        addReservationBy(themes.get(1), time, member, 11);
        addReservationBy(themes.get(2), time, member, 10);
        addReservationBy(themes.get(0), time, member, 9);
        addReservationBy(themes.get(3), time, member, 8);
        addReservationBy(themes.get(4), time, member, 6);
        addReservationBy(themes.get(6), time, member, 5);
        addReservationBy(themes.get(7), time, member, 4);
        addReservationBy(themes.get(5), time, member, 3);

        // when
        RestAssured
                .given()
                .when().get("/themes/ranking")
                .then().body("size()", is(8))
                .body("[0].id", is(2))
                .body("[1].id", is(3))
                .body("[2].id", is(1))
                .body("[3].id", is(4))
                .body("[4].id", is(5))
                .body("[5].id", is(7))
                .body("[6].id", is(8))
                .body("[7].id", is(6));
    }

    private List<Theme> setThemes(int count) {
        List<Theme> themes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Theme theme = new Theme("name", "desc", "thumb");
            themeRepository.save(theme);
            themes.add(theme);
        }
        return themes;
    }

    private void addReservationBy(Theme theme, ReservationTime time, Member member, int count) {
        for (int i = 1; i <= count; i++) {
            reservationRepository.save(new Reservation(LocalDate.now().minusDays(count % 7), time, theme, member));
        }
    }

}
