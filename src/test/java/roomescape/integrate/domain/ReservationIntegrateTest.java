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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.dto.request.CreateThemeRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
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

    String token;

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

        LoginMemberRequest loginMemberRequest = authService.getLoginMemberByToken(token);
        reservationService.addReservation(reservationRequest, loginMemberRequest);

        Reservation reservation = reservationRepository.findById(1L).get();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @Sql(scripts = "/ranking-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void 테마_랭킹_테스트() {
        // given
        List<Theme> themes = themeService.findAll();

        // when
        Response response = RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .extract().response();

        List<ThemeResponse> rankingThemes = response.jsonPath().getList("", ThemeResponse.class);
        List<Long> rankingThemeIds = rankingThemes.stream()
                .map(ThemeResponse::id)
                .toList();

        List<Long> order = themes.stream().map(Theme::getId).toList().reversed();

        // then
        assertThat(rankingThemeIds).containsExactlyElementsOf(order);
    }

    @Test
    @Sql(scripts = "/mine-reservation-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void 대상_유저_예약조회_테스트() {
        //given
        Member member = memberRepository.findById(100L).get();

        String token = jwtTokenProvider.createTokenByMember(member);
        //when
        Response response = RestAssured.given().log().all()
                .when()
                .cookie("token", token)
                .get("/reservations/mine")
                .then().log().all()
                .extract().response();

        List<MyReservationResponse> responses = response.jsonPath().getList("", MyReservationResponse.class);

        //then
        assertThat(responses).hasSize(4);
    }
}
