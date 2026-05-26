package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.auth.Role;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.PastReservationCancelNotAllowedException;
import roomescape.exception.PastReservationNotAllowedException;
import roomescape.exception.ReservationAlreadyExistsException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationOwnerMismatchException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.WrongStoreAccessException;

public class ReservationServiceTest {

    private static final Long BROWN_ID = 1L;
    private static final Long JEONGKONG_ID = 2L;

    private ReservationDao reservationDao;
    private ReservationTimeDao reservationTimeDao;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationDao = mock(ReservationDao.class);
        reservationTimeDao = mock(ReservationTimeDao.class);
        reservationService = new ReservationService(reservationDao, reservationTimeDao);
    }

    @Test
    void 존재하지않는_예약은_삭제할_수_없다() {
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
    void 예약자가_일치하지_않으면_예약을_변경할_수_없다() {
        Long reservationId = 1L;
        Long timeId = 2L;
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
    void 존재하지않는_예약시간으로는_예약을_변경할_수_없다() {
        Long reservationId = 1L;
        Long timeId = 999L;
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
        Long reservationId = 1L;
        Long timeId = 2L;
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
    void 존재하지않는_예약은_변경할_수_없다() {
        Long reservationId = 1L;
        Long timeId = 2L;
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
        Long timeId = 1L;
        Long reservationId = 2L;
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
        Long reservationId = 1L;
        Long storeId = 1L;
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
        Long reservationId = 1L;
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
    void 존재하지_않는_예약을_매니저가_삭제하면_ReservationNotFoundException() {
        Long reservationId = 999L;
        Member gangnamManager = new Member(
                10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

        when(reservationDao.findReservationById(reservationId))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> reservationService.deleteByManager(reservationId, gangnamManager))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void findByStoreId는_DAO에_위임한다() {
        Long storeId = 1L;
        Reservation reservation = new Reservation(
                1L,
                BROWN_ID,
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                storeId
        );
        when(reservationDao.findByStoreId(storeId)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.findByStoreId(storeId);

        assertThat(result).hasSize(1);
        verify(reservationDao).findByStoreId(storeId);
    }

    @Test
    void 매니저가_자기_매장_예약을_변경할_수_있다() {
        Long reservationId = 1L;
        Long newTimeId = 2L;
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
        Long reservationId = 1L;
        Long newTimeId = 2L;
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
}
