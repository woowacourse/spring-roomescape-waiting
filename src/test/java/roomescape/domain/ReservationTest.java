package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;
import roomescape.domain.ReservationStatus;

class ReservationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 5, 12, 0);

    private Slot slot(LocalDate date, LocalTime startAt) {
        return new Slot(date, ReservationTime.create(1, startAt), Theme.create(1, "테마", "url", "설명"));
    }

    private Reservation reservation(Member owner, Slot slot) {
        return Reservation.create(1, owner, slot, ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("본인 예약이면 소유권 검증을 통과한다.")
    void validateOwnedByOk() {
        Reservation reservation = reservation(new Member("me"), slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0)));

        assertThatCode(() -> reservation.validateOwnedBy(new Member("me")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("타인 예약이면 소유권 검증에서 예외를 던진다.")
    void validateOwnedByThrows() {
        Reservation reservation = reservation(new Member("me"), slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> reservation.validateOwnedBy(new Member("other")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("이미 시작된 예약은 검증 시 예외를 던진다.")
    void validateNotStartedThrows() {
        Reservation reservation = reservation(new Member("me"), slot(LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> reservation.validateNotStarted(NOW))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    @DisplayName("아직 시작되지 않은 예약은 검증을 통과한다.")
    void validateNotStartedOk() {
        Reservation reservation = reservation(new Member("me"), slot(LocalDate.of(2026, 6, 5), LocalTime.of(14, 0)));

        assertThatCode(() -> reservation.validateNotStarted(NOW))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("isOwnedBy는 이름 일치 여부를 반환한다.")
    void isOwnedBy() {
        Reservation reservation = reservation(new Member("me"), slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0)));

        assertThat(reservation.isOwnedBy(new Member("me"))).isTrue();
        assertThat(reservation.isOwnedBy(new Member("other"))).isFalse();
    }

    @Test
    @DisplayName("withSlot은 id와 owner는 유지하고 슬롯만 교체한 새 예약을 만든다.")
    void withSlot() {
        Slot original = slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0));
        Slot newSlot = slot(LocalDate.of(2026, 6, 7), LocalTime.of(14, 0));
        Reservation reservation = reservation(new Member("me"), original);

        Reservation changed = reservation.withSlot(newSlot);

        assertThat(changed.id()).isEqualTo(reservation.id());
        assertThat(changed.owner()).isEqualTo(new Member("me"));
        assertThat(changed.slot()).isEqualTo(newSlot);
    }
}
