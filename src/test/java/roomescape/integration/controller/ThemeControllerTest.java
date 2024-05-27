package roomescape.integration.controller;

import static org.hamcrest.Matchers.is;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationFixture.ReservationOfDateAndTheme;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.themeOfName;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.fixture.MemberFixture;
import roomescape.domain.ReservationStatus;
import roomescape.dto.ThemeResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;


    @BeforeEach
    void initData() {
        RestAssured.port = port;
        reservationTimeRepository.save(DEFAULT_RESERVATION_TIME);
        memberRepository.save(DEFAULT_MEMBER);
    }

    @DisplayName("테마를 인기순으로 조회할 수 있다.")
    @Test
    void findPopularThemesTest() {
        //when
        Theme firstTheme = themeOfName("first");
        Theme secondTheme = themeOfName("second");
        Theme thirdTheme = themeOfName("third");
        Theme fourthTheme = themeOfName("fourth");

        firstTheme = themeRepository.save(firstTheme);
        secondTheme = themeRepository.save(secondTheme);
        thirdTheme = themeRepository.save(thirdTheme);
        fourthTheme = themeRepository.save(fourthTheme);

        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(1), firstTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(2), firstTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(3), firstTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(4), firstTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(5), firstTheme));

        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(1), secondTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(2), secondTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(3), secondTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(4), secondTheme));

        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(1), thirdTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(2), thirdTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(3), thirdTheme));

        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(1), fourthTheme));
        reservationRepository.save(
                ReservationOfDateAndTheme(LocalDate.now().minusDays(2), fourthTheme));

        //then
        List<ThemeResponse> themeResponses = RestAssured.given().log().all()
                .when()
                .params("count", 10)
                .get("/themes/ranking")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$", ThemeResponse.class);

        Assertions.assertThat(themeResponses).contains(
                ThemeResponse.from(firstTheme),
                ThemeResponse.from(secondTheme),
                ThemeResponse.from(thirdTheme),
                ThemeResponse.from(fourthTheme)
        );
    }

    @DisplayName("테마가 3개 존재할 때")
    @Nested
    class ExistReservationTheme {
        private Theme sameTheme = themeOfName("theme2");
        private Theme usedTheme = themeOfName("theme1");
        private Theme notUsedTheme = themeOfName("theme2");

        @BeforeEach
        void init() {
            sameTheme = themeRepository.save(sameTheme);
            usedTheme = themeRepository.save(usedTheme);
            notUsedTheme = themeRepository.save(notUsedTheme);
        }

        @DisplayName("전체 테마를 조회할 수 있다.")
        @Test
        void findReservationThemesTest() {
            RestAssured.given().log().all()
                    .when().get("/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3));
        }

        @DisplayName("테마를 생성할 수 있다.")
        @Test
        void createReservationThemeTest() {
            RestAssured.given().log().all()
                    .when()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "otherName",
                            "description", "description",
                            "thumbnail", "thumbnail"))
                    .post("/themes")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(4),
                            "name", is("otherName"),
                            "description", is("description"),
                            "thumbnail", is("thumbnail")
                    );

            RestAssured.given().when().get("/themes")
                    .then().body("size()", is(4));
        }

        @DisplayName("중복된 테마를 생성할 수 없다.")
        @Test
        void duplicatedReservationThemeTest() {
            RestAssured.given().log().all()
                    .when()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", sameTheme.getName(),
                            "description", "description",
                            "thumbnail", "thumbnail"))
                    .post("/themes")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(DUPLICATE_THEME.getMessage()));
        }

        @DisplayName("사용되지 않는 테마를 삭제할 수 있다.")
        @Test
        void deleteNotUsedThemeTest() {
            RestAssured.given().log().all()
                    .when().delete("/themes/" + notUsedTheme.getId())
                    .then()
                    .statusCode(204);

            RestAssured.given().when().get("/themes")
                    .then().body("size()", is(2));
        }

        @DisplayName("사용되는 테마를 삭제할 수 없다.")
        @Test
        void deleteUsedThemeTest() {
            reservationRepository.save(ReservationOfDateAndTheme(LocalDate.now(), usedTheme));

            RestAssured.given().log().all()
                    .when().delete("/themes/" + usedTheme.getId())
                    .then()
                    .statusCode(400)
                    .body("detail", is(DELETE_USED_THEME.getMessage()));

            RestAssured.given().when().get("/themes")
                    .then().body("size()", is(3));
        }
    }
}
