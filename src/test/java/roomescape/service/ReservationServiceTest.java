package roomescape.service;

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
import roomescape.domain.ReservationWait;
import roomescape.exception.auth.WrongStoreAccessException;
import roomescape.exception.reservation.*;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.reservationwait.PastReservationWaitNotAllowedException;
import roomescape.exception.reservationwait.ReservationWaitAlreadyExistsException;
import roomescape.exception.reservationwait.SelfReservationWaitNotAllowedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        when(reservationDao.insertWithKeyHolder(JEONGKONG_ID, futureDate, 1L, 1L, 1L))
                .thenThrow(new DuplicateKeyException("Duplicate key exception"));

        assertThatThrownBy(() -> reservationService.createReservation(JEONGKONG_ID, futureDate, 1L, 1L, 1L))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 지난_날짜로는_새_예약을_생성할_수_없다() {
        when(reservationTimeDao.findReservationTimeById(1L))
                .thenReturn(new ReservationTime(
                        1L,
                        LocalTime.of(10, 0)
                ));
        assertThatThrownBy(() -> reservationService.createReservation(JEONGKONG_ID, LocalDate.of(2025, 1, 1), 1L, 1L, 1L))
                .isInstanceOf(PastReservationNotAllowedException.class);
    }

    @Test
    void 이미_지난_예약은_취소할_수_없다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        when(reservationDao.findReservationById(1L))
                .thenReturn(new Reservation(
                        1L,
                        JEONGKONG_ID,
                        pastDate,
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));
        assertThatThrownBy(() -> reservationService.deleteReservation(1L, JEONGKONG_ID))
                .isInstanceOf(PastReservationCancelNotAllowedException.class);
    }

    @Test
    void 예약자가_일치하지_않으면_예약을_삭제할_수_없다() {
        long reservationId = 1L;
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        LocalDate.now().plusDays(1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));

        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId, JEONGKONG_ID))
                .isInstanceOf(ReservationOwnerMismatchException.class);

        verify(reservationWaitDao, never()).findEarliestMemberId(anyLong());
        verify(reservationDao, never()).delete(anyLong());
        verify(reservationDao, never()).updateMemberId(anyLong(), anyLong());
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
        verify(reservationDao, never()).updateMemberId(anyLong(), anyLong());
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

        verify(reservationDao).updateMemberId(reservationId, JEONGKONG_ID);
        verify(reservationWaitDao).deleteByReservationIdAndMemberId(reservationId, JEONGKONG_ID);
        verify(reservationDao, never()).delete(anyLong());
    }

    @Test
    void 예약자가_일치하지_않으면_예약을_변경할_수_없다() {
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

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId,
                futureDate,
                JEONGKONG_ID,
                timeId
        )).isInstanceOf(ReservationOwnerMismatchException.class);
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
        when(reservationDao.updateById(reservationId, futureDate, timeId))
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
    void 지난_날짜로는_예약을_변경할_수_없다() {
        long timeId = 1L;
        long reservationId = 2L;
        LocalDate pastDate = LocalDate.now().minusDays(1);

        when(reservationTimeDao.findReservationTimeById(timeId))
                .thenReturn(new ReservationTime(
                        timeId,
                        LocalTime.of(11, 0)
                ));
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        LocalDate.now().plusDays(1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservationId,
                pastDate,
                BROWN_ID,
                timeId
        )).isInstanceOf(PastReservationNotAllowedException.class);
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

        assertThatCode(() -> reservationService.deleteByManager(reservationId, gangnamManager))
                .doesNotThrowAnyException();

        verify(reservationDao).delete(reservationId);
    }

    @Test
    void 다른_매장_매니저는_예약을_삭제할_수_없다() {
        long reservationId = 1L;
        Member hongdaeManager = new Member(
                11L, "hongdae@email.com", "password", "홍대매니저", Role.MANAGER, 2L);

        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId,
                        BROWN_ID,
                        LocalDate.now().plusDays(1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L,
                        1L
                ));

        assertThatThrownBy(() -> reservationService.deleteByManager(reservationId, hongdaeManager))
                .isInstanceOf(WrongStoreAccessException.class);
    }

    @Test
    void 매니저가_존재하지_않는_예약을_삭제할_수_없다() {
        long reservationId = 999L;
        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

        when(reservationDao.findReservationById(reservationId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.deleteByManager(reservationId, gangnamManager))
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

        assertThatCode(() -> reservationService.updateByManager(
                reservationId, futureDate, newTimeId, gangnamManager))
                .doesNotThrowAnyException();

        verify(reservationDao).updateById(reservationId, futureDate, newTimeId);
    }

    @Test
    void 다른_매장_매니저는_예약을_변경할_수_없다() {
        long reservationId = 1L;
        long newTimeId = 2L;
        LocalDate futureDate = LocalDate.now().plusDays(5);
        Member hongdaeManager = new Member(
                11L, "hongdae@email.com", "password", "홍대매니저", Role.MANAGER, 2L);

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

        assertThatThrownBy(() -> reservationService.updateByManager(
                reservationId, futureDate, newTimeId, hongdaeManager))
                .isInstanceOf(WrongStoreAccessException.class);
    }

    @Test
    void 예약대기를_생성한다() {
        long id = 1L;
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation(
                id,
                JEONGKONG_ID,
                futureDate,
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );
        ReservationWait reservationWait = new ReservationWait(
                id,
                1L,
                BROWN_ID,
                LocalDateTime.of(futureDate, LocalTime.of(10, 0))
        );

        when(reservationDao.findReservationById(1L)).thenReturn(reservation);
        when(reservationWaitDao.createReservationWait(BROWN_ID, 1L))
                .thenReturn(id);
        when(reservationWaitDao.findReservationWaitById(id))
                .thenReturn(Optional.of(reservationWait));
        ReservationWait result = reservationService.createWait(BROWN_ID, 1L);
        assertThat(reservationWait).usingRecursiveComparison().isEqualTo(result);
        verify(reservationWaitDao).createReservationWait(BROWN_ID, 1L);
    }

    @Test
    void 과거_날짜로는_예약대기를_생성할_수_없다() {
        long id = 1L;
        long themeId = 1L;
        long storeId = 1L;
        Reservation reservation = new Reservation(
                id,
                BROWN_ID,
                LocalDate.now().minusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                themeId,
                storeId
        );

        when(reservationDao.findReservationById(1L)).thenReturn(reservation);
        assertThatThrownBy(() -> reservationService.createWait(BROWN_ID, 1L))
                .isInstanceOf(PastReservationWaitNotAllowedException.class);
    }

    @Test
    void 같은_사용자는_같은_슬롯에_예약대기를_걸_수_없다() {
        long id = 1L;
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation(
                id,
                JEONGKONG_ID,
                futureDate,
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );

        when(reservationDao.findReservationById(1L)).thenReturn(reservation);
        when(reservationWaitDao.createReservationWait(BROWN_ID, 1L))
                .thenThrow(new DuplicateKeyException("Duplicate key exception"));
        assertThatThrownBy(() -> reservationService.createWait(BROWN_ID, 1L))
                .isInstanceOf(ReservationWaitAlreadyExistsException.class);
    }

    @Test
    void 본인_예약에는_대기를_생성할_수_없다() {
        long memberId = 1L;
        long reservationId = 1L;
        when(reservationDao.findReservationById(reservationId))
                .thenReturn(new Reservation(
                        reservationId, memberId,
                        LocalDate.now().plusDays(1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        1L, 1L));

        assertThatThrownBy(() -> reservationService.createWait(memberId, reservationId))
                .isInstanceOf(SelfReservationWaitNotAllowedException.class);
    }
}
