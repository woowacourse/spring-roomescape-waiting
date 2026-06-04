package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

class ReservationWaitingTest {

    @Test
    @DisplayName("ReservationWaiting 객체를 성공적으로 생성하고 필드를 검증한다.")
    void create_Success() {
        // given
        String name = "브라운";
        LocalDate date = LocalDate.of(2026, 5, 5);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");

        // when
        ReservationWaiting waiting = new ReservationWaiting(name, date, time, theme, LocalDateTime.of(2026, 5, 4, 10, 0));

        // then
        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getName()).isEqualTo(name);
        assertThat(waiting.getDate()).isEqualTo(date);
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getRequestedAt()).isEqualTo(LocalDateTime.of(2026, 5, 4, 10, 0));
    }

    @Test
    @DisplayName("예약 대기 시간이 현재보다 미래인 경우 유효성 검증을 통과한다.")
    void validateExpiry_Future_Success() {
        // given
        LocalDate futureDate = LocalDate.of(2026, 5, 5);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(null, "브라운", new roomescape.reservation.domain.ReservationSlot(futureDate, time, theme), futureDate.atStartOfDay());

        // when & then
        assertThatCode(() -> waiting.validateDeletable("브라운", LocalDateTime.of(2026, 5, 4, 10, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 날짜가 현재보다 과거인 경우 예외를 발생시킨다.")
    void validateExpiry_PastDate_ThrowsException() {
        // given
        LocalDate pastDate = LocalDate.of(2026, 5, 5);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(null, "브라운", new roomescape.reservation.domain.ReservationSlot(pastDate, time, theme), pastDate.atStartOfDay());

        // when & then
        assertThatThrownBy(() -> waiting.validateDeletable("브라운", LocalDateTime.of(2026, 5, 6, 10, 0)))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 대기 날짜는 오늘이나 시간이 현재보다 과거인 경우 예외를 발생시킨다.")
    void validateExpiry_PastTime_ThrowsException() {
        // given
        LocalDate today = LocalDate.of(2026, 5, 5);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(null, "브라운", new roomescape.reservation.domain.ReservationSlot(today, time, theme), today.atStartOfDay());

        // when & then
        assertThatThrownBy(() -> waiting.validateDeletable("브라운", LocalDateTime.of(2026, 5, 5, 11, 0)))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 대기 작성자 본인 검증 시 일치하면 예외가 발생하지 않는다.")
    void validateOwner_Match_Success() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());

        // when & then
        assertThatCode(() -> waiting.validateDeletable("브라운", java.time.LocalDateTime.now().plusDays(2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 작성자 본인 검증 시 일치하지 않으면 예외를 발생시킨다.")
    void validateOwner_Mismatch_ThrowsException() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());

        // when & then
        assertThatThrownBy(() -> waiting.validateDeletable("네오", java.time.LocalDateTime.now().plusDays(2)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationWaitingErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("식별자(ID)가 같으면 동등한 객체로 판단한다.")
    void equalsAndHashCode_SameId_Success() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting1 = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());
        ReservationWaiting waiting2 = new ReservationWaiting(1L, "네오", slot, slot.date().atStartOfDay());

        // when & then
        assertThat(waiting1).isEqualTo(waiting2);
        assertThat(waiting1.hashCode()).isEqualTo(waiting2.hashCode());
    }

    @Test
    @DisplayName("예약 대기는 requestedAt, id 순으로 정렬된다.")
    void compareTo_ordersByRequestedAtThenId() {
        // given
        roomescape.reservation.domain.ReservationSlot slot = new roomescape.reservation.domain.ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting first = new ReservationWaiting(2L, "브라운", slot, LocalDateTime.of(2026, 5, 4, 10, 0));
        ReservationWaiting second = new ReservationWaiting(1L, "브라운", slot, LocalDateTime.of(2026, 5, 4, 10, 0));
        ReservationWaiting third = new ReservationWaiting(3L, "브라운", slot, LocalDateTime.of(2026, 5, 4, 9, 0));

        List<ReservationWaiting> waitings = new ArrayList<>(List.of(first, second, third));

        // when
        Collections.sort(waitings);

        // then
        assertThat(waitings).containsExactly(third, second, first);
    }
    @Test
    @DisplayName("대기 신청 슬롯의 예약자가 본인과 다르면 예외가 발생하지 않는다.")
    void validateNoConflictWithReservation_DifferentName_Success() {
        // given
        ReservationSlot slot = new ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());
        Reservation targetReservation = new Reservation(1L, "포비", slot, slot.date().atStartOfDay());

        // when & then
        assertThatCode(() -> waiting.validateNoConflictWithReservation(targetReservation))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대기 신청 슬롯의 예약자가 본인이면 InvalidBusinessStateException이 발생한다.")
    void validateNoConflictWithReservation_SameName_ThrowsException() {
        // given
        ReservationSlot slot = new ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());
        Reservation targetReservation = new Reservation(1L, "브라운", slot, slot.date().atStartOfDay());

        // when & then
        assertThatThrownBy(() -> waiting.validateNoConflictWithReservation(targetReservation))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
    }

    @Test
    @DisplayName("동일 슬롯에 대기가 없으면 validateNoDuplicateWaiting은 예외가 발생하지 않는다.")
    void validateNoDuplicateWaiting_NoDuplicate_Success() {
        // given
        ReservationSlot slot = new ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());

        // when & then
        assertThatCode(() -> waiting.validateNoDuplicateWaiting(false))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("동일 슬롯에 이미 대기가 있으면 validateNoDuplicateWaiting은 InvalidBusinessStateException이 발생한다.")
    void validateNoDuplicateWaiting_Duplicate_ThrowsException() {
        // given
        ReservationSlot slot = new ReservationSlot(
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마", "설명", "url")
        );
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", slot, slot.date().atStartOfDay());

        // when & then
        assertThatThrownBy(() -> waiting.validateNoDuplicateWaiting(true))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
    }
}



