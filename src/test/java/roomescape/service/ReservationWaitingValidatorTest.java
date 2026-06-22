package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationWaitingValidatorTest {

    private final JpaReservationRepository reservationRepository = mock();
    private final JpaReservationWaitingRepository reservationWaitingRepository = mock();
    private final ReservationWaitingValidator validator = new ReservationWaitingValidator(
            reservationRepository,
            reservationWaitingRepository);

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
    private final Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");

    @Test
    void 예약된_시간이고_본인_예약과_중복_대기가_아니면_대기_신청이_가능하다() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, time.getId(), theme.getId()))
                .thenReturn(true);
        when(reservationRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                "브라운", date, time.getId(), theme.getId()))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                "브라운", date, time.getId(), theme.getId()))
                .thenReturn(false);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateWaiting(waiting));
    }

    @Test
    void 예약_가능한_시간에_대기_신청시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, time.getId(), theme.getId()))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> validator.validateWaiting(waiting))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약 가능한 시간에는 대기를 신청할 수 없습니다.");
    }

    @Test
    void 본인이_예약한_시간에_대기_신청시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, time.getId(), theme.getId()))
                .thenReturn(true);
        when(reservationRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                "브라운", date, time.getId(), theme.getId()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validator.validateWaiting(waiting))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
    }

    @Test
    void 이미_대기한_시간에_대기_신청시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);
        when(reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, time.getId(), theme.getId()))
                .thenReturn(true);
        when(reservationRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                "브라운", date, time.getId(), theme.getId()))
                .thenReturn(false);
        when(reservationWaitingRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                "브라운", date, time.getId(), theme.getId()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> validator.validateWaiting(waiting))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 예약 대기를 신청한 시간입니다.");
    }

    @Test
    void 지난_시간으로_예약_대기_신청시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(() -> validator.validateWaiting(waiting))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 시간으로는 예약 대기를 신청할 수 없습니다.");
    }

    @Test
    void 본인의_미래_예약_대기이면_삭제할_수_있다() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validateUpdatableReservation(waiting, "브라운"));
    }

    @Test
    void 다른_사용자의_예약_대기_삭제시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", date);

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatableReservation(waiting, "구구"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 예약 대기만 취소할 수 있습니다.");
    }

    @Test
    void 지난_예약_대기_삭제시_예외_발생() {
        // given
        ReservationWaiting waiting = waiting("브라운", LocalDate.now().minusDays(1));

        // when & then
        assertThatThrownBy(() -> validator.validateUpdatableReservation(waiting, "브라운"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 예약 대기는 취소할 수 없습니다.");
    }

    private ReservationWaiting waiting(String name, LocalDate date) {
        return new ReservationWaiting(1L, name, date, time, theme);
    }
}
