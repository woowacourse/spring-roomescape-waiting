package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.WaitIsFullException;
import roomescape.exception.custom.WaitNotExistsException;
import roomescape.repository.WaitRepository;

public class WaitServiceTest {
    private WaitService waitService;
    private WaitRepository waitRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private LocalDate reservationDate;
    private Slot slot;
    private Slot otherSlot;
    private Member fizz;
    private Member luke;
    private Member neo;
    private Member lucky;

    private Clock fixedClock;

    @BeforeEach
    void beforeEach() {
        fixedClock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        waitRepository = Mockito.mock(WaitRepository.class);
        waitService = new WaitService(waitRepository, fixedClock);

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
        reservationDate = LocalDate.of(2026, 5, 2);
        slot = new Slot(reservationDate, reservationTime, theme);
        otherSlot = new Slot(LocalDate.of(2026, 5, 3), reservationTime, theme);
        fizz = new Member(1L, "fizz");
        luke = new Member(2L, "luke");
        neo = new Member(3L, "neo");
        lucky = new Member(4L, "lucky");
    }

    @Test
    void saveTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);
        Wait wait = waitWithoutId.withId(1L);

        List<Wait> waits = List.of();

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);
        when(waitRepository.save(waitWithoutId)).thenReturn(wait);
        when(waitRepository.calculateWaitingOrder(wait.getReservationDate(), wait.getTimeId(), wait.getThemeId(),
                wait.getId())).thenReturn(1L);

        assertThat(waitService.save(waitWithoutId)).isEqualTo(wait);
    }

    @Test
    void saveDuplicatedExceptionTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 11, 0), fizz, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 11, 0), luke, slot);

        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);

        List<Wait> waits = List.of(wait1, wait2);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(AlreadyWaitingException.class);
    }

    @Test
    void saveSizeFullExceptionTest() {
        Wait waitWithoutId = new Wait(LocalDateTime.of(2026, 5, 2, 14, 0), fizz, slot);

        Wait otherWait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 11, 0), luke, slot);
        Wait otherWait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 12, 0), neo, slot);
        Wait otherWait3 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 13, 0), lucky, slot);

        List<Wait> waits = List.of(otherWait1, otherWait2, otherWait3);

        when(waitRepository.findBySlot(reservationDate, reservationTime.getId(), theme.getId())).thenReturn(waits);

        assertThatThrownBy(() -> waitService.save(waitWithoutId))
                .isInstanceOf(WaitIsFullException.class);
    }

    @Test
    void findByNameTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), fizz, otherSlot);

        List<Wait> fizzWaits = List.of(wait1, wait2);
        Waits result = new Waits(fizzWaits);

        when(waitRepository.findByMember_Name("fizz")).thenReturn(fizzWaits);
        when(waitRepository.calculateWaitingOrder(wait1.getReservationDate(), wait1.getTimeId(), wait1.getThemeId(),
                wait1.getId())).thenReturn(1L);
        when(waitRepository.calculateWaitingOrder(wait2.getReservationDate(), wait2.getTimeId(), wait2.getThemeId(),
                wait2.getId())).thenReturn(2L);

        assertThat(waitService.findByName("fizz")).isEqualTo(result);
    }

    @Test
    void findAllTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);
        Wait wait2 = new Wait(3L, LocalDateTime.of(2026, 5, 2, 12, 0), luke, otherSlot);

        List<Wait> waits = List.of(wait1, wait2);
        Waits result = new Waits(waits);

        when(waitRepository.findAllWaits()).thenReturn(waits);
        when(waitRepository.calculateWaitingOrder(wait1.getReservationDate(), wait1.getTimeId(), wait1.getThemeId(),
                wait1.getId())).thenReturn(1L);
        when(waitRepository.calculateWaitingOrder(wait2.getReservationDate(), wait2.getTimeId(), wait2.getThemeId(),
                wait2.getId())).thenReturn(2L);

        assertThat(waitService.findAll()).isEqualTo(result);
    }

    @Test
    void deleteTest() {
        waitRepository.deleteById(1L);

        verify(waitRepository, times(1)).deleteById(1L);
    }

    @Test
    void findBySlotTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 2, 12, 0), luke, slot);

        List<Wait> waits = List.of(wait1, wait2);
        Waits result = new Waits(waits);

        when(waitRepository.findBySlot(slot.getReservationDate(), slot.getTime().getId(),
                slot.getTheme().getId())).thenReturn(
                waits);
        when(waitRepository.calculateWaitingOrder(wait1.getReservationDate(), wait1.getTimeId(), wait1.getThemeId(),
                wait1.getId())).thenReturn(1L);
        when(waitRepository.calculateWaitingOrder(wait2.getReservationDate(), wait2.getTimeId(), wait2.getThemeId(),
                wait2.getId())).thenReturn(2L);

        assertThat(waitService.findBySlot(slot)).isEqualTo(result);
    }

    @Test
    void findWaitTest() {
        Wait wait = new Wait(1L, LocalDateTime.of(2026, 5, 2, 10, 0), fizz, slot);
        when(waitRepository.findById(1L)).thenReturn(Optional.of(wait));

        assertThat(waitService.findWait(1L)).isEqualTo(wait);
    }

    @Test
    void findWaitExceptionTest() {
        when(waitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitService.findWait(1L))
                .isInstanceOf(WaitNotExistsException.class);
    }
}
