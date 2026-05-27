package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.PastTimeException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingService = new WaitingService(waitingRepository, reservationRepository, timeSlotRepository);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 삭제한다.")
    void deleteWaiting() {
        Waiting waiting = createWaitingEntity();
        given(waitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatCode(() -> waitingService.removeWaiting(1L, "브라운")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 삭제하면, 예외가 발생한다.")
    void deleteNotExistsWaiting() {
        given(waitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.removeWaiting(1L, "브라운"))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 중복해서 저장하면, 예외가 발생한다.")
    void saveDuplicateWaiting() {
        Waiting waiting = createTransientWaiting();
        given(waitingRepository.isExists(waiting)).willReturn(true);

        assertThatThrownBy(() -> waitingService.saveWaiting(waiting))
                .isInstanceOf(DuplicateWaitingException.class);
    }

    @Test
    @DisplayName("존재하는 예약에 대기를 추가하면, 예외가 발생한다.")
    void saveReservedWaiting() {
        Waiting waiting = createTransientWaiting();
        given(reservationRepository.findByDateAndTimeIdAndThemeId(waiting.getDate(),
                waiting.getTimeSlotId(), waiting.getThemeId())).willReturn(Optional.of(
                new Reservation(1L, "브라운", LocalDate.now(), new TimeSlot(1L, LocalTime.now()),
                        new Theme(1L, "null", "null", "null"))));

        assertThatThrownBy(() -> waitingService.saveWaiting(waiting))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("이미 지난 시간으로 대기를 추가하면, 예외가 발생한다.")
    void savePassedWaiting() {
        Waiting waiting = createTransientWaiting();
        given(timeSlotRepository.findById(1L)).willReturn(Optional.of(new TimeSlot(1L, LocalTime.of(0, 0))));

        assertThatThrownBy(() -> waitingService.saveWaiting(waiting))
                .isInstanceOf(PastTimeException.class);
    }

    private Waiting createWaitingEntity() {
        return new Waiting(1L, "브라운", LocalDate.now(), 1L, 1L, 1);
    }

    private Waiting createTransientWaiting() {
        return Waiting.transientOf("브라운", LocalDate.now(), 1L, 1L);
    }
}
