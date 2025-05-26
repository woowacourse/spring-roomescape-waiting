package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createNewReservation;
import static roomescape.TestFixture.createTimeFrom;

import io.restassured.RestAssured;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.dto.request.CreateReservationTimeRequest;
import roomescape.controller.dto.response.AvailableReservationTimeResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationTimeRepository;

class ReservationTimeControllerTest extends AbstractRestDocsTest {

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

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
        List<ReservationTimeResponse> responses = givenWithDocs("reservationTime-get")
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
        givenWithDocs("reservationTime-create")
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
        givenWithDocs("reservationTime-delete")
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
        List<AvailableReservationTimeResponse> responses = RestAssured.given(documentationSpec)
                .filter(document("reservationTime-available-get",
                        queryParameters(
                                parameterWithName("themeId").description("테마 ID"),
                                parameterWithName("date").description("예약 날짜")
                        )
                ))
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
