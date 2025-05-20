package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Sql("/member.sql")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationControllerTest {

    @Autowired
    ReservationTimeRepository timeRepository;
    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 유저_예약_생성_조회_삭제() {
        // given
        Map<String, String> params = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(1);
        params.put("date", localDate.toString());
        timeRepository.save(ReservationTime.from(LocalTime.of(10, 0)));
        themeRepository.save(Theme.of("name", "desc", "thumb"));
        params.put("timeId", "1");
        params.put("themeId", "1");

        Map<String, String> adminUser = Map.of("email", "admin@naver.com", "password", "1234");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminUser)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");

        // when
        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
