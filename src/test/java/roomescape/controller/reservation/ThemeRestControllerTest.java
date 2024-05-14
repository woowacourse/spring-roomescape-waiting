package roomescape.controller.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    private final ReservationTimeRequest reservationTimeCreate1 = new ReservationTimeRequest("10:00");
    private final ReservationTimeRequest reservationTimeCreate2 = new ReservationTimeRequest("12:00");

    private final ThemeRequest themeCreate1 = new ThemeRequest("공포", "공포는 무서워", "hi.jpg");
    private final ThemeRequest themeCreate2 = new ThemeRequest("추리", "추리는 재밌어", "hi.jpg");
    private final ThemeRequest themeCreate3 = new ThemeRequest("탈출", "탈출은 빠르게", "hi.jpg");

    private final MemberCreateRequest memberCreate1 = new MemberCreateRequest("t1@t1.com", "123", "재즈");
    private final MemberCreateRequest memberCreate2 = new MemberCreateRequest("t2@t2.com", "124", "러너덕");
    private final MemberCreateRequest memberCreate3 = new MemberCreateRequest("t3@t3.com", "125", "재즈덕");
    private final MemberCreateRequest memberCreate4 = new MemberCreateRequest("t4@t4.com", "126", "덕");

    private final AdminReservationRequest reservationCreate1 = new AdminReservationRequest(1L, 1L,
            "2100-01-01", 1L);
    private final AdminReservationRequest reservationCreate2 = new AdminReservationRequest(2L, 1L,
            "2025-08-05", 1L);
    private final AdminReservationRequest reservationCreate3 = new AdminReservationRequest(2L, 2L,
            "2025-08-05", 2L);
    private final AdminReservationRequest reservationCreate4 = new AdminReservationRequest(2L, 2L,
            "2025-08-06", 1L);

    private final Member admin = new Member(2L, "t2@t2.com", "124", "재즈", "ADMIN");
    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        adminToken = jwtManager.generateToken(admin);
    }

    private void create(String path, Object param) {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(param)
                .when().post(path)
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마를 생성하는데 성공하면 응답과 201 상태 코드를 반환한다.")
    @Test
    void return_201_when_create_theme() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(themeCreate1)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("테마 목록을 조회하는데 성공하면 응답과 200 상태 코드를 반환한다.")
    @Test
    void return_200_when_find_all_themes() {
        create("/admin/themes", themeCreate1);
        create("/admin/themes", themeCreate2);
        List<ThemeResponse> actualResponse = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ThemeResponse.class);

        ThemeResponse expectedResponse1 = new ThemeResponse(1L, "공포", "공포는 무서워", "hi.jpg");
        ThemeResponse expectedResponse2 = new ThemeResponse(2L, "추리", "추리는 재밌어", "hi.jpg");

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expectedResponse1, expectedResponse2));
    }

    @DisplayName("인기 테마를 조회하는데 성공하면 응답과 200 상태 코드를 반환한다.")
    @Test
    void return_200_when_find_top_booked_themes() {
        create("/admin/themes", themeCreate1);
        create("/admin/themes", themeCreate2);
        create("/admin/themes", themeCreate3);
        create("/admin/times", reservationTimeCreate1);
        create("/admin/times", reservationTimeCreate2);
        create("/members/signup", memberCreate1);
        create("/members/signup", memberCreate2);
        create("/members/signup", memberCreate3);
        create("/members/signup", memberCreate4);
        create("/admin/reservations", reservationCreate1);
        create("/admin/reservations", reservationCreate2);
        create("/admin/reservations", reservationCreate3);
        create("/admin/reservations", reservationCreate4);

        List<ThemeResponse> actualResponse = RestAssured.given().log().all()
                .when().get("/themes/popular?start-date=2025-08-05&end-date=2025-08-07&count=2")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ThemeResponse.class);

        List<ThemeResponse> expectedResponse = List.of(
                new ThemeResponse(2L, "추리", "추리는 재밌어", "hi.jpg"),
                new ThemeResponse(1L, "공포", "공포는 무서워", "hi.jpg")
        );

        assertThat(actualResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    @DisplayName("테마를 삭제하는데 성공하면 응답과 204 상태 코드를 반환한다.")
    @Test
    void return_204_when_delete_theme() {
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(themeCreate1)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(204);
    }
}
