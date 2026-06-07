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
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.WaitIsFullException;
import roomescape.exception.custom.WaitNotExistsException;
import roomescape.repository.WaitRepository;
import roomescape.repository.dto.WaitDetailDto;
import roomescape.service.dto.WaitInfo;

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
        List<WaitDetailDto> waits = List.of();
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate, reservationTime,
                theme);
        Wait wait = Wait.withId(1L, waitWithoutId);
        WaitInfo waitInfo = WaitInfo.of(wait, 1L);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);
        when(waitRepository.save(waitWithoutId)).thenReturn(wait);
        when(waitRepository.findOrderByWait(wait)).thenReturn(1L);

        assertThat(waitService.save(waitWithoutId)).isEqualTo(waitInfo);
    }

    @Test
    void saveDuplicatedExceptionTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 11, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 11, 0), "luke", reservationDate,
                reservationTime, theme);

        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);

        List<WaitDetailDto> waits = List.of(WaitDetailDto.of(wait1, 1L), WaitDetailDto.of(wait2, 2L));

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(AlreadyWaitingException.class);
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

        List<WaitDetailDto> waits = List.of(WaitDetailDto.of(otherWait1, 1L), WaitDetailDto.of(otherWait2, 2L),
                WaitDetailDto.of(otherWait3, 3L));

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(WaitIsFullException.class);
    }

    @Test
    void findByNameTest() {
        LocalDate otherReservationDate = LocalDate.of(2026, 5, 3);

        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), "fizz", otherReservationDate,
                reservationTime, theme);

        List<WaitDetailDto> fizzWaits = List.of(WaitDetailDto.of(wait1, 1L), WaitDetailDto.of(wait2, 1L));
        List<WaitInfo> fizzWaitInfos = List.of(WaitInfo.of(wait1, 1L), WaitInfo.of(wait2, 1L));

        when(waitRepository.findByName("fizz")).thenReturn(fizzWaits);
        when(waitRepository.findOrderByWait(wait1)).thenReturn(1L);
        when(waitRepository.findOrderByWait(wait2)).thenReturn(1L);

        assertThat(waitService.findByName("fizz")).isEqualTo(fizzWaitInfos);
    }

    @Test
    void findAllTest() {
        LocalDate otherReservationDate = LocalDate.of(2026, 5, 3);

        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), "luke", otherReservationDate,
                reservationTime, theme);

        List<WaitDetailDto> waits = List.of(WaitDetailDto.of(wait1, 1L), WaitDetailDto.of(wait2, 1L));
        List<WaitInfo> fizzWaitInfos = List.of(WaitInfo.of(wait1, 1L), WaitInfo.of(wait2, 1L));

        when(waitRepository.findAll()).thenReturn(waits);
        when(waitRepository.findOrderByWait(wait1)).thenReturn(1L);
        when(waitRepository.findOrderByWait(wait2)).thenReturn(1L);

        assertThat(waitService.findAll()).isEqualTo(fizzWaitInfos);
    }

    @Test
    void deleteTest() {
        waitRepository.deleteById(1L);

        verify(waitRepository, times(1)).deleteById(1L);
    }

    @Test
    void findBySlotTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate,
                reservationTime, theme);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 12, 0), "luke", reservationDate,
                reservationTime, theme);

        List<WaitDetailDto> waits = List.of(WaitDetailDto.of(wait1, 1L), WaitDetailDto.of(wait2, 2L));
        List<WaitInfo> fizzWaitInfos = List.of(WaitInfo.of(wait1, 1L), WaitInfo.of(wait2, 2L));

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);
        when(waitRepository.findOrderByWait(wait1)).thenReturn(1L);
        when(waitRepository.findOrderByWait(wait2)).thenReturn(2L);

        assertThat(waitService.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).isEqualTo(
                fizzWaitInfos);
    }

    @Test
    void findWaitTest() {
        Wait wait = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), "fizz", reservationDate, reservationTime, theme);
        when(waitRepository.findById(1L)).thenReturn(Optional.of(WaitDetailDto.of(wait, 1L)));
        WaitInfo waitInfo = WaitInfo.of(wait, 1L);

        assertThat(waitService.findWait(1L)).isEqualTo(waitInfo);
    }

    @Test
    void findWaitExceptionTest() {
        when(waitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitService.findWait(1L))
                .isInstanceOf(WaitNotExistsException.class);
    }
}
