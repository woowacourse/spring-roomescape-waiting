package roomescape.integrate.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.dto.request.CreateThemeRequest;
import roomescape.dto.request.CreateWaitReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.request.SignupRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.Role;
import roomescape.jwt.JwtTokenProvider;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.AuthService;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ReservationWaitIntegrateTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MemberService memberService;

    String token;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member("어드민", "test_admin@test.com", "test", Role.ADMIN));
        token = jwtTokenProvider.createTokenByMember(member);
    }

    @Test
    void 예약_대기_추가_테스트() {
        // given
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest reservationTimeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(reservationTimeRequest);

        CreateThemeRequest themeRequest = new CreateThemeRequest("테마", "설명", "썸네일");
        Theme theme = themeService.addTheme(themeRequest);

        CreateWaitReservationRequest request = new CreateWaitReservationRequest(LocalDate.now().plusDays(1),
                reservationTime.getId(), theme.getId());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_대기_삭제_테스트() {
        // given
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest reservationTimeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(reservationTimeRequest);

        CreateThemeRequest themeRequest = new CreateThemeRequest("테마", "설명", "썸네일");
        Theme theme = themeService.addTheme(themeRequest);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                tomorrow, reservationTime.getId(), theme.getId());
        LoginMemberRequest loginMemberRequest = authService.getLoginMemberByToken(token);
        reservationService.addReservation(reservationRequest, loginMemberRequest);

        CreateWaitReservationRequest waitRequest = new CreateWaitReservationRequest(tomorrow,
                reservationTime.getId(), theme.getId());

        Member member = memberService.addMember(new SignupRequest("test", "wait@wait.com", "wait"));
        jwtTokenProvider.createTokenByMember(member);

        LoginMemberRequest waitMemberRequest = authService.getLoginMemberByToken(token);
        reservationService.addWaitReservation(waitRequest, waitMemberRequest);

        Reservation reservation = reservationRepository.findById(2L).get();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/waiting/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }
}
