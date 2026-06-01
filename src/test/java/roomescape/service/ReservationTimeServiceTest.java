package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.dao.ReservationTimeDao;
import roomescape.exception.reservationtime.ReservationTimeInUseException;

public class ReservationTimeServiceTest {

    private ReservationTimeDao reservationTimeDao;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeDao = mock(ReservationTimeDao.class);
        reservationTimeService = new ReservationTimeService(reservationTimeDao);
    }

    @Test
    void 존재하지_않는_시간_삭제는_멱등하게_성공한다() {
        when(reservationTimeDao.delete(1L))
                .thenReturn(0);

        assertThatCode(() -> reservationTimeService.deleteReservationTime(1L))
                .doesNotThrowAnyException();
        verify(reservationTimeDao).delete(1L);
    }

    @Test
    void 예약이_있는_시간은_삭제할_수_없다() {
        when(reservationTimeDao.delete(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key violation"));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(ReservationTimeInUseException.class);
    }
}
