package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitingListTest {

    private final ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    private final Theme theme = Theme.createWithId(1L, "우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg", 30000L);

    @Test
    void WaitingList_객체_생성() {
        final String name = "재즈";
        final LocalDate date = LocalDate.now();

        final WaitingList waitingList = WaitingList.create(name, date, reservationTime, theme);

        assertThat(waitingList.getName()).isEqualTo(name);
        assertThat(waitingList.getReservationDate().date()).isEqualTo(date);
        assertThat(waitingList.getReservationTime()).isEqualTo(reservationTime);
        assertThat(waitingList.getTheme()).isEqualTo(theme);
        assertThat(waitingList.getCreatedAt()).isNotNull();
    }

    @Test
    void ID를_포함한_WaitingList_객체_생성() {
        final LocalDateTime createdAt = LocalDateTime.now();

        final WaitingList waitingList = WaitingList.createWithId(1L, "재즈", LocalDate.now(), reservationTime, theme, createdAt);

        assertThat(waitingList.getId()).isEqualTo(1L);
        assertThat(waitingList.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void ID가_null이면_예외발생() {
        assertThatThrownBy(() -> WaitingList.createWithId(null, "재즈", LocalDate.now(), reservationTime, theme, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ID를_변경한_새로운_WaitingList_객체_반환() {
        final WaitingList waitingList = WaitingList.create("재즈", LocalDate.now(), reservationTime, theme);

        final WaitingList waitingListWithId = waitingList.withId(1L);

        assertThat(waitingListWithId.getId()).isEqualTo(1L);
        assertThat(waitingListWithId.getName()).isEqualTo(waitingList.getName());
    }
}
