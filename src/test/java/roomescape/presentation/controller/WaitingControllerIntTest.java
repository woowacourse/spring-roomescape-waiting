package roomescape.presentation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_1;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.THEME_1;
import static roomescape.testFixture.Fixture.TOMORROW;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.infrastructure.jwt.JwtTokenProvider;
import roomescape.testFixture.JdbcHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaitingControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String tokenForUser;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;
        resetH2TableIds(jdbcTemplate);

        tokenForUser = jwtTokenProvider.createToken(new MemberIdDto(MEMBER2_USER.getId()));
    }

    @DisplayName("예약대기 생성 성공")
    @Test
    void createWaiting() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);
        JdbcHelper.insertReservationTime(jdbcTemplate, RESERVATION_TIME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_1);
        JdbcHelper.insertGameSchedule(jdbcTemplate, GAME_SCHEDULE_1);

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .contentType("application/json")
                .body("""
                        {
                            "themeId": 1,
                            "date": "%s",
                            "timeId": 1
                        }
                        """.formatted(TOMORROW))
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", is(1))
                .body("member.id", is(2))
                .body("theme.id", is(1))
                .body("date", is(TOMORROW.toString()))
                .body("time.id", is(1));
    }

    @DisplayName("예약대기 삭제 성공")
    @Test
    void deleteWaiting() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);
        JdbcHelper.insertReservationTime(jdbcTemplate, RESERVATION_TIME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_1);
        JdbcHelper.insertGameSchedule(jdbcTemplate, GAME_SCHEDULE_1);

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .contentType("application/json")
                .body("""
                        {
                            "themeId": 1,
                            "date": "%s",
                            "timeId": 1
                        }
                        """.formatted(TOMORROW))
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .when().delete("/waiting/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("예약이 없는 게임 일정에 대한 예약대기는 생성할 수 없다")
    @Test
    void cannotCreateWaitingWithoutReservation() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);
        JdbcHelper.insertReservationTime(jdbcTemplate, RESERVATION_TIME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_1);

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .contentType("application/json")
                .body("""
                        {
                            "themeId": 1,
                            "date": "%s",
                            "timeId": 1
                        }
                        """.formatted(TOMORROW))
                .when().post("/waiting")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType("text/plain")
                .body(is("예약대기를 신청할 수 없습니다. 예약하기를 이용해주세요."));
    }
} 
