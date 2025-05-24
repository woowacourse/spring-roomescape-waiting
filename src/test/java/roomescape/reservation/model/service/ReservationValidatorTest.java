package roomescape.reservation.model.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.ReservationTestFixture.createCanceledReservation;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
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
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.exception.ReservationException.ReservationNotFoundException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.vo.Schedule;
import roomescape.support.RepositoryTestSupport;

@Import(ReservationValidator.class)
class ReservationValidatorTest extends RepositoryTestSupport {

    @Autowired
    private ReservationValidator reservationValidator;

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
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        savedTheme = reservationThemeRepository.save(getReservationThemeFixture());
        savedMember = memberRepository.save(getUserFixture());
        savedTime = reservationTimeRepository.save(getReservationTimeFixture());
        testDate = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("중복되지 않은 스케줄은 중복 검증을 통과한다")
    void validateNoDuplication_success() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        // when & then
        assertThatCode(() -> reservationValidator.validateNoDuplication(schedule))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중복된 스케줄이 있으면 예외가 발생한다")
    void validateNoDuplication_throws_exception() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation existingReservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(existingReservation);

        // when & then
        assertThatThrownBy(() -> reservationValidator.validateNoDuplication(schedule))
                .isInstanceOf(InvalidReservationTimeException.class)
                .hasMessage("이미 예약된 시간입니다. 다른 시간을 예약해주세요.");
    }

    @Test
    @DisplayName("취소된 예약이 있는 스케줄은 중복 검증을 통과한다")
    void validateNoDuplication_success_only_cancelled() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation cancelledReservation = createCanceledReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(cancelledReservation);

        // when & then
        assertThatCode(() -> reservationValidator.validateNoDuplication(schedule))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약이 존재하는 스케줄은 존재 검증을 통과한다")
    void validateExistenceBySchedule_success() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation existingReservation = createConfirmedReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(existingReservation);

        // when & then
        assertThatCode(() -> reservationValidator.validateExistenceBySchedule(schedule))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약이 존재하지 않는 스케줄은 존재 검증에서 예외가 발생한다")
    void validateExistenceBySchedule_throws_exception() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        // when & then
        assertThatThrownBy(() -> reservationValidator.validateExistenceBySchedule(schedule))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("취소된 예약만 있는 스케줄은 존재 검증에서 예외가 발생한다")
    void validateExistenceBySchedule_throws_exception_only_canceled() {
        // given
        Schedule schedule = new Schedule(testDate, savedTime.getId(), savedTheme.getId());

        Reservation cancelledReservation = createCanceledReservation(testDate, savedTime, savedTheme, savedMember);
        reservationRepository.save(cancelledReservation);

        // when & then
        assertThatThrownBy(() -> reservationValidator.validateExistenceBySchedule(schedule))
                .isInstanceOf(ReservationNotFoundException.class);
    }
}
