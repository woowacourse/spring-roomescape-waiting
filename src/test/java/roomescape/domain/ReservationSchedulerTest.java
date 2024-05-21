package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.domain.repository.MemberCommandRepository;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
@Transactional
class ReservationSchedulerTest {

    @Autowired
    private ReservationScheduler reservationScheduler;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private WaitingCommandRepository waitingCommandRepository;

    @Autowired
    private WaitingQueryRepository waitingQueryRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @Autowired
    private TimeQueryRepository timeQueryRepository;

    @Autowired
    private ThemeQueryRepository themeQueryRepository;

    @DisplayName("과거의 예약을 취소하려하면 예외가 발생한다.")
    @Test
    void cancelExceptionTest() {
        Member member = memberQueryRepository.getById(1L);
        Time time = timeQueryRepository.getById(1L);
        Theme theme = themeQueryRepository.getById(1L);
        Reservation reservation = reservationCommandRepository.save(
                new Reservation(member, LocalDate.of(1999, 12, 25), time, theme));

        assertThatCode(() -> reservationScheduler.cancel(reservation.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.DATE_EXPIRED);
    }
    
    @DisplayName("예약 취소시 예약 대기가 존재하면 가장 빠른 예약 대기가 예약된다.")
    @Test
    void cancelAndReserveWaitingTest() {
        Member member = memberCommandRepository.save(
                new Member(new PlayerName("test"), new Email("test@test.com"), new Password("testTest1!"), Role.BASIC));
        Time time = timeQueryRepository.getById(1L);
        Theme theme = themeQueryRepository.getById(1L);
        LocalDate reservationDate = LocalDate.now().plusDays(10);

        Reservation reservation = reservationCommandRepository.save(new Reservation(member, reservationDate, time, theme));
        Waiting waiting = waitingCommandRepository.save(new Waiting(member, reservationDate, time, theme));

        reservationScheduler.cancel(reservation.getId());
        Reservation replacedWaiting = reservationQueryRepository.findByDateAndTimeAndTheme(reservationDate, time, theme)
                .orElseThrow();

        assertThat(waitingQueryRepository.findById(waiting.getId())).isEmpty();
        assertThat(replacedWaiting.getMember()).isEqualTo(waiting.getMember());
        assertThat(replacedWaiting.getDate()).isEqualTo(waiting.getDate());
        assertThat(replacedWaiting.getTime()).isEqualTo(waiting.getTime());
        assertThat(replacedWaiting.getTheme()).isEqualTo(waiting.getTheme());
    }
}
