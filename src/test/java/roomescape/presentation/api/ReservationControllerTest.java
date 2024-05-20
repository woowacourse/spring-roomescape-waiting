package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.presentation.BaseControllerTest;
import roomescape.presentation.dto.request.ReservationWebRequest;

@Sql("/member.sql")
class ReservationControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @Sql("/waitings.sql")
    @DisplayName("나의 예약들을 예약 대기 순번과 함께 조회하고, 성공하면 200을 반환한다.")
    void getMyReservations() {
        adminLogin();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations/mine")
                .then().log().all()
                .extract();

        List<MyReservationResponse> reservationResponses = response.jsonPath()
                .getList(".", MyReservationResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(reservationResponses).hasSize(3);

            softly.assertThat(reservationResponses.get(0).id()).isEqualTo(1);
            softly.assertThat(reservationResponses.get(0).rank()).isEqualTo(0);

            softly.assertThat(reservationResponses.get(1).id()).isEqualTo(7);
            softly.assertThat(reservationResponses.get(1).rank()).isEqualTo(1);

            softly.assertThat(reservationResponses.get(2).id()).isEqualTo(8);
            softly.assertThat(reservationResponses.get(2).rank()).isEqualTo(0);
        });
    }

    @Nested
    @DisplayName("예약을 생성하는 경우")
    class AddReservation {

        @Test
        @DisplayName("성공할 경우 201을 반환한다.")
        void addReservation() {
            userLogin();

            reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
            themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));

            ReservationWebRequest request = new ReservationWebRequest(LocalDate.of(2024, 4, 9), 1L, 1L);

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .extract();

            ReservationResponse reservationResponse = response.as(ReservationResponse.class);
            MemberResponse memberResponse = reservationResponse.member();
            ReservationTimeResponse reservationTimeResponse = reservationResponse.time();
            ThemeResponse themeResponse = reservationResponse.theme();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
                softly.assertThat(response.header("Location")).isEqualTo("/reservations/1");

                softly.assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2024, 4, 9));
                softly.assertThat(memberResponse).isEqualTo(new MemberResponse(2L, "user@gmail.com", "유저", Role.USER));
                softly.assertThat(reservationTimeResponse)
                        .isEqualTo(new ReservationTimeResponse(1L, LocalTime.of(11, 0)));
                softly.assertThat(themeResponse)
                        .isEqualTo(new ThemeResponse(1L, "테마 이름", "테마 설명", "https://example.com"));
            });
        }

        @Test
        @DisplayName("지나간 날짜/시간이면 400을 반환한다.")
        void failWhenDateTimePassed() {
            userLogin();

            reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
            themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));

            ReservationWebRequest request = new ReservationWebRequest(LocalDate.of(2024, 4, 7), 1L, 1L);

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .extract();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                softly.assertThat(response.body().asString()).contains("지나간 날짜/시간에 대한 예약은 불가능합니다.");
            });
        }
    }
}
