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
import roomescape.reservation.dto.request.ReservationRequest.ReservationCreateRequest;
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
        Member member = new Member(0L, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
        Theme theme = new Theme(0L, "테마", "설명", "썸네일");
        themeRepository.save(theme);
        ReservationTime reservationTime = new ReservationTime(0L, LocalTime.of(10, 0));
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
                .body("time.id", equalTo(1))
                .body("time.startAt", equalTo("10:00:00"))
                .body("theme.id", equalTo(1))
                .body("theme.name", equalTo("테마"))
                .body("theme.description", equalTo("설명"))
                .body("theme.thumbnail", equalTo("썸네일"));
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
                .body("[0].time.id", equalTo(1))
                .body("[0].time.startAt", equalTo("10:00:00"))
                .body("[0].member.id", equalTo(1))
                .body("[0].member.name", equalTo(DEFAULT_NAME))
                .body("[0].member.email", equalTo(DEFAULT_EMAIL))
                .body("[0].theme.id", equalTo(1))
                .body("[0].theme.name", equalTo("테마"))
                .body("[0].theme.description", equalTo("설명"))
                .body("[0].theme.thumbnail", equalTo("썸네일"));
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
    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    void deleteNonExistentReservation() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.deleteWithToken("/reservations/1", token)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("존재하지 않는 id 입니다."));
    }
}
