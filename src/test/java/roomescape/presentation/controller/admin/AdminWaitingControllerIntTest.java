package roomescape.presentation.controller.admin;

import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_1;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_2;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.THEME_1;
import static roomescape.testFixture.Fixture.THEME_2;
import static roomescape.testFixture.Fixture.TOMORROW;
import static roomescape.testFixture.Fixture.WAITING_1;
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
class AdminWaitingControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String tokenForAdmin;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;
        resetH2TableIds(jdbcTemplate);

        tokenForAdmin = jwtTokenProvider.createToken(new MemberIdDto(MEMBER1_ADMIN.getId()));
    }

    @DisplayName("모든 예약대기 목록을 조회한다")
    @Test
    void getAllWaitings() {
        // given
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        JdbcHelper.insertReservationTime(jdbcTemplate, RESERVATION_TIME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_2);
        JdbcHelper.insertGameSchedule(jdbcTemplate, GAME_SCHEDULE_1);
        JdbcHelper.insertGameSchedule(jdbcTemplate, GAME_SCHEDULE_2);
        JdbcHelper.insertWaiting(jdbcTemplate, WAITING_1);

        // when & then
        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1))
                .body("[0].id", is(1))
                .body("[0].memberName", is(MEMBER1_ADMIN.getName()))
                .body("[0].themeName", is(THEME_2.getName()))
                .body("[0].date", is(TOMORROW.toString()))
                .body("[0].startAt", is(RESERVATION_TIME_1.getStartAt().toString()));
    }
} 
