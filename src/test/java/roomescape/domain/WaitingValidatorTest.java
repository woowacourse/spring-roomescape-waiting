package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createTimeFrom;
import static roomescape.TestFixture.createWaiting;
import static roomescape.TestFixture.fixedClockAt;

import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.UnAvailableReservationException;

@ExtendWith(MockitoExtension.class)
class WaitingValidatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("미래 날짜와 시간에 대한 대기는 가능하다")
    void validateNotPast_success() {
        // given
        Clock clock = fixedClockAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        WaitingValidator waitingValidator = new WaitingValidator(clock, reservationRepository, waitingRepository);

        LocalDateTime futureDateTime = LocalDateTime.of(2025, 1, 2, 0, 0);
        Waiting waiting = createWaiting(
                createDefaultMember(),
                futureDateTime.toLocalDate(),
                createTimeFrom(futureDateTime.toLocalTime()),
                createDefaultTheme()
        );

        // when & then
        assertDoesNotThrow(() -> waitingValidator.validateCanWaiting(waiting));
    }

    @Test
    @DisplayName("과거 날짜와 시간에 대한 대기는 불가능하다")
    void validateNotPast_failure() {
        // given
        Clock clock = fixedClockAt(LocalDateTime.of(2025, 1, 2, 0, 0));
        WaitingValidator waitingValidator = new WaitingValidator(clock, reservationRepository, waitingRepository);

        LocalDateTime pastDateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Waiting waiting = createWaiting(
                createDefaultMember(),
                pastDateTime.toLocalDate(),
                createTimeFrom(pastDateTime.toLocalTime()),
                createDefaultTheme()
        );

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateCanWaiting(waiting))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("지난 날짜와 시간에 대한 대기는 불가능합니다.");
    }

    @Test
    @DisplayName("이미 동일한 시간에 대기가 존재하면 예외가 발생한다")
    void validateAlreadyWaiting_failure() {
        // given
        WaitingValidator waitingValidator = new WaitingValidator(fixedClockAt(LocalDateTime.of(2025, 1, 1, 0, 0)), reservationRepository, waitingRepository);

        Member member = createDefaultMember();
        Theme theme = createDefaultTheme();
        ReservationTime time = createDefaultReservationTime();
        Waiting waiting = createWaiting(member, DEFAULT_DATE, time, theme);

        when(reservationRepository.hasAlreadyReserved(member.getId(), theme.getId(), time.getId(), DEFAULT_DATE))
                .thenReturn(false);
        when(waitingRepository.hasAlreadyWaited(member.getId(), theme.getId(), time.getId(), DEFAULT_DATE))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateCanWaiting(waiting))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("이미 동일한 시간에 대기가 존재합니다.");
    }

    @Test
    @DisplayName("이미 동일한 시간에 예약이 존재하면 예외가 발생한다")
    void validateAlreadyReserved_failure() {
        // given
        WaitingValidator waitingValidator = new WaitingValidator(fixedClockAt(LocalDateTime.of(2025, 1, 1, 0, 0)), reservationRepository, waitingRepository);

        Member member = createDefaultMember();
        Theme theme = createDefaultTheme();
        ReservationTime time = createDefaultReservationTime();
        Waiting waiting = createWaiting(member, DEFAULT_DATE, time, theme);

        when(reservationRepository.hasAlreadyReserved(member.getId(), theme.getId(), time.getId(), DEFAULT_DATE))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateCanWaiting(waiting))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("이미 동일한 시간에 대기가 존재합니다.");
    }
}
