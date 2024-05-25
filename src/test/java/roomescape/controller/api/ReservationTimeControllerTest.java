package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.BaseControllerTest;
import roomescape.controller.dto.request.ReservationTimeRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.dto.response.AvailableReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

class ReservationTimeControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @TestFactory
    @DisplayName("예약 시간을 생성, 조회, 삭제한다.")
    Stream<DynamicTest> addReservationTimeAndGetAndDelete() {
        return Stream.of(
                DynamicTest.dynamicTest("예약 시간을 생성한다.", this::addReservationTime),
                DynamicTest.dynamicTest("예약 시간을 모두 조회한다.", this::getAllReservationTimes),
                DynamicTest.dynamicTest("예약 시간을 삭제한다.", this::deleteReservationTimeById)
        );
    }

    @TestFactory
    @DisplayName("중복된 예약 시간을 생성하면 실패한다.")
    Stream<DynamicTest> failWhenDuplicatedTime() {
        return Stream.of(
                DynamicTest.dynamicTest("예약 시간을 생성한다.", this::addReservationTime),
                DynamicTest.dynamicTest("이미 존재하는 예약 시간을 생성한다.", this::addReservationTimeFailWhenDuplicatedTime)
        );
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 실패한다.")
    void deleteReservationTimeByIdFailWhenNotFoundId() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 id의 시간이 존재하지 않습니다.");
        });
    }

    @Test
    @DisplayName("이미 사용 중인 예약 시간을 삭제하면 실패한다.")
    void deleteReservationTimeByIdFailWhenUsedTime() {
        Member member = memberRepository.save(new Member("member@gmail.com", "password", "member", Role.USER));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        reservationRepository.save(new Reservation(LocalDate.of(2024, 4, 9), member, reservationTime, theme));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 시간을 사용하는 예약이 존재합니다.");
        });
    }

    @Test
    @DisplayName("이용가능한 시간들을 조회한다.")
    @Sql("/reservation.sql")
    void getAvailableReservationTimes() {
        ExtractableResponse<Response> extractResponse = RestAssured.given().log().all()
                .param("date", "2024-04-09")
                .param("themeId", 1L)
                .when().get("/times/available")
                .then().log().all()
                .extract();

        List<AvailableReservationTimeResponse> responses = extractResponse.jsonPath()
                .getList("list", AvailableReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(4);

            softly.assertThat(responses.get(0).timeId()).isEqualTo(1L);
            softly.assertThat(responses.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(responses.get(0).alreadyBooked()).isFalse();

            softly.assertThat(responses.get(1).timeId()).isEqualTo(2L);
            softly.assertThat(responses.get(1).startAt()).isEqualTo("12:00");
            softly.assertThat(responses.get(1).alreadyBooked()).isTrue();

            softly.assertThat(responses.get(2).timeId()).isEqualTo(3L);
            softly.assertThat(responses.get(2).startAt()).isEqualTo("17:00");
            softly.assertThat(responses.get(2).alreadyBooked()).isFalse();

            softly.assertThat(responses.get(3).timeId()).isEqualTo(4L);
            softly.assertThat(responses.get(3).startAt()).isEqualTo("21:00");
            softly.assertThat(responses.get(3).alreadyBooked()).isTrue();
        });
    }

    private void addReservationTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .extract();

        ReservationTimeResponse reservationTimeResponse = response.as(ReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(response.header("Location")).isEqualTo("/times/1");
            softly.assertThat(reservationTimeResponse).isEqualTo(new ReservationTimeResponse(1L, LocalTime.of(10, 30)));
        });
    }

    private void addReservationTimeFailWhenDuplicatedTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 시간은 이미 존재합니다.");
        });
    }

    private void getAllReservationTimes() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .extract();

        List<ReservationTimeResponse> reservationTimeResponses = response.jsonPath()
                .getList("list", ReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(reservationTimeResponses).hasSize(1);
            softly.assertThat(reservationTimeResponses)
                    .containsExactly(new ReservationTimeResponse(1L, LocalTime.of(10, 30)));
        });
    }

    private void deleteReservationTimeById() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        });
    }
}
