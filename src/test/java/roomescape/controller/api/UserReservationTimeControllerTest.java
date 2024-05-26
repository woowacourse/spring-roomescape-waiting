package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.LoginRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.AdminReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserReservationTimeControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private AdminReservationService adminReservationService;

    private String userToken;

    @BeforeEach
    void setUpToken() {
        memberRepository.save(new Member("러너덕", "user@a.com", "123a!", Role.USER));

        LoginRequest user = new LoginRequest("user@a.com", "123a!");

        userToken = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
            .when().post("/login")
            .then().extract().cookie("token");
    }

    @DisplayName("성공: 날짜, 테마 ID로부터 예약 시간 및 가능 여부 반환")
    @Test
    void findAllWithAvailability() {
        reservationTimeService.save("10:00");
        reservationTimeService.save("23:00");
        themeService.save("t1", "d1", "https://test.com/test.jpg");
        adminReservationService.reserve(1L, LocalDate.parse("2060-01-01"), 1L, 1L);

        RestAssured.given().log().all()
            .cookie("token", userToken)
            .queryParam("date", "2060-01-01")
            .queryParam("id", 1L)
            .when().get("/times/available")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("startAt", contains("10:00", "23:00"))
            .body("alreadyBooked", contains(true, false));
    }
}
