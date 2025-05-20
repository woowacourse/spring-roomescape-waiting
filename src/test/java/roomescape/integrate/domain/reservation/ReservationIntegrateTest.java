package roomescape.integrate.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationmember.MyReservationMemberResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.integrate.fixture.RequestFixture;
import roomescape.repository.reservation.ReservationRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ReservationIntegrateTest {

    private final RequestFixture requestFixture = new RequestFixture();

    private static Map<String, String> cookies;
    private long themeId;
    private long timeId;

    @BeforeEach
    void setup() {
        requestFixture.reqeustSignup("투다", "test@email.com", "testtest");
        cookies = requestFixture.requestLogin("test@email.com", "testtest");
        themeId = requestFixture.requestAddTheme("테마 명", "description", "thumbnail");
        LocalTime afterTime = LocalTime.now().plusMinutes(1L);
        timeId = requestFixture.requestAddTime(afterTime.toString());
    }

    @Test
    void 예약_추가_테스트() {
        requestFixture.requestAddReservation("예약", LocalDate.now().toString(), themeId, timeId, cookies);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 예약_삭제_테스트() {
        long reservationId = requestFixture.requestAddReservation("예약", LocalDate.now().toString(), themeId, timeId,
                cookies);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 테마_랭킹_테스트(@Autowired ReservationRepository reservationRepository) {
        LocalTime afterTime = LocalTime.now().plusMinutes(1);
        long timeId = requestFixture.requestAddTime(afterTime.toString());

        long themeId1 = requestFixture.requestAddTheme("테마 명1", "description", "thumbnail");
        long themeId2 = requestFixture.requestAddTheme("테마 명2", "description", "thumbnail");
        long themeId3 = requestFixture.requestAddTheme("테마 명3", "description", "thumbnail");

        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(1),
                new ReservationTime(timeId, afterTime), new Theme(themeId1, "테마 명1", "description", "thumbnail")));
        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(2),
                new ReservationTime(timeId, afterTime), new Theme(themeId1, "테마 명1", "description", "thumbnail")));
        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(3),
                new ReservationTime(timeId, afterTime), new Theme(themeId1, "테마 명1", "description", "thumbnail")));
        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(4),
                new ReservationTime(timeId, afterTime), new Theme(themeId2, "테마 명2", "description", "thumbnail")));
        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(5),
                new ReservationTime(timeId, afterTime), new Theme(themeId2, "테마 명2", "description", "thumbnail")));
        reservationRepository.save(new Reservation(null, "이름", LocalDate.now().minusDays(6),
                new ReservationTime(timeId, afterTime), new Theme(themeId3, "테마 명3", "description", "thumbnail")));

        Response response = RestAssured.given()
                .when().get("/reservations/popular-themes")
                .then()
                .extract().response();

        List<ThemeResponseDto> rankingThemes = response.jsonPath().getList("", ThemeResponseDto.class);
        List<Long> rankingThemeIds = rankingThemes.stream()
                .map(ThemeResponseDto::id)
                .toList();

        assertThat(rankingThemeIds).containsExactlyElementsOf(List.of(themeId1, themeId2, themeId3));
    }

    @Test
    void 유저의_예약을_가져올_수_있다(){
        requestFixture.requestAddReservation("예약", LocalDate.now().plusDays(1).toString(), themeId, timeId, cookies);
        requestFixture.requestAddReservation("예약2", LocalDate.now().plusDays(2).toString(), themeId, timeId, cookies);

        RestAssured.given().log().all()
                .cookies(cookies)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 유저의_예약은_예약_상태를_가진다(){
        requestFixture.requestAddReservation("예약", LocalDate.now().plusDays(1).toString(), themeId, timeId, cookies);
        requestFixture.requestAddReservation("예약2", LocalDate.now().plusDays(2).toString(), themeId, timeId, cookies);

        Response response = RestAssured.given().log().all()
                .cookies(cookies)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .extract().response();

        List<MyReservationMemberResponseDto> myReservationMemberResponseDtos = response.jsonPath()
                .getList("", MyReservationMemberResponseDto.class);

        assertThat(myReservationMemberResponseDtos.getFirst().status()).contains("예약");
    }
}
