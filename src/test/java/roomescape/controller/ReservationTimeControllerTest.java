package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createTimeFrom;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createNewReservation;

import io.restassured.RestAssured;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.request.CreateReservationTimeRequest;
import roomescape.controller.response.AvailableReservationTimeResponse;
import roomescape.controller.response.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationTimeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다")
    void getAll() {
        // given
        dbHelper.insertTime(createDefaultReservationTime());

        // when & then
        List<ReservationTimeResponse> responses = given().log().all()
                .when()
                .get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", ReservationTimeResponse.class);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("예약 시간을 생성한다")
    void create() {
        // given
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(
                LocalTime.of(13, 0)
        );

        // when & then
        given().log().all()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        ReservationTime saved = reservationTimeRepository.findById(1L).get();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void delete() {
        // given
        ReservationTime reservationTime = createDefaultReservationTime();
        dbHelper.insertTime(reservationTime);

        // when & then
        given().log().all()
                .when()
                .delete("/times/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(reservationTimeRepository.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("테마와 날짜에 따른 예약 가능 시간을 조회한다")
    void getAvailableTimes() {
        // given
        ReservationTime time1 = createTimeFrom(LocalTime.of(10, 0));
        dbHelper.insertTime(time1);
        dbHelper.insertTime(createTimeFrom(LocalTime.of(11, 0)));
        Theme theme = createDefaultTheme();
        dbHelper.insertTheme(theme);

        dbHelper.insertReservation(createNewReservation(createDefaultMember(), DEFAULT_DATE, time1, theme));

        // when & then
        List<AvailableReservationTimeResponse> responses = given().log().all()
                .when()
                .get("/times/available?themeId=1&date=" + DEFAULT_DATE)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", AvailableReservationTimeResponse.class);

        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).booked()).isTrue(),
                () -> assertThat(responses.get(1).booked()).isFalse()
        );
    }
} 
