package roomescape.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDetailRepository detailRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;

    private Member member = new Member("범블비", "aa@email.com", "1111");
    private Theme theme = new Theme("Harry Potter", "해리포터와 도비", "thumbnail.jpg");
    private Time time = new Time(LocalTime.of(12, 0));
    private ReservationDetail reservationDetail = new ReservationDetail(theme, time, LocalDate.MAX);
    private Reservation reservation = new Reservation(member, reservationDetail);

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        member = memberRepository.save(member);
        theme = themeRepository.save(theme);
        time = timeRepository.save(time);
        reservationDetail = detailRepository.save(reservationDetail);
        reservation = reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("성공 : 테마 정보를 얻을 수 있다.")
    void findThemes() {
        int actualSize = RestAssured
                .when().get("/themes")
                .then()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        List<Theme> expected = themeRepository.findAll();

        assertThat(actualSize).isEqualTo(expected.size());
    }

    @Test
    @DisplayName("성공 : 최근 7일간 인기 테마 정보를 얻을 수 있다.")
    void findThemeRanking() {
        ReservationDetail lastReservationDetail = new ReservationDetail(theme, time, LocalDate.now().minusDays(8));
        detailRepository.save(lastReservationDetail);
        Reservation lastReservation = new Reservation(member, lastReservationDetail);
        reservationRepository.save(lastReservation);

        int actualSize = RestAssured
                .when().get("/themes/rank")
                .then()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        assertThat(actualSize).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 : 테마를 만들 수 있다.")
    void createTheme() {
        ThemeRequest params = new ThemeRequest("Harry Potter2", "해리포터와 도비2", "thumbnail.jpg");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then()
                .statusCode(201)
                .header("Location", "/themes/2");
        List<Theme> actual = themeRepository.findAll();

        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("성공 : 테마를 제거할 수 있다.")
    void deleteTheme() {
        reservationRepository.deleteAll();
        detailRepository.deleteAll();

        RestAssured.given()
                .when()
                .delete("/themes/1")
                .then()
                .statusCode(204);

        Optional<Theme> expected = themeRepository.findById(1L);
        assertThat(expected).isEmpty();
    }
}
