package roomescape.reservation.controller;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.controller.dto.MemberResponse;
import roomescape.auth.service.AuthService;
import roomescape.auth.service.TokenProvider;
import roomescape.auth.service.dto.SignUpCommand;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.reservation.service.MemberReservationService;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.ThemeService;
import roomescape.reservation.service.dto.MemberReservationCreate;
import roomescape.reservation.service.dto.ReservationTimeCreate;
import roomescape.reservation.service.dto.ThemeCreate;
import roomescape.util.ControllerTest;
import roomescape.waiting.service.WaitingReservationService;

@DisplayName("예약 API 통합 테스트")
class ReservationControllerTest extends ControllerTest {
    @Autowired
    WaitingReservationService waitingReservationService;
    @Autowired
    MemberReservationService memberReservationService;
    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    ThemeService themeService;
    @Autowired
    MemberService memberService;
    @Autowired
    AuthService authService;

    String token;

    @BeforeEach
    void setUp() {
        authService.signUp(new SignUpCommand(
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
                new ReservationTimeCreate(LocalTime.NOON));
        ThemeResponse themeResponse = themeService.create(new ThemeCreate("name", "description", "thumbnail"));

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
                new ReservationTimeCreate(LocalTime.NOON));
        ThemeResponse themeResponse = themeService.create(new ThemeCreate("name", "description", "thumbnail"));
        MemberResponse memberResponseOf = memberService.findAll()
                .stream()
                .filter(memberResponse -> Objects.equals(memberResponse.name(), getMemberChoco().getName()))
                .findAny().orElseThrow();

        Member member = memberService.findById(memberResponseOf.id());
        ReservationResponse reservationResponse = memberReservationService.createMemberReservation(
                new MemberReservationCreate(
                        member.getId(),
                        LocalDate.now().plusDays(10),
                        reservationTimeResponse.id(),
                        themeResponse.id()
                )
        );

        return Stream.of(
                dynamicTest("타인의 예약 삭제 시, 403을 반환한다.", () -> {
                    //given
                    authService.signUp(
                            new SignUpCommand(getMemberClover().getName(), getMemberClover().getEmail(),
                                    getMemberClover().getPassword()));

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

    @DisplayName("지나간 날짜와 시간에 대한 예약 생성 시, 400을 반환한다.")
    @Test
    void createReservationAfterNow() {
        //given
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeCreate(LocalTime.NOON));
        ThemeResponse themeResponse = themeService.create(new ThemeCreate("name", "description", "thumbnail"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().minusDays(2).toString());
        params.put("timeId", reservationTimeResponse.id());
        params.put("themeId", themeResponse.id());

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
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

    @DisplayName("예약 대기를 등록하고 삭제한다.")
    @TestFactory
    Stream<DynamicTest> waitingAndDelete() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(
                new ReservationTimeCreate(LocalTime.NOON));
        ThemeResponse themeResponse = themeService.create(new ThemeCreate("name", "description", "thumbnail"));
        MemberResponse memberResponseOf = memberService.findAll()
                .stream()
                .filter(memberResponse -> Objects.equals(memberResponse.name(), getMemberChoco().getName()))
                .findAny().orElseThrow();

        Member member = memberService.findById(memberResponseOf.id());
        ReservationResponse reservationResponse = memberReservationService.createMemberReservation(
                new MemberReservationCreate(
                        member.getId(),
                        LocalDate.now().plusDays(10),
                        reservationTimeResponse.id(),
                        themeResponse.id()
                )
        );
        String cloverToken = tokenProvider.createAccessToken(getMemberClover().getEmail());

        List<ReservationResponse> waitingResponses = new ArrayList<>();
        return Stream.of(
                dynamicTest("예약 대기 시, 201을 반환한다.", () -> {
                    //given
                    authService.signUp(
                            new SignUpCommand(getMemberClover().getName(), getMemberClover().getEmail(),
                                    getMemberClover().getPassword()));

                    Map<String, Object> params = new HashMap<>();
                    params.put("date", reservationResponse.date().toString());
                    params.put("timeId", reservationResponse.time().id());
                    params.put("themeId", reservationResponse.theme().id());

                    // when & then
                    ReservationResponse waitingResponse = RestAssured.given().log().all()
                            .cookie("token", cloverToken)
                            .contentType(ContentType.JSON)
                            .body(params)
                            .when().post("/reservations/waiting")
                            .then().log().all()
                            .statusCode(201).extract().as(ReservationResponse.class);

                    waitingResponses.add(waitingResponse);
                }),
                dynamicTest("예약 대기 삭제 시 204를 반환한다.", () -> {
                    //given & when & then
                    RestAssured.given().log().all()
                            .cookie("token", cloverToken)
                            .when()
                            .delete("/reservations/" + waitingResponses.get(0).memberReservationId() + "/waiting")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }
}
