package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.WaitRepository;

public class WaitServiceTest {
    private WaitService waitService;
    private WaitRepository waitRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private LocalDate reservationDate;

    @BeforeEach
    void beforeEach() {
        waitRepository = Mockito.mock(WaitRepository.class);
        waitService = new WaitService(waitRepository);

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
        reservationDate = LocalDate.of(2026, 5, 2);
    }

    @Test
    void saveTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate, reservationTime,
                theme);
        Wait wait = Wait.of(1L, waitWithoutId);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(new Waits(List.of()));
        when(waitRepository.save(waitWithoutId)).thenReturn(wait);

        assertThat(waitService.save(waitWithoutId)).isEqualTo(wait);
    }

    @Test
    void saveDuplicatedExceptionTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait = Wait.of(1L, waitWithoutId);

        Wait otherWait = new Wait(2L, LocalDateTime.of(2026, 5, 2, 11, 0), "luke", reservationDate,
                reservationTime, theme);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(new Waits(List.of(wait, otherWait)));

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void saveSizeFullExceptionTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 14, 0), "fizz", reservationDate,
                reservationTime, theme);

        Wait otherWait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 11, 0), "luke", reservationDate,
                reservationTime, theme);
        Wait otherWait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 12, 0), "neo", reservationDate,
                reservationTime, theme);
        Wait otherWait3 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 13, 0), "lucky", reservationDate,
                reservationTime, theme);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(new Waits(List.of(otherWait1, otherWait2, otherWait3)));

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void findByNameTest() {
        LocalDate otherReservationDate = LocalDate.of(2026, 5, 3);

        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), "fizz", otherReservationDate,
                reservationTime, theme);

        List<Wait> fizzWaits = List.of(wait1, wait2);
        when(waitRepository.findByName("fizz")).thenReturn(fizzWaits);

        assertThat(waitService.findByName("fizz")).isEqualTo(fizzWaits);
    }

    @Test
    void findAllTest() {
        LocalDate otherReservationDate = LocalDate.of(2026, 5, 3);

        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), "luke", otherReservationDate,
                reservationTime, theme);

        List<Wait> waits = List.of(wait1, wait2);
        when(waitRepository.findAll()).thenReturn(waits);

        assertThat(waitService.findAll()).isEqualTo(waits);
    }

    @Test
    void deleteTest() {
        waitRepository.delete(1L);

        verify(waitRepository, times(1)).delete(1L);
    }

    @Test
    void findBySlotTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 12, 0), "luke", reservationDate,
                reservationTime, theme);

        Waits waits = new Waits(List.of(wait1, wait2));
        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);

        assertThat(waitService.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).isEqualTo(waits);
    }

    @Test
    void findWaitTest() {
        Wait wait = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate, reservationTime, theme);
        when(waitRepository.findById(1L)).thenReturn(Optional.of(wait));

        assertThat(waitService.findWait(1L)).isEqualTo(wait);
    }

    @Test
    void findWaitExceptionTest() {
        when(waitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitService.findWait(1L))
                .isInstanceOf(RoomEscapeException.class);
    }
}
