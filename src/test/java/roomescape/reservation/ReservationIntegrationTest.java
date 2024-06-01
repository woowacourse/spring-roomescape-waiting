package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.reservation.dao.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationContent;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
@TestMethodOrder(OrderAnnotation.class)
class ReservationIntegrationTest {

    private static final Member MEMBER = new Member(1L, "켬미", "aaa@naver.com", "1111");
    private static final Time TIME = new Time(1L, LocalTime.of(22, 59));
    private static final Theme THEME = new Theme(1L, "Harry Potter", "해리포터와 도비", "thumbnail.jpg");
    private static final ReservationContent CONTENT = new ReservationContent(LocalDate.MAX, TIME, THEME);
    private static final Reservation RESERVATION = new Reservation(MEMBER, CONTENT);
    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;
    private String cookie;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("로그인-예약 추가-예약 조건 조회-내 예약 조회-예약 삭제 시나리오 테스트.")
    @TestMethodOrder(OrderAnnotation.class)
    class LoggedInUser {

        @BeforeEach
        void login() {
            MemberLoginRequest memberLoginRequest = new MemberLoginRequest("aaa@naver.com", "1111");

            cookie = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(memberLoginRequest)
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .cookie("token");
        }

        @Test
        @Order(1)
        @DisplayName("예약을 DB에 추가할 수 있다.")
        void createReservation() {
            ReservationRequest params = new ReservationRequest(LocalDate.MAX.minusDays(1), MEMBER, 1L, 1L);

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .cookie("token", cookie)
                    .body(params)
                    .when()
                    .post("/reservations")
                    .then()
                    .statusCode(201)
                    .header("Location", "/reservations/1");

            List<Reservation> reservations = reservationRepository.findAll();

            assertThat(reservations).hasSize(2);
        }

        @Test
        @Order(2)
        @DisplayName("내 예약 목록을 읽을 수 있다.")
        void readMyReservations() {
            int size = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .cookie("token", cookie)
                    .when()
                    .get("/reservations/mine")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getInt("size()");

            assertThat(size).isEqualTo(2);
        }

        @Test
        @Order(3)
        @DisplayName("정보와 일치하는 예약 목록을 읽을 수 있다.")
        void readDetailReservations() {
            int size = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .cookie("token", cookie)
                    .when()
                    .get("/admin/reservations/search?themeId=1&memberId=1&dateFrom=2000-08-05&dateTo=2999-12-31")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getInt("size()");

            assertThat(size).isEqualTo(1);
        }

        @Test
        @Order(4)
        @DisplayName("삭제할 id를 받아서 DB에서 해당 예약을 삭제 할 수 있다.")
        void deleteReservation() {
            RestAssured.given()
                    .cookie("token", cookie)
                    .when()
                    .delete("/reservations/999")
                    .then()
                    .statusCode(204);

            Optional<Reservation> reservations = reservationRepository.findById(999L);

            assertThat(reservations).isEmpty();
        }

    }

}
