package roomescape.integrate.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.dto.reservation.ThemeResponseDto;
import roomescape.repository.reservation.ReservationRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationIntegrateTest {

    static Map<String, String> params;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    static {
        params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "15:40");
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("drop table reservation");  // 자식 테이블 먼저
        jdbcTemplate.execute("drop table reservation_time");
        jdbcTemplate.execute("drop table theme");
    }

    @Test
    void 예약_추가_테스트() {
        Map<String, String> timeParam = new HashMap<>();
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        timeParam.put("startAt", afterTime.toString());

        Map<String, String> themeParam = new HashMap<>();
        themeParam.put("name", "테마 명");
        themeParam.put("description", "description");
        themeParam.put("thumbnail", "thumbnail");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_삭제_테스트() {
        long deleteReservationId = 1L;

        Map<String, String> timeParam = new HashMap<>();
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        timeParam.put("startAt", afterTime.toString());

        Map<String, String> themeParam = new HashMap<>();
        themeParam.put("name", "테마 명");
        themeParam.put("description", "description");
        themeParam.put("thumbnail", "thumbnail");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().delete("/reservations/" + deleteReservationId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 테마_랭킹_테스트(@Autowired ReservationRepository reservationRepository) {
        Map<String, String> timeParam = new HashMap<>();
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        timeParam.put("startAt", afterTime.toString());

        Map<String, String> themeParam = new HashMap<>();
        themeParam.put("name", "테마 명1");
        themeParam.put("description", "description");
        themeParam.put("thumbnail", "thumbnail");

        Map<String, String> themeParam2 = new HashMap<>();
        themeParam2.put("name", "테마 명2");
        themeParam2.put("description", "description");
        themeParam2.put("thumbnail", "thumbnail");

        Map<String, String> themeParam3 = new HashMap<>();
        themeParam3.put("name", "테마 명3");
        themeParam3.put("description", "description");
        themeParam3.put("thumbnail", "thumbnail");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam2)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParam3)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        Reservation reservation1 = new Reservation(null, "이름", LocalDate.now().minusDays(1),
                new ReservationTime(1L, afterTime), new Theme(1L, "테마 명1", "description", "thumbnail"));
        Reservation reservation2 = new Reservation(null, "이름", LocalDate.now().minusDays(2),
                new ReservationTime(1L, afterTime), new Theme(1L, "테마 명1", "description", "thumbnail"));
        Reservation reservation3 = new Reservation(null, "이름", LocalDate.now().minusDays(3),
                new ReservationTime(1L, afterTime), new Theme(1L, "테마 명1", "description", "thumbnail"));
        Reservation reservation4 = new Reservation(null, "이름", LocalDate.now().minusDays(4),
                new ReservationTime(1L, afterTime), new Theme(2L, "테마 명2", "description", "thumbnail"));
        Reservation reservation5 = new Reservation(null, "이름", LocalDate.now().minusDays(5),
                new ReservationTime(1L, afterTime), new Theme(2L, "테마 명2", "description", "thumbnail"));
        Reservation reservation6 = new Reservation(null, "이름", LocalDate.now().minusDays(6),
                new ReservationTime(1L, afterTime), new Theme(3L, "테마 명3", "description", "thumbnail"));

        reservationRepository.add(reservation1);
        reservationRepository.add(reservation2);
        reservationRepository.add(reservation3);
        reservationRepository.add(reservation4);
        reservationRepository.add(reservation5);
        reservationRepository.add(reservation6);

        Response response = RestAssured.given().log().all()
                .when().get("/reservations/popular-themes")
                .then().log().all()
                .extract().response();

        List<ThemeResponseDto> rankingThemes = response.jsonPath().getList("", ThemeResponseDto.class);
        List<Long> rankingThemeIds = rankingThemes.stream()
                .map(ThemeResponseDto::id)
                .toList();

        assertThat(rankingThemeIds).containsExactlyElementsOf(List.of(1L, 2L, 3L));
    }
}
