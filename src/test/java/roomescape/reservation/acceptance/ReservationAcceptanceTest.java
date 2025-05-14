package roomescape.reservation.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime);
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        // when & then
        TestHelper.postWithToken("/reservations", reservationRequest, token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("startAt", equalTo("10:00:00"))
                .body("themeName", equalTo("테마"));
    }

    @Test
    @DisplayName("중복된 예약을 생성할 수 없다.")
    void createDuplicateReservation() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        TestHelper.postWithToken("/reservations", reservationRequest, token);

        // when & then
        TestHelper.postWithToken("/reservations", reservationRequest, token)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("해당 날짜와 시간에 이미 예약이 존재합니다."));
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        TestHelper.postWithToken("/reservations", reservationRequest, token);

        // when & then
        TestHelper.get("/reservations")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("[0].startAt", equalTo("10:00:00"))
                .body("[0].memberName", equalTo(DEFAULT_NAME))
                .body("[0].themeName", equalTo("테마"));
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        TestHelper.postWithToken("/reservations", reservationRequest, token);

        // when & then
        TestHelper.deleteWithToken("/reservations/1", token)
                .then()
                .statusCode(HttpStatus.OK.value());

        TestHelper.get("/reservations")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("필터링된 예약을 조회할 수 없다.")
    void getFilteredReservations() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        TestHelper.postWithToken("/reservations", reservationRequest, token);
        String url = String.format("/reservations/filtered?themeId=%d&memberId=%d&dateFrom=%s&dateTo=%s",
                1L, 1L, LocalDate.now(), LocalDate.now().plusDays(7));

        // when & then
        TestHelper.getWithToken(url, token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("[0].startAt", equalTo("10:00:00"))
                .body("[0].memberName", equalTo(DEFAULT_NAME))
                .body("[0].themeName", equalTo("테마"));
    }

    @Test
    @DisplayName("유저 예약 기록을 확인한다.")
    void getReservationsByMember() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var reservationRequest = new ReservationCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        TestHelper.postWithToken("/reservations", reservationRequest, token);

        // when & then
        TestHelper.getWithToken("/reservations/mine", token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자는 필터링된 예약을 조회할 수 없다.")
    void getFilteredReservationsWithNonAdmin() {
        // given
        Member nonAdminMember = new Member("일반회원", "user@email.com", "password", RoleType.USER);
        memberRepository.save(nonAdminMember);
        String token = TestHelper.login("user@email.com", "password");
        String url = String.format("/reservations/filtered?themeId=%d&memberId=%d&dateFrom=%s&dateTo=%s",
                1L, 1L, LocalDate.now(), LocalDate.now().plusDays(7));

        // when & then
        TestHelper.getWithToken(url, token)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("관리자가 다른 회원의 예약을 생성한다.")
    void createReservationByAdmin() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Member userMember = new Member("일반회원", "user@email.com", "password", RoleType.USER);
        memberRepository.save(userMember);

        var adminCreateRequest = new ReservationAdminCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L,
                userMember.getId()
        );

        // when & then
        TestHelper.postWithToken("/admin/reservations", adminCreateRequest, token)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("date", equalTo(LocalDate.now().plusDays(1).toString()))
                .body("startAt", equalTo("10:00:00"))
                .body("themeName", equalTo("테마"));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자는 다른 회원의 예약을 생성할 수 없다.")
    void createReservationByNonAdmin() {
        // given
        Member nonAdminMember = new Member("일반회원", "user@email.com", "password", RoleType.USER);
        memberRepository.save(nonAdminMember);
        String token = TestHelper.login("user@email.com", "password");

        var adminCreateRequest = new ReservationAdminCreateRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L,
                1L
        );

        // when & then
        TestHelper.postWithToken("/admin/reservations", adminCreateRequest, token)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
