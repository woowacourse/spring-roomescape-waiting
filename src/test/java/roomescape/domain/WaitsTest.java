package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.WaitIsFullException;

public class WaitsTest {

    private ReservationTime reservationTime;
    private Theme theme;
    private Slot slot;
    private Member luke;
    private Member fizz;
    private Member neo;
    private Member brown;

    @BeforeEach
    void beforeEach() {
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "테마", "설명", "url.jpg");
        slot = new Slot(LocalDate.of(2026, 5, 1), reservationTime, theme);
        luke = new Member(1L, "luke");
        fizz = new Member(2L, "fizz");
        neo = new Member(3L, "neo");
        brown = new Member(4L, "brown");
    }

    @Test
    void isFullWaitsBySlotTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 1, 9, 0), luke, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 1, 9, 1), fizz, slot);
        Wait wait3 = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 2), neo, slot);

        Slot otherSlot = new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme);
        Wait otherWait = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 2), neo, otherSlot);

        Waits waits1 = new Waits(List.of(wait1, wait2, otherWait));
        assertThat(waits1.isFullWaitsBySlot(slot)).isFalse();

        Waits waits2 = new Waits(List.of(wait1, wait2, wait3, otherWait));
        assertThat(waits2.isFullWaitsBySlot(slot)).isTrue();
    }

    @Test
    void isEmptyWaitsBySlotTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 1, 9, 0), luke, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 1, 9, 1), fizz, slot);
        Wait wait3 = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 2), neo, slot);

        Slot otherSlot = new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme);

        Waits waits1 = new Waits(List.of(wait1, wait2, wait3));
        assertThat(waits1.isEmptyWaitsBySlot(slot)).isFalse();

        Waits waits2 = new Waits(List.of(wait1, wait2, wait3));
        assertThat(waits2.isEmptyWaitsBySlot(otherSlot)).isTrue();
    }

    @Test
    void validateCreateDuplicatedExceptionTest() {
        Waits waits = new Waits(List.of(new Wait(1L, LocalDateTime.now(), fizz, slot)));

        assertThatThrownBy(() -> waits.validateCreate(fizz, slot))
                .isInstanceOf(AlreadyWaitingException.class);
    }

    @Test
    void validateCreateDuplicatedWaitFullExceptionTest() {
        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 1, 9, 0), luke, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 1, 9, 1), fizz, slot);
        Wait wait3 = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 2), neo, slot);
        Waits waits = new Waits(List.of(wait1, wait2, wait3));

        assertThatThrownBy(() -> waits.validateCreate(brown, slot))
                .isInstanceOf(WaitIsFullException.class);
    }

    @Test
    void firstWaitBySlotTest() {
        Slot otherSlot = new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme);

        Wait wait1 = new Wait(1L, LocalDateTime.of(2026, 5, 1, 9, 0), luke, slot);
        Wait wait2 = new Wait(2L, LocalDateTime.of(2026, 5, 1, 9, 1), fizz, otherSlot);
        Wait wait3 = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 2), neo, otherSlot);

        Waits waits = new Waits(List.of(wait1, wait2, wait3));

        assertThat(waits.firstWaitBySlot(otherSlot)).isEqualTo(wait2);
    }

    @Test
    void waitsWithOrderTest() {
        Wait firstWait = new Wait(2L, LocalDateTime.of(2026, 5, 1, 7, 0), fizz, slot);
        Wait secondWait = new Wait(1L, LocalDateTime.of(2026, 5, 1, 8, 0), luke, slot);
        Wait thirdWait = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 0), neo, slot);

        Waits waits = new Waits(List.of(secondWait, thirdWait, firstWait));

        Map<Wait, Long> waitsWithOrder = waits.waitsWithOrder();

        assertThat(waitsWithOrder.get(firstWait)).isEqualTo(1L);
        assertThat(waitsWithOrder.get(secondWait)).isEqualTo(2L);
        assertThat(waitsWithOrder.get(thirdWait)).isEqualTo(3L);
    }

    @Test
    void waitsWithOrderByNameTest() {
        Slot otherSlot = new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme);

        Wait firstWaitFizz = new Wait(2L, LocalDateTime.of(2026, 5, 1, 7, 0), fizz, slot);
        Wait firstWaitLuke = new Wait(1L, LocalDateTime.of(2026, 5, 1, 8, 0), luke, otherSlot);
        Wait secondWaitFizz = new Wait(3L, LocalDateTime.of(2026, 5, 1, 9, 0), fizz, otherSlot);

        Waits waits = new Waits(List.of(firstWaitFizz, firstWaitLuke, secondWaitFizz));

        Map<Wait, Long> waitsWithOrder = waits.waitsWithOrderByName("fizz");

        assertThat(waitsWithOrder.get(firstWaitFizz)).isEqualTo(1L);
        assertThat(waitsWithOrder.get(secondWaitFizz)).isEqualTo(2L);
    }

    @Test
    void calculateOrderTest() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url.jpg");

        Slot slotA = new Slot(LocalDate.of(2026, 5, 1), time, theme);
        Slot slotB = new Slot(LocalDate.of(2026, 5, 2), time, theme);

        Wait waitB1 = new Wait(1L, LocalDateTime.of(2026, 5, 1, 9, 0), luke, slotB);
        Wait waitA1 = new Wait(2L, LocalDateTime.of(2026, 5, 1, 10, 0), fizz, slotA);
        Wait waitB2 = new Wait(3L, LocalDateTime.of(2026, 5, 1, 11, 0), neo, slotB);

        Waits waits = new Waits(List.of(waitB1, waitA1, waitB2));

        assertThat(waits.calculateOrder(waitA1)).isEqualTo(1L);
    }
}
