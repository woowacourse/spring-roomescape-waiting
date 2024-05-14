package roomescape.reservation.controller;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.service.TokenProvider;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationTimeRequest;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ThemeRequest;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;
import roomescape.util.ControllerTest;

@DisplayName("예약 API 통합 테스트")
class ReservationControllerTest extends ControllerTest {
    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    MemberService memberService;

    @Autowired
    TokenProvider tokenProvider;

    String token;

    @BeforeEach
    void setUp() {
        memberService.create(
                new SignUpRequest(getMemberChoco().getName(), getMemberChoco().getEmail(), "1234"));
        token = tokenProvider.createAccessToken(getMemberChoco().getEmail());
    }

    @DisplayName("사용자 예약 생성 시 201을 반환한다.")
    @Test
    void create() {
        //given
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeRequest("12:00"));
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", "2099-08-05");
        reservation.put("timeId", reservationTimeResponse.id());
        reservation.put("themeId", themeResponse.id());

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예약을 삭제한다.")
    @TestFactory
    Stream<DynamicTest> delete() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeRequest("12:00"));
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        ReservationResponse reservationResponse = reservationService.createMemberReservation(
                getMemberChoco(),
                new ReservationRequest(
                        LocalDate.now().toString(),
                        reservationTimeResponse.id(),
                        themeResponse.id())
        );

        return Stream.of(
                dynamicTest("타인의 예약 삭제 시, 403을 반환한다.", () -> {
                    //given
                    memberService.create(
                            new SignUpRequest(getMemberClover().getName(), getMemberClover().getEmail(), "qwer"));

                    String cloverToken = tokenProvider.createAccessToken(getMemberClover().getEmail());

                    RestAssured.given().log().all()
                            .cookie("token", cloverToken)
                            .when().delete("/reservations/" + reservationResponse.memberReservationId())
                            .then().log().all()
                            .statusCode(403);
                }),
                dynamicTest("예약 삭제 시 204를 반환한다.", () -> {
                    //given
                    RestAssured.given().log().all()
                            .cookie("token", token)
                            .when().delete("/reservations/" + reservationResponse.memberReservationId())
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("예약 조회 시 200을 반환한다.")
    @Test
    void find() {
        //given & when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예약 생성 시, 잘못된 날짜 형식에 대해 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "20-12-31", "2020-1-30", "2020-11-0", "-1"})
    void createBadRequest(String date) {
        //given
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeRequest("12:00"));
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", date);
        reservation.put("timeId", reservationTimeResponse.id());
        reservation.put("themeId", themeResponse.id());

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약 생성 시, 400을 반환한다.")
    @Test
    void createReservationAfterNow() {
        //given
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeRequest("12:00"));
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.now().minusDays(2).toString());
        reservation.put("timeId", reservationTimeResponse.id());
        reservation.put("themeId", themeResponse.id());

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
