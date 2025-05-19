package roomescape.presentation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.THEME_1;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import java.time.LocalDate;
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
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", RESERVATION_TIME_1.getStartAt());
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                THEME_1.getName(),
                THEME_1.getDescription(),
                THEME_1.getThumbnail());
        jdbcTemplate.update(
                "INSERT INTO member (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)",
                MEMBER2_USER.getId(),
                MEMBER2_USER.getName(),
                MEMBER2_USER.getEmail(),
                MEMBER2_USER.getPassword(),
                MEMBER2_USER.getRole().name()
        );

        jdbcTemplate.update(
                "INSERT INTO reservation (date, status, time_id, member_id, theme_id) VALUES (?, 'RESERVED', ?, ?, ?)",
                LocalDate.now().plusDays(1),
                RESERVATION_TIME_1.getId(),
                MEMBER2_USER.getId(),
                THEME_1.getId()
        );

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }
}
