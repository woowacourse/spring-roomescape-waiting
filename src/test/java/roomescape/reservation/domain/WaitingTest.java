package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.RoomEscapeException;

class WaitingTest {

    private static final User STARK = User.builder()
            .name("스타크")
            .build();
    private static final Rank FIRST_RANK = Rank.builder()
            .value(1)
            .build();

    @DisplayName("현재 시간 이후의 슬롯으로 예약 대기 생성을 테스트합니다.")
    @Test
    void create_waiting() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        Waiting waiting = Waiting.create(
                STARK,
                slot,
                LocalDateTime.of(2026, 5, 1, 9, 0)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waiting.getUser()).isEqualTo(STARK);
            softly.assertThat(waiting.getSlot()).isEqualTo(slot);
        });
    }

    @DisplayName("현재 시간보다 이전 슬롯으로 예약 대기 생성 시 예외를 테스트합니다.")
    @Test
    void create_past_waiting_exception() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        assertThatThrownBy(() -> Waiting.create(
                STARK,
                slot,
                LocalDateTime.of(2026, 6, 1, 9, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("예약 대기 순번 미루기를 테스트합니다.")
    @Test
    void postpone_waiting() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Waiting waiting = Waiting.builder()
                .user(STARK)
                .slot(slot)
                .rank(FIRST_RANK)
                .build();

        Waiting postponedWaiting = waiting.postpone(2, 4, LocalDateTime.of(2026, 5, 1, 9, 0));

        assertThat(postponedWaiting.getRank().value()).isEqualTo(3);
    }

    @DisplayName("이미 지나간 슬롯의 예약 대기 순번 미루기 시 예외를 테스트합니다.")
    @Test
    void postpone_past_waiting_exception() {
        ReservationSlot slot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 30))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();
        Waiting waiting = Waiting.builder()
                .user(STARK)
                .slot(slot)
                .rank(FIRST_RANK)
                .build();

        assertThatThrownBy(() -> waiting.postpone(
                1,
                4,
                LocalDateTime.of(2026, 6, 1, 9, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 미룰 수 없습니다.");
    }
}
