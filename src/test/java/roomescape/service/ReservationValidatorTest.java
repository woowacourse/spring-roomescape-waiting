package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReservationValidatorTest {

    private final ReservationRepository reservationRepository = mock();
    private final ReservationValidator validator = new ReservationValidator(reservationRepository);

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
    private final Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");

    @Test
    void 사용자가_미래_예약_가능한_슬롯이면_생성할_수_있다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        when(reservationRepository.existsBySlot(reservation))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateCreatableByUser(reservation, now));
        verify(reservationRepository, times(1)).existsBySlot(reservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 사용자가_지난_시간으로_예약_생성시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().minusDays(1), time);

        // when & then
        assertThatThrownBy(() -> validator.validateCreatableByUser(reservation, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_SCHEDULE)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 이미_예약된_슬롯이면_예약_생성시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        when(reservationRepository.existsBySlot(reservation))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validator.validateCreatableByUser(reservation, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessage("이미 예약된 시간입니다.");
        verify(reservationRepository, times(1)).existsBySlot(reservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 관리자는_지난_시간이어도_예약_가능한_슬롯이면_생성할_수_있다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().minusDays(1), time);
        when(reservationRepository.existsBySlot(reservation))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateCreatableByAdmin(reservation));
        verify(reservationRepository, times(1)).existsBySlot(reservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 본인의_미래_예약이면_변경_가능하다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateModifiableByUser(reservation, "브라운", now));
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 본인의_예약이_아니면_변경_가능_검증시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("구구", now.toLocalDate().plusDays(1), time);

        // when & then
        assertThatThrownBy(() -> validator.validateModifiableByUser(reservation, "브라운", now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_RESOURCE)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 지난_예약이면_변경_가능_검증시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().minusDays(1), time);

        // when & then
        assertThatThrownBy(() -> validator.validateModifiableByUser(reservation, "브라운", now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_RESOURCE_LOCKED)
                .hasMessage("이미 지난 예약은 변경하거나 취소할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 변경하려는_예약이_유효하면_통과한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        Reservation updatedReservation = reservation("브라운", now.toLocalDate().plusDays(1),
                new ReservationTime(2L, LocalTime.parse("09:00")));
        when(reservationRepository.existsBySlot(updatedReservation))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateUpdatedReservation(reservation, updatedReservation, now));
        verify(reservationRepository, times(1)).existsBySlot(updatedReservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 기존_날짜와_시간으로_예약_변경시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        Reservation updatedReservation = reservation("브라운", now.toLocalDate().plusDays(1), time);

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatedReservation(reservation, updatedReservation, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCHANGED_RESERVATION)
                .hasMessage("기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 지난_시간으로_예약_변경시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        Reservation updatedReservation = reservation("브라운", now.toLocalDate().minusDays(1), time);

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatedReservation(reservation, updatedReservation, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_SCHEDULE)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 이미_예약된_슬롯으로_예약_변경시_예외가_발생한다() {
        // given
        Reservation reservation = reservation("브라운", now.toLocalDate().plusDays(1), time);
        Reservation updatedReservation = reservation("브라운", now.toLocalDate().plusDays(1),
                new ReservationTime(2L, LocalTime.parse("09:00")));
        when(reservationRepository.existsBySlot(updatedReservation))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatedReservation(reservation, updatedReservation, now))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessage("이미 예약된 시간입니다.");
        verify(reservationRepository, times(1)).existsBySlot(updatedReservation);
        verifyNoMoreInteractions(reservationRepository);
    }

    private Reservation reservation(String name, LocalDate date, ReservationTime time) {
        return new Reservation(1L, name, date, time, theme);
    }
}
