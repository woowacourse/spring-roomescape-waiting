package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import roomescape.Fixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
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

    private Theme defaultTheme = new Theme("theme1", "description", "thumbnail");
    private Member defaultMember = Fixture.defaultMember;

    @BeforeEach
    void initData() {
        RestAssured.port = port;
        defaultTheme = themeRepository.save(defaultTheme);
        defaultMember = memberRepository.save(defaultMember);
    }

    @DisplayName("여러 예약이 존재할 때 예약 가능 시간을 조회할 수 있다.")
    @Test
    void findAvailableTimesTest() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");
        theme = themeRepository.save(theme);

        ReservationTime usedReservationTime = new ReservationTime(LocalTime.of(11, 30));
        ReservationTime notUsedReservationTime = new ReservationTime(LocalTime.of(12, 30));
        usedReservationTime = reservationTimeRepository.save(usedReservationTime);
        notUsedReservationTime = reservationTimeRepository.save(notUsedReservationTime);

        LocalDate findDate = LocalDate.of(2024, 5, 4);
        reservationRepository.save(
                new Reservation(findDate, usedReservationTime, theme, defaultMember.getLoginMember()));

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
        private final ReservationTime notUsedReservationTime = new ReservationTime(LocalTime.of(12, 30));
        private ReservationTime usedReservationTime = new ReservationTime(LocalTime.of(11, 30));

        @BeforeEach
        void init() {
            usedReservationTime = reservationTimeRepository.save(usedReservationTime);
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
                    .body("message", is(DUPLICATE_RESERVATION_TIME.getMessage()));
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
                    new Reservation(LocalDate.now(), usedReservationTime, defaultTheme, defaultMember.getLoginMember())
            );

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then()
                    .statusCode(400)
                    .body("message", is(DELETE_USED_TIME.getMessage()));

            RestAssured.given().when().get("/times")
                    .then().body("size()", is(2));
        }
    }
}
