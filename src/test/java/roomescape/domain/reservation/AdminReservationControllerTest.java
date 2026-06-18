package roomescape.domain.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
class AdminReservationControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @LocalServerPort
    private int port;

    @Value("${token}")
    private String adminToken;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationDate futureDate;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        futureDate = reservationDateRepository.save(ReservationDate.createWithoutId(LocalDate.now().plusDays(10)));
        time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(22, 0)));
        theme = themeRepository.save(Theme.createWithoutId("테스트테마", "설명", "url"));
    }

    @Test
    @DisplayName("관리자 권한으로 모든 예약을 조회한다.")
    void getAllReservations() {
        Member member = memberRepository.save(Member.createWithoutId("관리자조회용"));
        reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("any { it.name == '관리자조회용' }", is(true));
    }

    @Test
    @DisplayName("관리자 권한으로 예약을 삭제한다.")
    void deleteReservation() {
        Member member = memberRepository.save(Member.createWithoutId("삭제될예약"));
        Reservation saved = reservationRepository.save(Reservation.createWithoutId(member, futureDate, time, theme));

        RestAssured.given().log().all()
                .header(ADMIN_HEADER, adminToken)
                .when().delete("/admin/reservations/" + saved.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("관리자 토큰 없이 접근할 경우 401 에러가 발생한다.")
    void unauthorizedAccess() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }
}
