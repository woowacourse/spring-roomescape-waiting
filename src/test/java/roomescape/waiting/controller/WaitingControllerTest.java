package roomescape.waiting.controller;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.jdbc.Sql;
import roomescape.admin.controller.AdminController;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.CookieProvider;
import roomescape.auth.provider.model.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.vo.Name;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql("/init.sql")
class WaitingControllerTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    WaitingController waitingController;

    @Autowired
    private TokenProvider tokenProvider;


    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 예약대기_정보를_잘_불러오는지_확인한다() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("14:00")));
        Theme theme = themeRepository.save(new Theme(new Name("공포 테마"), "설명", "썸네일"));
        Member member = memberRepository.save(new Member(new Name("레몬"), "lemone@naver.com", "abc123", MemberRole.ADMIN));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(2), reservationTime, theme, member));
        WaitingRequest waitingRequest = new WaitingRequest(reservation.getDate(), reservationTime.getId(),
                theme.getId());
        Member waitingMember = memberRepository.save(
                new Member(new Name("사과"), "apple@naver.com", "apple123", MemberRole.ADMIN));
        waitingService.addWaiting(waitingRequest, waitingMember.getId());

        Token token = tokenProvider.getAccessToken(member.getId());
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);
        RestAssured.given().log().all()
                .cookie(cookie.toString())
                .when().get("/waitings")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(Long.valueOf(member.getId()).intValue()))
                .body("[0].memberName", equalTo(waitingMember.getName()))
                .body("[0].themeName", equalTo(theme.getName()))
                .body("[0].date", equalTo(waitingRequest.date().toString()))
                .body("[0].startAt", equalTo(reservationTime.getStartAt().toString()));
    }

    @Test
    void 예약대기_신청을_취소한다() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("14:00")));
        Theme theme = themeRepository.save(new Theme(new Name("공포 테마"), "설명", "썸네일"));
        Member member = memberRepository.save(new Member(new Name("레몬"), "lemone@naver.com", "abc123", MemberRole.ADMIN));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(2), reservationTime, theme, member));
        WaitingRequest waitingRequest = new WaitingRequest(reservation.getDate(), reservationTime.getId(),
                theme.getId());
        Member waitingMember = memberRepository.save(
                new Member(new Name("사과"), "apple@naver.com", "apple123", MemberRole.ADMIN));
        WaitingResponse waitingResponse = waitingService.addWaiting(waitingRequest, waitingMember.getId());

        Token token = tokenProvider.getAccessToken(member.getId());
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);
        RestAssured.given().log().all()
                .cookie(cookie.toString())
                .when().delete("/waitings/" + waitingResponse.id())
                .then().statusCode(204);
    }
}
