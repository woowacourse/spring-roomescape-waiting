package roomescape.member.controller;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.admin.domain.dto.AdminReservationRequestDto;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.auth.fixture.AuthFixture;
import roomescape.global.auth.service.AuthService;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationTime.ReservationTimeTestDataConfig;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;
import roomescape.user.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes =
        {MemberTestDataConfig.class, ThemeTestDataConfig.class, ReservationTimeTestDataConfig.class,})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private UserService service;
    @Autowired
    private ReservationTimeTestDataConfig reservationTimeTestDataConfig;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private ReservationTimeTestDataConfig timeTestDataConfig;

    private static LocalDate date;
    private static User memberStatic;
    private static TokenResponseDto memberTokenResponseDto;

    @BeforeAll
    public static void setUp(@Autowired AuthService authService,
                             @Autowired MemberTestDataConfig memberTestDataConfig
    ) {
        date = LocalDate.now().plusDays(1);

        memberStatic = memberTestDataConfig.getSavedMember();

        memberTokenResponseDto = authService.login(
                AuthFixture.createTokenRequestDto(memberStatic.getEmail(), memberStatic.getPassword()));
    }

    @DisplayName("유저의 예약 리스트 조회 기능 : 성공 시 200 OK 반환")
    @Test
    void findAllReservationsByMember_success_byMember() {
        // given
        Long themeId = themeTestDataConfig.getSavedId();
        Long reservationTimeId = reservationTimeTestDataConfig.getSavedId();

        AdminReservationRequestDto dto = new AdminReservationRequestDto(date,
                themeId,
                reservationTimeId,
                memberStatic.getId());

        String token = memberTokenResponseDto.accessToken();

        // when
        // then
        RestAssured.given().log().all()
                .cookies("token", token)
                .contentType(ContentType.JSON)
                .body(dto)
                .when().get("/members/reservations-mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
