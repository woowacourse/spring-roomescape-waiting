package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.TokenProvider;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;
import roomescape.util.ControllerTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;

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

    @Autowired
    AuthService authService;

    String token;

    @BeforeEach
    void setUp() {
        authService.signUp(new SignUpRequest(
                getMemberChoco().getName(),
                getMemberChoco().getEmail(),
                getMemberChoco().getPassword()
        ));
        token = tokenProvider.createAccessToken(getMemberChoco().getEmail());
    }

    @DisplayName("사용자 예약 생성 시 201을 반환한다.")
    @Test
    void create() {
        //given
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeRequest("12:00"));
        ThemeResponse themeResponse = themeService.create(new ThemeRequest("name", "description", "thumbnail"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2099-08-05");
        params.put("timeId", reservationTimeResponse.id());
        params.put("themeId", themeResponse.id());

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
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
        MemberResponse memberResponseOf = memberService.findAll()
                .stream()
                .filter(memberResponse -> Objects.equals(memberResponse.name(), getMemberChoco().getName()))
                .findAny().orElseThrow();

        Member member = memberService.findById(memberResponseOf.id());
        ReservationResponse reservationResponse = reservationService.createMemberReservation(
                AuthInfo.of(member),
                new ReservationRequest(
                        LocalDate.now().plusDays(10).toString(),
                        reservationTimeResponse.id(),
                        themeResponse.id())
        );

        return Stream.of(
                dynamicTest("타인의 예약 삭제 시, 403을 반환한다.", () -> {
                    //given
                    memberService.create(
                            new SignUpRequest(getMemberClover().getName(), getMemberClover().getEmail(), getMemberClover().getPassword()));

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

    @DisplayName("내 예약 조회 페이지 조회 시 200을 반환한다.")
    @Test
    void getMyReservationPage() {
        //given

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(200);
    }
}
