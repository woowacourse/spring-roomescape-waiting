package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.input.ThemeInput;
import roomescape.util.DatabaseCleaner;

//@formatter:off
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeApiControllerTest {

    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    ReservationService reservationService;
    @Autowired
    MemberService memberService;

    @Autowired
    ThemeService themeService;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("테마 생성에 성공하면, 201을 반환한다")
    void return_201_when_theme_create_success() {
        final Map<String, String> params = new HashMap<>();
        params.put("name", "레벨2 탈출");
        params.put("description", "우테코 레벨2를 탈출하는 내용입니다.");
        params.put("thumbnail", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        RestAssured.given().contentType(ContentType.JSON).body(params)
                   .when().post("/themes")
                   .then().statusCode(201);
    }

    @Test
    @DisplayName("테마 조회에 성공하면, 200을 반환한다")
    void return_200_when_get_themes_success() {
        final ThemeInput input = new ThemeInput(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        themeService.createTheme(input);

        RestAssured.given()
                   .when().get("/themes")
                   .then().statusCode(200).body("size()", is(1));
    }

    @Test
    @DisplayName("특정 테마가 존재하지 않는데, 그 테마를 삭제하려 할 때 404을 반환한다.")
    void return_404_when_not_exist_id() {
        RestAssured.given()
                   .when().delete("/themes/-1")
                   .then().statusCode(404);
    }

    @Test
    @DisplayName("특정 테마에 대한 예약이 존재하는데, 그 테마를 삭제하려 할 때 400를 반환한다.")
    void return_409_when_delete_id_that_exist_reservation() {
        final long timeId = reservationTimeService.createReservationTime(new ReservationTimeInput("09:00"))
                                            .id();
        final long themeId = themeService.createTheme(ThemeFixture.getInput())
                                   .id();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput()).id();
        reservationService.createReservation(new ReservationInput("2025-04-30", timeId, themeId,memberId));

        RestAssured.given()
                   .when().delete("/themes/" + themeId)
                   .then().statusCode(400);
    }
}
