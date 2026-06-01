package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.auth.Role;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;

public class ReservationServiceTest {

    private static final long BROWN_ID = 1L;
    private static final long JEONGKONG_ID = 2L;

    private ReservationDao reservationDao;
    private ReservationTimeDao reservationTimeDao;
    private ReservationService reservationService;
    private ReservationWaitDao reservationWaitDao;

    @BeforeEach
    void setUp() {
        reservationDao = mock(ReservationDao.class);
        reservationTimeDao = mock(ReservationTimeDao.class);
        reservationWaitDao = mock(ReservationWaitDao.class);
        reservationService = new ReservationService(reservationDao, reservationTimeDao, reservationWaitDao);
    }

    @Test
    void 존재하지_않는_예약은_삭제할_수_없다() {
        when(reservationDao.findReservationById(3L))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.deleteReservation(3L, BROWN_ID))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void 예약이_존재할_경우_새_예약을_생성할_수_없다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(reservationTimeDao.findReservationTimeById(1L))
                .thenReturn(new ReservationTime(
                        1L,
                        LocalTime.of(10, 0)
                ));
        when(reservationDao.insert(any(Reservation.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key exception"));

        assertThatThrownBy(() -> reservationService.createReservation(JEONGKONG_ID, futureDate, 1L, 1L, 1L))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 대기자가_없으면_예약을_삭제한다() {
        long reservationId = 1L;
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        futureDate,
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));
        when(reservationWaitDao.findEarliestMemberId(reservationId))
                .thenReturn(Optional.empty());

        reservationService.deleteReservation(reservationId, BROWN_ID);

        verify(reservationDao).delete(reservationId);
        verify(reservationDao, never()).update(any(Reservation.class));
        verify(reservationWaitDao, never()).deleteByReservationIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    void 대기자가_있으면_가장_빠른_대기자에게_예약을_양도한다() {
        long reservationId = 1L;
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        futureDate,
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));
        when(reservationWaitDao.findEarliestMemberId(reservationId))
                .thenReturn(Optional.of(JEONGKONG_ID));

        reservationService.deleteReservation(reservationId, BROWN_ID);

        verify(reservationDao).update(argThat(r ->
                r.getId().equals(reservationId) && r.getMemberId().equals(JEONGKONG_ID)));
        verify(reservationWaitDao).deleteByReservationIdAndMemberId(reservationId, JEONGKONG_ID);
        verify(reservationDao, never()).delete(anyLong());
    }

    @Test
    void 존재하지_않는_예약시간으로는_예약을_변경할_수_없다() {
        long reservationId = 1L;
        long timeId = 999L;
        LocalDate futureDate = LocalDate.now().plusDays(1);

        when(reservationTimeDao.findReservationTimeById(timeId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId,
                futureDate,
                BROWN_ID,
                timeId
        )).isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 이미_예약된_시간으로는_예약을_변경할_수_없다() {
        long reservationId = 1L;
        long timeId = 2L;
        LocalDate futureDate = LocalDate.now().plusDays(1);

        when(reservationTimeDao.findReservationTimeById(timeId))
                .thenReturn(new ReservationTime(
                        timeId,
                        LocalTime.of(11, 0)
                ));
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        futureDate,
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));
        when(reservationDao.update(any(Reservation.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key exception"));

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId,
                futureDate,
                BROWN_ID,
                timeId
        )).isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 존재하지_않는_예약은_변경할_수_없다() {
        long reservationId = 1L;
        long timeId = 2L;
        LocalDate futureDate = LocalDate.now().plusDays(1);

        when(reservationTimeDao.findReservationTimeById(timeId))
                .thenReturn(new ReservationTime(
                        timeId,
                        LocalTime.of(11, 0)
                ));
        when(reservationDao.findReservationById(reservationId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId,
                futureDate,
                BROWN_ID,
                timeId
        )).isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void 매니저가_자기_매장_예약을_삭제할_수_있다() {
        long reservationId = 1L;
        long storeId = 1L;
        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, storeId);

        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        LocalDate.now().plusDays(1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        storeId
                ));

        assertThatCode(() -> reservationService.deleteReservationByManager(reservationId, gangnamManager))
                .doesNotThrowAnyException();

        verify(reservationDao).delete(reservationId);
    }

    @Test
    void 매니저가_존재하지_않는_예약을_삭제할_수_없다() {
        long reservationId = 999L;
        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

        when(reservationDao.findReservationById(reservationId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.deleteReservationByManager(reservationId, gangnamManager))
                .isInstanceOf(ReservationNotFoundException.class);
    }


    @Test
    void 매니저가_자기_매장_예약을_변경할_수_있다() {
        long reservationId = 1L;
        long newTimeId = 2L;
        LocalDate futureDate = LocalDate.now().plusDays(5);
        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

        when(reservationTimeDao.findReservationTimeById(newTimeId))
                .thenReturn(new ReservationTime(newTimeId, LocalTime.of(11, 0)));
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        futureDate,
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));

        assertThatCode(() -> reservationService.updateReservationByManager(
                reservationId, futureDate, newTimeId, gangnamManager))
                .doesNotThrowAnyException();

        verify(reservationDao).update(argThat(r ->
                r.getId().equals(reservationId)
                        && r.getDate().equals(futureDate)
                        && r.getTime().getId().equals(newTimeId)));
    }

}
