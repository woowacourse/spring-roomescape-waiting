package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ReservationRequestFixture;
import roomescape.application.dto.ReservationRequest;
import roomescape.application.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationQueryRepository;

class ReservationAcceptanceTest extends AcceptanceTest {

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @DisplayName("예약을 추가한다.")
    @Test
    void createReservationTest() {
        memberTokenSetUp();
        ReservationRequest request = ReservationRequestFixture.of(LocalDate.of(2024,12,1), 1L, 1L);

        ReservationResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", memberToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ReservationResponse.class);

        assertAll(
                () -> assertThat(response.member().name()).isEqualTo("회원"),
                () -> assertThat(response.date()).isEqualTo(LocalDate.of(2024, 12, 1))
        );
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가 요청하면 에러가 발생한다.")
    @Test
    void createNotFoundTheme() {
        memberTokenSetUp();
        ReservationRequest request = ReservationRequestFixture.of(1L, 100L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", memberToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가 요청하면 에러가 발생한다.")
    @Test
    void creatNotFoundReservationTime() {
        memberTokenSetUp();
        ReservationRequest request = ReservationRequestFixture.of(100L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", memberToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("과거 시간으로 예약 요청하면 에러가 발생한다.")
    @Test
    void createPastDate() {
        memberTokenSetUp();
        ReservationRequest request = ReservationRequestFixture.of(LocalDate.of(1999, 1, 1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", memberToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("이미 존재하는 예약을 요청하면 에러가 발생한다.")
    @Test
    void createDuplicatedReservation() {
        memberTokenSetUp();
        Reservation reservation = reservationQueryRepository.findAll().get(0);

        ReservationRequest request = ReservationRequestFixture.of(reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getTime().getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", memberToken)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("내 예약 목록을 조회한다")
    @Test
    void findMyReservations() {
        memberTokenSetUp();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", memberToken)
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }
}
