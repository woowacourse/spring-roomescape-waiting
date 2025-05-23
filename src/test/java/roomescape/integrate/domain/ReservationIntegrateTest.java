package roomescape.integrate.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.entity.ConfirmedReservation;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.WaitingReservation;
import roomescape.global.ReservationStatus;
import roomescape.global.Role;
import roomescape.jwt.JwtTokenProvider;
import roomescape.repository.MemberRepository;
import roomescape.repository.ConfirmReservationRepository;
import roomescape.repository.WaitingReservationRepository;
import roomescape.service.AuthService;
import roomescape.service.ConfirmReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitingReservationService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationIntegrateTest {

    @Autowired
    ConfirmReservationService confirmReservationService;

    @Autowired
    WaitingReservationService waitingReservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ConfirmReservationRepository confirmReservationRepository;

    @Autowired
    WaitingReservationRepository waitingReservationRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member("어드민123", "test_admin@test.com", "test", Role.ADMIN));
        token = jwtTokenProvider.createTokenByMember(member);
        Member otherMember = memberRepository.save(new Member("test123", "test123@test.com", "test", Role.USER));
        otherToken = jwtTokenProvider.createTokenByMember(otherMember);

    }

    @Test
    void 예약_추가_테스트() {
        // given
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest reservationTimeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(reservationTimeRequest);

        CreateThemeRequest themeRequest = new CreateThemeRequest("테마", "설명", "썸네일");
        Theme theme = themeService.addTheme(themeRequest);

        Map<String, Object> reservationParam = Map.of(
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", reservationTime.getId(),
                "themeId", theme.getId()
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservationParam)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_삭제_테스트() {
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
        Reservation reservation = confirmReservationService.addReservation(reservationRequest, loginMemberRequest);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 테마_랭킹_테스트() {
        // given
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest reservationTimeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(reservationTimeRequest);

        CreateThemeRequest themeRequest_1 = new CreateThemeRequest("테마 1", "설명", "썸네일");
        Theme theme_1 = themeService.addTheme(themeRequest_1);
        CreateThemeRequest themeRequest_2 = new CreateThemeRequest("테마 2", "설명", "썸네일");
        Theme theme_2 = themeService.addTheme(themeRequest_2);
        CreateThemeRequest themeRequest_3 = new CreateThemeRequest("테마 3", "설명", "썸네일");
        Theme theme_3 = themeService.addTheme(themeRequest_3);

        Member member = memberRepository.save(new Member("테스트", "test_user@test.com", "test", Role.USER));

        ConfirmedReservation reservation1 = new ConfirmedReservation(member, LocalDate.now().minusDays(1),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation2 = new ConfirmedReservation(member, LocalDate.now().minusDays(2),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation3 = new ConfirmedReservation(member, LocalDate.now().minusDays(3),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation4 = new ConfirmedReservation(member, LocalDate.now().minusDays(4),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail"));
        ConfirmedReservation reservation5 = new ConfirmedReservation(member, LocalDate.now().minusDays(5),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail"));
        ConfirmedReservation reservation6 = new ConfirmedReservation(member, LocalDate.now().minusDays(6),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_3.getId(), "테마 명3", "description", "thumbnail"));

        confirmReservationRepository.save(reservation1);
        confirmReservationRepository.save(reservation2);
        confirmReservationRepository.save(reservation3);
        confirmReservationRepository.save(reservation4);
        confirmReservationRepository.save(reservation5);
        confirmReservationRepository.save(reservation6);

        // when
        Response response = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .extract().response();

        List<ThemeResponse> rankingThemes = response.jsonPath().getList("", ThemeResponse.class);
        List<Long> rankingThemeIds = rankingThemes.stream()
                .map(ThemeResponse::id)
                .toList();

        // then
        assertThat(rankingThemeIds).containsExactlyElementsOf(
                List.of(theme_1.getId(), theme_2.getId(), theme_3.getId()));
    }

    @Test
    void 대상_유저_예약조회_테스트() {

        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest reservationTimeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(reservationTimeRequest);

        CreateThemeRequest themeRequest_1 = new CreateThemeRequest("테마 1", "설명", "썸네일");
        Theme theme_1 = themeService.addTheme(themeRequest_1);
        CreateThemeRequest themeRequest_2 = new CreateThemeRequest("테마 2", "설명", "썸네일");
        Theme theme_2 = themeService.addTheme(themeRequest_2);
        CreateThemeRequest themeRequest_3 = new CreateThemeRequest("테마 3", "설명", "썸네일");
        Theme theme_3 = themeService.addTheme(themeRequest_3);

        Member member = memberRepository.save(new Member("테스트", "test_user@test.com", "test", Role.USER));
        Member member1 = memberRepository.save(new Member("테스트2", "test2_user@test.com", "test", Role.USER));

        ConfirmedReservation reservation1 = new ConfirmedReservation(member, LocalDate.now().minusDays(1),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation2 = new ConfirmedReservation(member, LocalDate.now().minusDays(2),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation3 = new ConfirmedReservation(member, LocalDate.now().minusDays(3),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail"));
        ConfirmedReservation reservation4 = new ConfirmedReservation(member, LocalDate.now().minusDays(4),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail"));
        ConfirmedReservation reservation5 = new ConfirmedReservation(member1, LocalDate.now().minusDays(5),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail"));
        ConfirmedReservation reservation6 = new ConfirmedReservation(member1, LocalDate.now().minusDays(6),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_3.getId(), "테마 명3", "description", "thumbnail"));

        confirmReservationRepository.save(reservation1);
        confirmReservationRepository.save(reservation2);
        confirmReservationRepository.save(reservation3);
        confirmReservationRepository.save(reservation4);
        confirmReservationRepository.save(reservation5);
        confirmReservationRepository.save(reservation6);

        String token = jwtTokenProvider.createTokenByMember(member);
        Response response = RestAssured.given().log().all()
                .when()
                .cookie("token", token)
                .get("/reservations/mine")
                .then().log().all()
                .extract().response();

        List<MyReservationResponse> responses = response.jsonPath().getList("", MyReservationResponse.class);
        assertThat(responses).hasSize(4);
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
        CreateWaitingRequest waitingRequest = new CreateWaitingRequest(
                tomorrow, reservationTime.getId(), theme.getId());

        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                tomorrow, reservationTime.getId(), theme.getId()
        );
        LoginMemberRequest loginMemberRequest = authService.getLoginMemberByToken(token);
        ConfirmedReservation confirm = confirmReservationService.addReservation(reservationRequest, loginMemberRequest);
        WaitingReservation waiting = waitingReservationService.addWaiting(waitingRequest, loginMemberRequest);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/waiting/" + waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 확정예약_삭제_시_대기예약_자동_승격_테스트() {
        // given
        LocalTime afterTime = LocalTime.now().plusHours(1L);
        CreateReservationTimeRequest timeRequest = new CreateReservationTimeRequest(afterTime);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(timeRequest);

        CreateThemeRequest themeRequest = new CreateThemeRequest("시간여행", "미래를 탈출하라", "thumb.png");
        Theme theme = themeService.addTheme(themeRequest);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // 사용자 1: 확정 예약
        LoginMemberRequest member1 = authService.getLoginMemberByToken(token);
        CreateReservationRequest reservationRequest = new CreateReservationRequest(
                tomorrow, reservationTime.getId(), theme.getId()
        );
        ConfirmedReservation confirmed = confirmReservationService.addReservation(reservationRequest, member1);

        // 사용자 2: 같은 시간, 테마에 대기 예약
        LoginMemberRequest member2 = authService.getLoginMemberByToken(otherToken);
        CreateWaitingRequest waitingRequest = new CreateWaitingRequest(
                tomorrow, reservationTime.getId(), theme.getId()
        );
        WaitingReservation waiting = waitingReservationService.addWaiting(waitingRequest, member2);

        // when: 확정 예약 삭제
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/" + confirmed.getId())
                .then().log().all()
                .statusCode(204);

        // then: 해당 시간에 기존 대기자가 confirm 되었는지 확인
        boolean replacedConfirm = confirmReservationRepository.existsByTimeIdAndThemeIdAndDate(
                waiting.getReservationTime().getId(),
                waiting.getTheme().getId(),
                waiting.getDate()
        );

        assertThat(replacedConfirm).isTrue();

        // 그리고 대기열에서는 사라졌는지 확인
        Optional<WaitingReservation> waitingCheck = waitingReservationRepository.findById(waiting.getId());
        assertThat(waitingCheck).isEmpty();
    }
}