package roomescape.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.domain.ReservationStatus;
import roomescape.dto.AvailableTimeResponse;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.fixture.ReservationFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void initData() {
        RestAssured.port = port;
        themeRepository.save(DEFAULT_THEME);
        memberRepository.save(DEFAULT_MEMBER);
    }

    @DisplayName("여러 예약이 존재할 때 예약 가능 시간을 조회할 수 있다.")
    @Test
    void findAvailableTimesTest() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");
        theme = themeRepository.save(theme);

        ReservationTime usedReservationTime = DEFAULT_RESERVATION_TIME;
        ReservationTime notUsedReservationTime = new ReservationTime(LocalTime.of(12, 30));
        reservationTimeRepository.save(usedReservationTime);
        notUsedReservationTime = reservationTimeRepository.save(notUsedReservationTime);

        LocalDate findDate = LocalDate.of(2024, 5, 4);
        reservationRepository.save(
                new Reservation(findDate, usedReservationTime, theme, DEFAULT_MEMBER, ReservationStatus.BOOKED));

        //when
        List<AvailableTimeResponse> availableTimeResponses = RestAssured.given().log().all()
                .when().params(Map.of("date", findDate.toString(),
                        "themeId", theme.getId()))
                .get("/times/available")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList("$", AvailableTimeResponse.class);

        assertThat(availableTimeResponses).contains(
                new AvailableTimeResponse(1, usedReservationTime.getStartAt(), true),
                new AvailableTimeResponse(2, notUsedReservationTime.getStartAt(), false)
        );
    }

    @DisplayName("예약 시간이 1개 존재할 때")
    @Nested
    class ExistReservationTime {
        private final ReservationTime usedReservationTime = DEFAULT_RESERVATION_TIME;
        private final ReservationTime notUsedReservationTime = new ReservationTime(LocalTime.of(12, 30));

        @BeforeEach
        void init() {
            reservationTimeRepository.save(usedReservationTime);
            reservationTimeRepository.save(notUsedReservationTime);
        }

        @DisplayName("전체 예약 시간을 조회할 수 있다.")
        @Test
        void findReservationTimesTest() {
            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("예약 시간을 생성할 수 있다.")
        @Test
        void createReservationTimeTest() {
            RestAssured.given().log().all()
                    .when()
                    .contentType(ContentType.JSON)
                    .body(Map.of("startAt", "13:30"))
                    .post("/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(3),
                            "startAt", is("13:30"));

            RestAssured.given().when().get("/times")
                    .then().body("size()", is(3));
        }

        @DisplayName("중복된 예약 시간을 생성할 수 없다.")
        @Test
        void duplicatedReservationTimeTest() {
            RestAssured.given().log().all()
                    .when()
                    .contentType(ContentType.JSON)
                    .body(Map.of("startAt", usedReservationTime.getStartAt().toString()))
                    .post("/times")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(DUPLICATE_RESERVATION_TIME.getMessage()));
        }

        @DisplayName("사용되지 않는 예약 시간을 삭제할 수 있다.")
        @Test
        void deleteNotUsedTimeTest() {
            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then()
                    .statusCode(204);

            RestAssured.given().when().get("/times")
                    .then().body("size()", is(1));
        }

        @DisplayName("사용되는 예약 시간을 삭제할 수 없다.")
        @Test
        void deleteUsedTimeTest() {
            reservationRepository.save(
                    ReservationFixture.ReservationOfDate(LocalDate.now())
            );

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then()
                    .statusCode(400)
                    .body("detail", is(DELETE_USED_TIME.getMessage()));

            RestAssured.given().when().get("/times")
                    .then().body("size()", is(2));
        }
    }
}
