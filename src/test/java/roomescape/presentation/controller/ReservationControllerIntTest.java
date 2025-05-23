package roomescape.presentation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_1;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.RESERVATION_2;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.THEME_1;
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
public class ReservationControllerIntTest {

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

    @DisplayName("사용자의 예약 목록 조회 성공")
    @Test
    void getReservationByMember() {
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);
        JdbcHelper.insertReservationTime(jdbcTemplate, RESERVATION_TIME_1);
        JdbcHelper.insertTheme(jdbcTemplate, THEME_1);
        JdbcHelper.insertGameSchedule(jdbcTemplate, GAME_SCHEDULE_1);
        JdbcHelper.insertReservation(jdbcTemplate, RESERVATION_2);

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }
}
