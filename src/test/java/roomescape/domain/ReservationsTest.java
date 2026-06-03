package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReservationsTest {

    @Test
    void 예약_엔트리를_추가한다() {
        // given
        Reservations reservations = new Reservations(List.of());

        // when
        reservations.addReserved("이프");

        // then
        assertThat(reservations.getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 대기_엔트리를_추가한다() {
        // given
        Reservations reservations = new Reservations(List.of());

        // when
        reservations.addWaiting("이프");

        // then
        assertThat(reservations.getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.WAITING);
    }

    @Test
    void 예약된_엔트리가_있으면_true를_반환한다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now()),
                reservation(2L, "라텔", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(reservations.hasReservedReservation()).isTrue();
    }

    @Test
    void 예약된_엔트리가_없으면_false를_반환한다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now()),
                canceledReservation(2L, "라텔", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(reservations.hasReservedReservation()).isFalse();
    }

    @Test
    void 식별자로_엔트리를_조회한다() {
        // given
        Reservation expected = reservation(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now());
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                expected
        ));

        // when & then
        assertThat(reservations.findById(2L)).contains(expected);
    }

    @Test
    void 식별자가_없는_엔트리는_식별자로_조회되지_않는다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(null, "이프", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(reservations.findById(1L)).isEmpty();
    }

    @Test
    void 활성_상태인_같은_이름이_존재하면_true를_반환한다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                canceledReservation(2L, "라텔", ReservationStatus.RESERVED, LocalDateTime.now())
        ));

        // when & then
        assertThat(reservations.hasReservationByName("이프")).isTrue();
        assertThat(reservations.hasReservationByName("라텔")).isFalse();
    }

    @Test
    void 대기_상태도_같은_이름으로_간주한다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.WAITING, LocalDateTime.now())
        ));

        // when & then
        assertThat(reservations.hasReservationByName("이프")).isTrue();
    }

    @Test
    void 가장_먼저_등록된_대기_엔트리를_예약으로_승격한다() {
        // given
        Reservation reserved = reservation(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now());
        Reservation firstWaiting = reservation(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(2));
        Reservation secondWaiting = reservation(3L, "이든", ReservationStatus.WAITING, LocalDateTime.now().minusMinutes(1));
        Reservations reservations = new Reservations(List.of(reserved, firstWaiting, secondWaiting));

        // when
        reservations.promoteFirstWaiting();

        // then
        assertThat(reservations.getReservations())
                .extracting(Reservation::getId, Reservation::getStatus)
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED),
                        tuple(2L, ReservationStatus.RESERVED),
                        tuple(3L, ReservationStatus.WAITING)
                );
    }

    @Test
    void 대기_엔트리가_없으면_승격하지_않는다() {
        // given
        Reservations reservations = new Reservations(List.of(
                reservation(1L, "이프", ReservationStatus.RESERVED, LocalDateTime.now()),
                canceledReservation(2L, "라텔", ReservationStatus.WAITING, LocalDateTime.now())
        ));

        // when
        reservations.promoteFirstWaiting();

        // then
        assertThat(reservations.getReservations())
                .extracting(Reservation::getId, Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED, ReservationActiveStatus.ACTIVE),
                        tuple(2L, ReservationStatus.WAITING, ReservationActiveStatus.CANCELED)
                );
    }

    private Reservation reservation(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return new Reservation(id, name, status, createdAt);
    }

    private Reservation canceledReservation(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return new Reservation(id, name, status, ReservationActiveStatus.CANCELED, createdAt);
    }
}
