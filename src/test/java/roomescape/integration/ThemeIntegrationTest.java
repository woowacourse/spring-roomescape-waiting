package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.service.theme.dto.ThemeListResponse;

class ThemeIntegrationTest extends IntegrationTest {
    @Nested
    @DisplayName("테마 목록 조회 API")
    class FindAllTheme {
        @Test
        void 테마_목록을_조회할_수_있다() {
            themeFixture.createFirstTheme();

            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("인기 테마 목록 조회 API")
    class FindAllPopularTheme {
        @Test
        void 최근_일주일동안_예약_건수_많은_순서대로_10개_테마를_인기_테마로_조회할_수_있다() {
            ReservationTime time = timeFixture.createPastTime();
            Theme theme = themeFixture.createFirstTheme();
            Member member = memberFixture.createUserMember();
            reservationFixture.createPastReservation(time, theme, member);

            RestAssured.given().log().all()
                    .when().get("/themes/popular")
                    .then().log().all()
                    .statusCode(200)
                    .body("themes.size()", is(1));

            ThemeListResponse response = RestAssured.get("/themes/popular")
                    .as(ThemeListResponse.class);
            assertThat(response.getThemes().get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("테마 추가 API")
    class SaveTheme {
        Map<String, String> params = new HashMap<>();

        @BeforeEach
        void setUp() {
            params.put("name", "레벨3");
            params.put("thumbnail", "https://naver.com");
            memberFixture.createAdminMember();
        }

        @Test
        void 테마를_추가할_수_있다() {
            params.put("description", "내용이다.");

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/themes/1")
                    .body("id", is(1));
        }

        @Test
        void 필드가_빈_값이면_테마를_추가할_수_없다() {
            params.put("description", null);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("테마 삭제 API")
    class DeleteTheme {
        Theme theme;
        Member member;

        @BeforeEach
        void setUp() {
            theme = themeFixture.createFirstTheme();
            member = memberFixture.createAdminMember();
        }

        @Test
        void 테마를_삭제할_수_있다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        void 존재하지_않는_테마는_삭제할_수_없다() {
            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/themes/13")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        void 예약이_존재하는_테마는_삭제할_수_없다() {
            ReservationTime time = timeFixture.createPastTime();
            reservationFixture.createFutureReservation(time, theme, member);

            RestAssured.given().log().all()
                    .cookies(cookieProvider.createAdminCookies())
                    .when().delete("/themes/1")
                    .then().log().all()
                    .statusCode(400);
        }
    }
}
