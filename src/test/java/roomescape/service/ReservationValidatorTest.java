package roomescape.service;

import org.junit.jupiter.api.Test;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ReservationValidatorTest {

    private final ReservationRepository reservationRepository = mock();
    private final ReservationValidator validator = new ReservationValidator(reservationRepository);

    @Test
    void 미래_날짜와_시간이면_통과한다() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateNotPast(LocalDate.now().plusDays(1), time));
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 지난_날짜나_시간이면_예외가_발생한다() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));

        // when & then
        assertThatThrownBy(() -> validator.validateNotPast(LocalDate.now().minusDays(1), time))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_SCHEDULE)
                .hasMessage("이미 지난 시간으로는 예약할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 예약되지_않은_시간이면_통과한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        when(reservationRepository.existsWith(date, timeId, themeId))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateAlreadyReserved(date, timeId, themeId));
        verify(reservationRepository, times(1)).existsWith(date, timeId, themeId);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 이미_예약된_시간이면_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        when(reservationRepository.existsWith(date, timeId, themeId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validator.validateAlreadyReserved(date, timeId, themeId))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessage("이미 예약된 시간입니다.");
        verify(reservationRepository, times(1)).existsWith(date, timeId, themeId);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 본인의_미래_예약이면_변경_가능하다() {
        // given
        Reservation reservation = createReservation("브라운", LocalDate.now().plusDays(1), new ReservationTime(1L, LocalTime.parse("08:00")));

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateUpdatableReservation(reservation, "브라운"));
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 본인의_예약이_아니면_변경_가능_검증시_예외가_발생한다() {
        // given
        Reservation reservation = createReservation("구구", LocalDate.now().plusDays(1), new ReservationTime(1L, LocalTime.parse("08:00")));

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatableReservation(reservation, "브라운"))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_RESOURCE)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 지난_예약이면_변경_가능_검증시_예외가_발생한다() {
        // given
        Reservation reservation = createReservation("브라운", LocalDate.now().minusDays(1), new ReservationTime(1L, LocalTime.parse("08:00")));

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatableReservation(reservation, "브라운"))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_RESOURCE_LOCKED)
                .hasMessage("이미 지난 예약은 변경하거나 취소할 수 없습니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 변경하려는_날짜와_시간이_유효하면_통과한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = createReservation("브라운", date, new ReservationTime(1L, LocalTime.parse("08:00")));
        Reservation updatedReservation = createReservation("브라운", date, new ReservationTime(2L, LocalTime.parse("09:00")));
        when(reservationRepository.existsWith(date, 2L, 1L))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateUpdatePolicy(reservation, updatedReservation));
        verify(reservationRepository, times(1)).existsWith(date, 2L, 1L);
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 기존_날짜와_시간으로_예약_변경시_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
        Reservation reservation = createReservation("브라운", date, time);
        Reservation updatedReservation = createReservation("브라운", date, time);

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatePolicy(reservation, updatedReservation))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCHANGED_RESERVATION)
                .hasMessage("기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다.");
        verify(reservationRepository, never()).existsWith(any(LocalDate.class), anyLong(), anyLong());
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 변경할_값이_있으면_통과한다() {
        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateUpdateValueExists(LocalDate.now().plusDays(1), null));
        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    void 변경할_값이_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> validator.validateUpdateValueExists(null, null))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT)
                .hasMessage("변경할 날짜 또는 시간이 필요합니다.");
        verifyNoMoreInteractions(reservationRepository);
    }

    private Reservation createReservation(String name, LocalDate date, ReservationTime time) {
        return new Reservation(
                1L,
                name,
                date,
                time,
                new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소"));
    }
}
