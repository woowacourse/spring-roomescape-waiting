package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.BookedReservationTimeResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;

@ActiveProfiles("test")
@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, MemberDao.class,
        ReservationService.class})
class ReservationServiceTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("모든 예약 정보를 가져온다")
    @Test
    void test1() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(time));
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        LocalDate date = LocalDate.of(2024, 4, 29);
        reservationDao.save(Reservation.withoutId(savedMember, date, savedTime, savedTheme));

        // when
        List<ReservationResponse> response = reservationService.getAll();

        // then
        assertThat(response).hasSize(1);
    }

    @DisplayName("예약 정보가 없다면 빈 리스트를 반환한다.")
    @Test
    void test2() {
        List<ReservationResponse> result = reservationService.getAll();

        assertThat(result).isEmpty();
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void test3() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(time));
        Long timeId = savedTime.getId();
        Member savedMember = memberDao.save(new Member("포스티", "test@test.com", "12341234", Role.MEMBER));

        LocalDate date = nextDay();

        ReservationRequest requestDto = new ReservationRequest(date, timeId, themeId);

        // when
        ReservationResponse result = reservationService.create(requestDto, savedMember);

        // then
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(result.member().name()).isEqualTo("포스티");
        softAssertions.assertThat(result.date()).isEqualTo(date);
        softAssertions.assertThat(result.time()).isEqualTo(new ReservationTimeResponse(timeId, time));
        softAssertions.assertThat(result.theme())
                .isEqualTo(new ThemeResponse(themeId, savedTheme.getName(), savedTheme.getDescription(),
                        savedTheme.getThumbnail()));

        softAssertions.assertAll();
    }

    @DisplayName("이미 존재하는 예약과 동일하면 예외가 발생한다.")
    @Test
    void test4() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(time));
        Long timeId = savedTime.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);
        LocalDate date = nextDay();

        ReservationRequest requestDto = new ReservationRequest(date, timeId, themeId);
        reservationService.create(requestDto, savedMember);

        // when & then
        assertThatThrownBy(() -> reservationService.create(requestDto, savedMember))
                .isInstanceOf(AlreadyInUseException.class);
    }

    @DisplayName("과거 날짜에 예약을 추가하면 예외가 발생한다.")
    @Test
    void test5() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        LocalDate date = now.toLocalDate();
        LocalTime pastTime = now.toLocalTime().minusMinutes(1);

        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(pastTime));
        Long timeId = savedTime.getId();

        ReservationRequest requestDto = new ReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.create(requestDto, savedMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 예약 시간 ID로 저장하면 예외를 반환한다.")
    @Test
    void test6() {
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        LocalDate date = nextDay();

        Long notExistId = 1000L;
        ReservationRequest requestDto = new ReservationRequest(date, notExistId, themeId);

        assertThatThrownBy(() -> reservationService.create(requestDto, savedMember))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("존재하지 않는 테마 ID로 저장하면 예외를 반환한다.")
    @Test
    void notExistThemeId() {
        LocalDate date = now.toLocalDate().plusDays(1);

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(time));
        Long timeId = savedTime.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        Long notExistId = 1000L;
        ReservationRequest requestDto = new ReservationRequest(date, timeId, notExistId);

        assertThatThrownBy(() -> reservationService.create(requestDto, savedMember))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void test7() {
        // given
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeDao.save(ReservationTime.withoutId(time));
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);

        LocalDate date = nextDay();
        Reservation savedReservation = reservationDao.save(
                Reservation.withoutId(savedMember, date, savedTime, savedTheme));

        Long id = savedReservation.getId();

        // then
        assertThatCode(() -> reservationService.delete(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약이 존재하지 않으면 예외를 반환한다.")
    @Test
    void test8() {
        Long id = 1L;
        assertThatThrownBy(() -> reservationService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("가능한 시간 대에 대하여 반환한다.")
    @Test
    void test9() {
        // given
        LocalDate date = nextDay();
        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(9, 0);
        ReservationTime reservationTime1 = reservationTimeDao.save(ReservationTime.withoutId(time1));
        ReservationTime reservationTime2 = reservationTimeDao.save(ReservationTime.withoutId(time2));
        Theme savedTheme = themeDao.save(Theme.withoutId("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberDao.save(member);
        reservationDao.save(Reservation.withoutId(savedMember, date, reservationTime1, savedTheme));

        // when
        List<BookedReservationTimeResponse> responses = reservationService.getAvailableTimes(date, themeId);

        // then
        List<Boolean> booleans = responses.stream()
                .map(BookedReservationTimeResponse::alreadyBooked)
                .toList();
        assertThat(booleans).containsExactlyInAnyOrder(true, false);
    }

    private LocalDate nextDay() {
        return LocalDate.now().plusDays(1);
    }
}
