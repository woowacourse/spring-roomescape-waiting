package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.fixture.ReservationFixture.createCanceledEntry;
import static roomescape.domain.fixture.ReservationFixture.createEntry;

import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void 예약_엔트리를_생성한다() {
        // when
        Reservation reservation = Reservation.reserve("이프");

        // then
        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getName,
                        Reservation::getStatus
                )
                .containsExactly(null, "이프", ReservationStatus.RESERVED);
        assertThat(reservation.getCreatedAt()).isNotNull();
    }

    @Test
    void 대기_엔트리를_생성한다() {
        // when
        Reservation reservation = Reservation.waiting("이프");

        // then
        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getName,
                        Reservation::getStatus
                )
                .containsExactly(null, "이프", ReservationStatus.WAITING);
        assertThat(reservation.getCreatedAt()).isNotNull();
    }

    @Test
    void 활성_예약_상태이면_true를_반환한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.RESERVED);

        // when & then
        assertThat(reservation.isActiveReserved()).isTrue();
    }

    @Test
    void 활성_대기_상태이면_true를_반환한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.WAITING);

        // when & then
        assertThat(reservation.isActiveWaiting()).isTrue();
    }

    @Test
    void 활성_상태에서_같은_식별자이면_true를_반환한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.RESERVED);

        // when & then
        assertThat(reservation.isActiveWithId(1L)).isTrue();
    }

    @Test
    void 취소_상태이면_활성_예약_상태가_아니다() {
        // given
        Reservation reservation = createCanceledEntry(1L, "이프", ReservationStatus.RESERVED);

        // when & then
        assertThat(reservation.isActiveReserved()).isFalse();
    }

    @Test
    void 같은_식별자이면_true를_반환한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.RESERVED);

        // when & then
        assertThat(reservation.isSameId(1L)).isTrue();
    }

    @Test
    void 식별자가_없으면_false를_반환한다() {
        // given
        Reservation reservation = createEntry(null, ReservationStatus.RESERVED);

        // when & then
        assertThat(reservation.isSameId(1L)).isFalse();
    }

    @Test
    void 엔트리를_취소한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.RESERVED);

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getActiveStatus()).isEqualTo(ReservationActiveStatus.CANCELED);
    }

    @Test
    void 엔트리를_예약으로_승격한다() {
        // given
        Reservation reservation = createEntry(1L, ReservationStatus.WAITING);

        // when
        reservation.promote();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}
