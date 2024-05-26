package roomescape.time.controller;

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
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.repository.TimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TimeControllerTest {
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
    @DisplayName("성공 : 시간 정보를 얻을 수 있다.")
    void findTimes() {
        int actualSize = RestAssured
                .when().get("/times")
                .then()
                .statusCode(200).extract()
                .jsonPath().getInt("size()");

        List<Time> expected = timeRepository.findAll();

        assertThat(actualSize).isEqualTo(expected.size());
    }

    @Test
    @DisplayName("성공 : 시간을 만들 수 있다.")
    void createTime() {
        TimeRequest params = new TimeRequest(LocalTime.of(10, 0));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then()
                .statusCode(201)
                .header("Location", "/times/2");
        List<Time> actual = timeRepository.findAll();

        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("성공 : 시간을 제거할 수 있다.")
    void deleteTime() {
        reservationRepository.deleteAll();
        detailRepository.deleteAll();

        RestAssured.given()
                .when()
                .delete("/times/1")
                .then()
                .statusCode(204);

        Optional<Time> expected = timeRepository.findById(1L);
        assertThat(expected).isEmpty();
    }
}
