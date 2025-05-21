package roomescape.presentation.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.createReservationBody;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.infrastructure.jwt.JwtTokenProvider;
import roomescape.testFixture.JdbcHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminReservationControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReservationRepository reservationRepository;

    private String tokenForAdmin;
    private String tokenForUser;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;

        tokenForAdmin = jwtTokenProvider.createToken(new MemberIdDto(MEMBER1_ADMIN.getId()));
        tokenForUser = jwtTokenProvider.createToken(new MemberIdDto(MEMBER2_USER.getId()));
    }

    @DisplayName("/admin/reservations 요청 시 201 CREATED")
    @Test
    public void request_createReservation() {
        resetH2TableIds(jdbcTemplate);
        JdbcHelper.insertMember(jdbcTemplate, MEMBER1_ADMIN);
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);
        JdbcHelper.insertTheme(jdbcTemplate, Theme.withoutId("테마1", "테마 1입니다.", "썸네일입니다."));
        JdbcHelper.insertReservationTime(jdbcTemplate, ReservationTime.of(1L, LocalTime.of(10, 0)));

        int repositorySize = reservationRepository.findAll().size();
        int expectedSize = repositorySize + 1;

        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .contentType(ContentType.JSON)
                .body(createReservationBody(2L))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(expectedSize));

        int afterAddSize = reservationRepository.findAll().size();
        assertThat(afterAddSize).isEqualTo(expectedSize);
    }

    @DisplayName("관리자가 아닌 일반 유저가 POST /admin/reservations 요청 시 401 Unauthorized")
    @Test
    public void request_createReservation_unauthorized() {
        resetH2TableIds(jdbcTemplate);
        JdbcHelper.insertMember(jdbcTemplate, MEMBER2_USER);

        RestAssured.given().log().all()
                .cookie("token", tokenForUser)
                .contentType(ContentType.JSON)
                .body(createReservationBody(2L))
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("어드민에서 조회 조건에 따라 예약 목록 조회 성공")
    @Test
    @Sql("/test-admin-get-reservations-data.sql")
    public void request_getReservationsUsingFilter() {
        RestAssured.given().log().all()
                .param("themeId", 1)
                .param("memberId", 3)
                .param("dateFrom", "2025-04-25")
                .param("dateTo", "2025-04-26")
                .cookie("token", tokenForAdmin)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("어드민에서 조회 조건에 themeId를 없이 예약 목록을 조회해도 조회 성공")
    @Test
    @Sql("/test-admin-get-reservations-data.sql")
    public void request_getReservationsWithoutThemeId() {
        RestAssured.given().log().all()
                .param("memberId", 3)
                .param("dateFrom", "2025-04-23")
                .param("dateTo", "2025-04-26")
                .cookie("token", tokenForAdmin)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(11));
    }

    @DisplayName("어드민에서 조회 조건 없이 예약 목록을 조회해도 조회 성공")
    @Test
    @Sql("/test-admin-get-reservations-data.sql")
    public void request_getReservationsWithNoConditons() {
        RestAssured.given().log().all()
                .cookie("token", tokenForAdmin)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(15));
    }
}
