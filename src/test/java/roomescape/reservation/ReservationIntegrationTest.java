package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.repository.ReservationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class ReservationIntegrationTest {
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
    @DisplayName("사용자 로그인-조회-예약 생성-삭제 시나리오 테스트")
    class ReservationManagement {
        @Test
        @DisplayName("예약 목록을 읽을 수 있다.")
        void readReservations() {
            int size = RestAssured.given()
                    .cookie("token", cookie)
                    .when()
                    .get("/reservations")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getInt("size()");

            List<Reservation> reservations = reservationRepository.findAll();

            assertThat(size).isEqualTo(reservations.size());
        }

        @Test
        @DisplayName("삭제할 id를 받아서 DB에서 해당 예약을 삭제 할 수 있다.")
        void deleteReservation() {
            RestAssured.given()
                    .when()
                    .delete("/reservations/999")
                    .then()
                    .statusCode(204);

            Optional<Reservation> reservations = reservationRepository.findById(999L);

            assertThat(reservations).isEmpty();
        }

        @Nested
        @DisplayName("When the user is logged in")
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
            @DisplayName("정보와 일치하는 예약 목록을 읽을 수 있다.")
            void readDetailReservations() {
                int size = RestAssured.given()
                        .contentType(ContentType.JSON)
                        .log()
                        .all()
                        .cookie("token", cookie)
                        .when()
                        .get("/admin/reservations/search?themeId=1&memberId=1&dateFrom=2000-08-05&dateTo=2999-08-06")
                        .then()
                        .log()
                        .all()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getInt("size()");

                assertThat(size).isEqualTo(2);
            }

            @Test
            @DisplayName("예약을 DB에 추가할 수 있다.")
            void createReservation() {
                ReservationCreateRequest params = new ReservationCreateRequest(1L, 1L, 1L, LocalDate.now().plusDays(1));

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
        }
    }
}
