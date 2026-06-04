package roomescape.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationwait.exception.ReservationWaitAlreadyExistsException;

public class ReservationWaitServiceTest {

    private static final long BROWN_ID = 1L;
    private static final long JEONGKONG_ID = 2L;

    private ReservationDao reservationDao;
    private ReservationWaitDao reservationWaitDao;
    private ReservationWaitService reservationWaitService;

    @BeforeEach
    void setUp() {
        reservationDao = mock(ReservationDao.class);
        reservationWaitDao = mock(ReservationWaitDao.class);
        reservationWaitService = new ReservationWaitService(reservationWaitDao, reservationDao);
    }

    @Test
    void 예약대기를_생성한다() {
        // given: 정콩이 예약, DAO stub 세팅
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
        when(reservationWaitDao.insert(any(ReservationWait.class)))
                .thenReturn(reservationWait);

        // when: BROWN 대기 신청
        ReservationWait result = reservationWaitService.createReservationWait(BROWN_ID, 1L);

        // then: 반환값 + DAO 호출 검증
        assertThat(reservationWait).usingRecursiveComparison().isEqualTo(result);
        verify(reservationWaitDao).insert(argThat(w ->
                w.getMemberId().equals(BROWN_ID) && w.getReservationId().equals(1L)));
    }

    @Test
    void 같은_사용자는_같은_슬롯에_예약대기를_걸_수_없다() {
        // given: 정콩이 예약, DAO 가 중복 예외 던지도록 stub
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
        when(reservationWaitDao.insert(any(ReservationWait.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key exception"));

        // when & then: Service 가 도메인 예외로 변환
        assertThatThrownBy(() -> reservationWaitService.createReservationWait(BROWN_ID, 1L))
                .isInstanceOf(ReservationWaitAlreadyExistsException.class);
    }
}
