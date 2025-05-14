package roomescape.integrate.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.entity.LoginMember;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.ReservationStatus;
import roomescape.global.Role;
import roomescape.jwt.JwtTokenProvider;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.AuthService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationIntegrateTest {

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

    private String token;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member("어드민", "test_admin@test.com", "test", Role.ADMIN));
        token = jwtTokenProvider.createTokenByMember(member);
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

        LoginMember loginMember = authService.getLoginMemberByToken(token);
        Reservation reservation = reservationService.addReservation(reservationRequest, loginMember);

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

        Reservation reservation1 = new Reservation(member, LocalDate.now().minusDays(1),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(member, LocalDate.now().minusDays(2),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation3 = new Reservation(member, LocalDate.now().minusDays(3),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation4 = new Reservation(member, LocalDate.now().minusDays(4),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation5 = new Reservation(member, LocalDate.now().minusDays(5),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation6 = new Reservation(member, LocalDate.now().minusDays(6),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_3.getId(), "테마 명3", "description", "thumbnail")
                , ReservationStatus.RESERVED);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        reservationRepository.save(reservation5);
        reservationRepository.save(reservation6);

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
    void 대상_유저_예약조회_테스트(){

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

        Reservation reservation1 = new Reservation(member, LocalDate.now().minusDays(1),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation2 = new Reservation(member, LocalDate.now().minusDays(2),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation3 = new Reservation(member, LocalDate.now().minusDays(3),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_1.getId(), "테마 명1", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation4 = new Reservation(member, LocalDate.now().minusDays(4),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation5 = new Reservation(member1, LocalDate.now().minusDays(5),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_2.getId(), "테마 명2", "description", "thumbnail")
                , ReservationStatus.RESERVED);
        Reservation reservation6 = new Reservation(member1, LocalDate.now().minusDays(6),
                new ReservationTime(reservationTime.getId(), afterTime),
                new Theme(theme_3.getId(), "테마 명3", "description", "thumbnail")
                , ReservationStatus.RESERVED);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        reservationRepository.save(reservation5);
        reservationRepository.save(reservation6);

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
}
