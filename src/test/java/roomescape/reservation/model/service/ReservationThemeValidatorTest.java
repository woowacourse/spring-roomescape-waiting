package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
import static roomescape.ReservationTestFixture.createTheme;
import static roomescape.ReservationTestFixture.getReservationThemeFixture;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;
import static roomescape.ReservationTestFixture.getUserFixture;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException.ReservationThemeInUseException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.RepositoryTestSupport;

@Import(ReservationThemeValidator.class)
class ReservationThemeValidatorTest extends RepositoryTestSupport {

    @Autowired
    private ReservationThemeValidator reservationThemeValidator;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTheme savedTheme;
    private Member savedMember;
    private ReservationTime savedTime;

    @BeforeEach
    void setUp() {
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        savedMember = memberRepository.save(getUserFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
    }

    @Test
    @DisplayName("활성 예약이 없는 테마는 검증을 통과한다")
    void validateNotActive_success() {
        // given
        ReservationTheme unusedTheme = reservationThemeRepository.save(
                createTheme("미사용 테마", "사용하지 않는 테마", "unused.com")
        );

        // when & then
        assertThatCode(() -> reservationThemeValidator.validateNotUsedInActive(unusedTheme.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("오늘 날짜의 예약이 있으면 예외가 발생한다")
    void validateNotUsedInActive_throws_exception_today() {
        // given
        LocalDate today = LocalDate.now();
        Reservation todayReservation = createConfirmedReservation(today, savedTime, savedTheme, savedMember);
        reservationRepository.save(todayReservation);

        // when & then
        assertThatThrownBy(() -> reservationThemeValidator.validateNotUsedInActive(savedTheme.getId()))
                .isInstanceOf(ReservationThemeInUseException.class)
                .hasMessage("해당 테마를 사용중인 예약이 존재합니다.");
    }

    @Test
    @DisplayName("미래 날짜의 예약이 있으면 예외가 발생한다")
    void validateNotUsedInActive_throws_exception_future() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        Reservation futureReservation = createConfirmedReservation(futureDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(futureReservation);

        // when & then
        assertThatThrownBy(() -> reservationThemeValidator.validateNotUsedInActive(savedTheme.getId()))
                .isInstanceOf(ReservationThemeInUseException.class)
                .hasMessage("해당 테마를 사용중인 예약이 존재합니다.");
    }

    @Test
    @DisplayName("과거 날짜의 예약만 있으면 검증을 통과한다")
    void validateNotUsedInActive_success_only_past() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(7);
        Reservation pastReservation = createConfirmedReservation(pastDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(pastReservation);

        // when & then
        assertThatCode(() -> reservationThemeValidator.validateNotUsedInActive(savedTheme.getId()))
                .doesNotThrowAnyException();
    }
}
