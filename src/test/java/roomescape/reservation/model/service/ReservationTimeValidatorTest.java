package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
import static roomescape.ReservationTestFixture.createTime;
import static roomescape.ReservationTestFixture.getReservationThemeFixture;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;
import static roomescape.ReservationTestFixture.getUserFixture;

import java.time.LocalDate;
import java.time.LocalTime;
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
import roomescape.reservation.model.exception.ReservationException.ReservationTimeInUseException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.RepositoryTestSupport;

@Import(ReservationTimeValidator.class)
class ReservationTimeValidatorTest extends RepositoryTestSupport {

    @Autowired
    private ReservationTimeValidator reservationTimeValidator;

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
    @DisplayName("활성 예약이 없는 예약 시간은 검증을 통과한다")
    void validateNotActive_success() {
        // given
        ReservationTime unusedTime = reservationTimeRepository.save(
                createTime(LocalTime.of(15, 30))
        );

        // when & then
        assertThatCode(() -> reservationTimeValidator.validateNotUsedInActive(unusedTime.getId()))
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
        assertThatThrownBy(() -> reservationTimeValidator.validateNotUsedInActive(savedTime.getId()))
                .isInstanceOf(ReservationTimeInUseException.class);
    }

    @Test
    @DisplayName("미래 날짜의 예약이 있으면 예외가 발생한다")
    void validateNotUsedInActive_throws_exception_future() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(7);
        Reservation futureReservation = createConfirmedReservation(futureDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(futureReservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeValidator.validateNotUsedInActive(savedTime.getId()))
                .isInstanceOf(ReservationTimeInUseException.class);
    }

    @Test
    @DisplayName("과거 날짜의 예약만 있으면 검증을 통과한다")
    void validateNotUsedInActive_success_only_past() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(7);
        Reservation pastReservation = createConfirmedReservation(pastDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(pastReservation);

        // when & then
        assertThatCode(() -> reservationTimeValidator.validateNotUsedInActive(savedTime.getId()))
                .doesNotThrowAnyException();
    }
}
