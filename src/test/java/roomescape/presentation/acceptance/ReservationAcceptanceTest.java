package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.ReservationRequest;
import roomescape.application.dto.ReservationResponse;
import roomescape.domain.MemberCommandRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationCommandRepository;
import roomescape.domain.ReservationQueryRepository;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.ThemeRepository;

public class ReservationAcceptanceTest extends AcceptanceTest {

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    private static String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = RestAssured.given()
                .contentType("application/json")
                .body("{\"email\":\"admin@wooteco.com\", \"password\":\"wootecoCrew6!\"}")
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("token");
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void createReservationTest() {
        ReservationRequest request = ReservationRequestFixture.of(1L, 1L);

        ReservationResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", accessToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ReservationResponse.class);

        assertThat(response.member().name()).isEqualTo("운영자");
        assertThat(response.date()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가 요청하면 에러가 발생한다.")
    @Test
    void createNotFoundTheme() {
        ReservationRequest request = ReservationRequestFixture.of(1L, 100L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", accessToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가 요청하면 에러가 발생한다.")
    @Test
    void creatNotFoundReservationTime() {
        ReservationRequest request = ReservationRequestFixture.of(100L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", accessToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("과거 시간으로 예약 요청하면 에러가 발생한다.")
    @Test
    void createPastDate() {
        ReservationRequest request = ReservationRequestFixture.of(LocalDate.of(1999, 1, 1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", accessToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("이미 존재하는 예약을 요청하면 에러가 발생한다.")
    @Test
    void createDuplicatedReservation() {
        Reservation reservation = reservationQueryRepository.findAll().get(0);

        ReservationRequest request = ReservationRequestFixture.of(reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getTime().getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", accessToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }
}
