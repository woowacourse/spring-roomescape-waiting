package roomescape.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dto.ReservationChangeRequest;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ThemeDao themeDao;

    @Mock
    private TimeDao timeDao;

    @Mock
    private ReservationWaitingDao reservationWaitingDao;

    @InjectMocks
    private ReservationService reservationService;

    // ===================== findAll =====================

    @Test
    void 전체_예약_조회_성공() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        List<Reservation> reservations = List.of(
                new Reservation(1L, "초록", 1L, LocalDate.now().plusDays(1), time),
                new Reservation(2L, "브라운", 1L, LocalDate.now().plusDays(1), time)
        );
        given(reservationDao.selectAll()).willReturn(reservations);

        List<Reservation> actual = reservationService.findAll();

        assertThat(actual).hasSize(2);
    }

    // ===================== findById =====================

    @Test
    void id로_예약_조회_성공() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
                reservationId, "초록", 1L, LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0))
        );
        given(reservationDao.selectById(reservationId)).willReturn(Optional.of(reservation));

        Reservation actual = reservationService.findById(reservationId);

        assertThat(actual.getId()).isEqualTo(reservationId);
    }

    @Test
    void 존재하지_않는_id로_조회시_예외발생() {
        Long notFoundId = 999L;
        given(reservationDao.selectById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findById(notFoundId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    // ===================== findAllByName =====================

    @Test
    void 이름으로_예약만_조회_성공() {
        String name = "초록";
        Long themeId = 1L;
        ReservationTime time = new ReservationTime(22L, LocalTime.now().plusMinutes(3));
        List<Reservation> reservations = List.of(
                new Reservation(name, themeId, LocalDate.now(), time),
                new Reservation(name, themeId, LocalDate.now(), time)
        );
        when(reservationDao.selectByName(name)).thenReturn(reservations);

        List<Reservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(2)
                .extracting(Reservation::getName)
                .containsOnly(name);
    }

    @Test
    void 이름으로_조회한_예약이_없으면_빈_목록을_반환한다() {
        String name = "에버";
        when(reservationDao.selectByName(anyString())).thenReturn(List.of());

        List<Reservation> actual = reservationService.findAllByName(name);

        assertThat(actual).isEmpty();
    }

    // ===================== add =====================

    @Test
    void 예약_추가_성공() {
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));

        given(timeDao.selectById(timeId)).willReturn(Optional.of(time));
        given(themeDao.existsById(themeId)).willReturn(true);
        given(reservationDao.insert(any(Reservation.class)))
                .willReturn(new Reservation(10L, "초록", themeId, date, time));

        Reservation actual = reservationService.add("초록", themeId, date, timeId);

        assertThat(actual.getId()).isEqualTo(10L);
        assertThat(actual.getName()).isEqualTo("초록");
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외발생() {
        given(timeDao.selectById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now().plusDays(1), 999L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 지난_날짜및시간_예약_하는_경우_예외발생() {
        ReservationTime mockTime = new ReservationTime(17L, LocalTime.now().minusMinutes(10));
        when(timeDao.selectById(anyLong())).thenReturn(Optional.of(mockTime));

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now().minusDays(1), 1L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.PAST_RESERVATION.getMessage());

        assertThatThrownBy(() -> reservationService.add("브라운", 1L, LocalDate.now(), mockTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.PAST_RESERVATION.getMessage());
    }

    @Test
    void 존재하지_않는_테마로_예약시_예외발생() {
        Long themeId = 999L;
        Long timeId = 1L;
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));

        given(timeDao.selectById(timeId)).willReturn(Optional.of(time));
        given(themeDao.existsById(themeId)).willReturn(false);

        assertThatThrownBy(() -> reservationService.add("브라운", themeId, LocalDate.now().plusDays(1), timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_예약이_존재하는_경우_예외발생() {
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));

        given(timeDao.selectById(timeId)).willReturn(Optional.of(time));
        given(themeDao.existsById(themeId)).willReturn(true);
        given(reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)).willReturn(true);

        assertThatThrownBy(() -> reservationService.add("브라운", themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void insert중_중복키_예외발생시_예약중복_예외로_변환된다() {
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));

        given(timeDao.selectById(timeId)).willReturn(Optional.of(time));
        given(themeDao.existsById(themeId)).willReturn(true);
        given(reservationDao.insert(any(Reservation.class)))
                .willThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> reservationService.add("브라운", themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    @Test
    void 본인_예약_변경_성공() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 2L;
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(12, 0));

        Reservation originReservation = new Reservation(
                reservationId,
                name,
                themeId,
                LocalDate.now().plusDays(2),
                new ReservationTime(3L, LocalTime.of(10, 0))
        );

        given(reservationDao.lockById(reservationId))
                .willReturn(Optional.of(reservationId));
        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(originReservation));
        given(timeDao.selectById(timeId))
                .willReturn(Optional.of(time));
        given(themeDao.existsById(themeId)).willReturn(true);

        Reservation changedReservation = new Reservation(reservationId, name, themeId, date, time);
        given(reservationDao.updateDateTimeById(reservationId, date, timeId))
                .willReturn(Optional.of(changedReservation));
        given(reservationWaitingDao.lockFirstByThemeAndDateAndTime(
                originReservation.getThemeId(), originReservation.getDate(), originReservation.getTime()))
                .willReturn(Optional.empty());

        Reservation actual = reservationService.modifyDateTimeByName(reservationId, name, themeId, date, timeId);

        assertThat(actual.getId()).isEqualTo(reservationId);
        assertThat(actual.getName()).isEqualTo(name);
        assertThat(actual.getDate()).isEqualTo(date);
        assertThat(actual.getTime().getId()).isEqualTo(timeId);
    }

    @Test
    void 다른_사람의_예약은_변경_예외발생() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
                reservationId,
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );

        ReservationChangeRequest request = new ReservationChangeRequest(
                "브라운",
                1L,
                LocalDate.now().plusDays(2),
                2L
        );

        given(timeDao.selectById(request.timeId()))
                .willReturn(Optional.of(new ReservationTime(request.timeId(), LocalTime.of(12, 0))));
        given(themeDao.existsById(request.themeId())).willReturn(true);
        given(reservationDao.existsByThemeIdAndDateAndTimeId(request.themeId(), request.date(), request.timeId()))
                .willReturn(false);
        given(reservationDao.lockById(reservationId))
                .willReturn(Optional.of(reservationId));
        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_RESERVATION_ACCESS.getMessage());
    }

    @Test
    void 존재하지_않는_예약은_변경_예외발생() {
        Long notFoundId = 999L;

        ReservationChangeRequest request = new ReservationChangeRequest(
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                2L
        );

        given(timeDao.selectById(request.timeId()))
                .willReturn(Optional.of(new ReservationTime(request.timeId(), LocalTime.of(10, 0))));
        given(themeDao.existsById(request.themeId())).willReturn(true);
        given(reservationDao.existsByThemeIdAndDateAndTimeId(request.themeId(), request.date(), request.timeId()))
                .willReturn(false);
        given(reservationDao.lockById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                notFoundId,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 변경시_존재하지_않는_시간이면_예외발생() {
        Long reservationId = 1L;
        String name = "로치";

        given(timeDao.selectById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId, name, 1L, LocalDate.now().plusDays(1), 999L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
    }

    @Test
    void 변경시_지난_날짜면_예외발생() {
        Long reservationId = 1L;
        String name = "로치";
        Long timeId = 2L;

        given(timeDao.selectById(timeId))
                .willReturn(Optional.of(new ReservationTime(timeId, LocalTime.of(10, 0))));

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId, name, 1L, LocalDate.now().minusDays(1), timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.PAST_RESERVATION.getMessage());
    }

    @Test
    void 변경시_존재하지_않는_테마면_예외발생() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 999L;
        Long timeId = 2L;

        given(timeDao.selectById(timeId))
                .willReturn(Optional.of(new ReservationTime(timeId, LocalTime.of(10, 0))));
        given(themeDao.existsById(themeId)).willReturn(false);

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId, name, themeId, LocalDate.now().plusDays(1), timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 변경시_이미_예약이_존재하면_예외발생() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 1L;
        Long timeId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);

        given(timeDao.selectById(timeId))
                .willReturn(Optional.of(new ReservationTime(timeId, LocalTime.of(10, 0))));
        given(themeDao.existsById(themeId)).willReturn(true);
        given(reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)).willReturn(true);

        assertThatThrownBy(() -> reservationService.modifyDateTimeByName(
                reservationId, name, themeId, date, timeId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_ALREADY_EXISTS.getMessage());
    }

    // ===================== deleteByIdIfNameMatches =====================

    @Test
    void 본인_예약_취소_성공() {
        Long reservationId = 1L;
        String name = "로치";
        Long themeId = 1L;

        Reservation originReservation = new Reservation(
                reservationId,
                name,
                themeId,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(10, 0))
        );

        given(reservationDao.lockById(reservationId)).willReturn(Optional.of(reservationId));
        given(reservationDao.selectById(reservationId)).willReturn(Optional.of(originReservation));
        given(reservationWaitingDao.lockFirstByThemeAndDateAndTime(eq(themeId), any(LocalDate.class), any(ReservationTime.class)))
                .willReturn(Optional.empty());

        reservationService.deleteByIdIfNameMatches(reservationId, name);

        verify(reservationDao, times(1)).deleteById(reservationId);
        verify(reservationDao, never()).insert(any(Reservation.class));
    }

    @Test
    void 다른_사람의_예약은_취소_예외발생() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
                reservationId,
                "로치",
                1L,
                LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );

        given(reservationDao.lockById(reservationId))
                .willReturn(Optional.of(reservationId));
        given(reservationDao.selectById(reservationId))
                .willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(reservationId, "브라운"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_RESERVATION_ACCESS.getMessage());
    }

    @Test
    void 존재하지_않는_예약은_취소_예외발생() {
        Long notFoundId = 999L;
        given(reservationDao.lockById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(notFoundId, "로치"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    void 지난_예약은_취소_예외발생() {
        Long pastReserved = 1L;
        Reservation reservation = new Reservation(
                pastReserved,
                "로치",
                1L,
                LocalDate.now().minusDays(1),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );

        given(reservationDao.lockById(pastReserved)).willReturn(Optional.of(pastReserved));
        given(reservationDao.selectById(pastReserved)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteByIdIfNameMatches(pastReserved, "로치"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.PAST_RESERVATION.getMessage());
    }

    // ===================== deleteById (관리자) =====================

    @Test
    void 관리자_예약_취소_성공_대기없으면_단순삭제() {
        Long reservationId = 1L;
        Long themeId = 1L;
        Reservation origin = new Reservation(
                reservationId, "로치", themeId, LocalDate.now().plusDays(1),
                new ReservationTime(3L, LocalTime.of(10, 0))
        );
        given(reservationDao.lockById(reservationId)).willReturn(Optional.of(reservationId));
        given(reservationDao.selectById(reservationId)).willReturn(Optional.of(origin));
        given(reservationWaitingDao.lockFirstByThemeAndDateAndTime(eq(themeId), any(LocalDate.class), any(ReservationTime.class)))
                .willReturn(Optional.empty());

        reservationService.deleteById(reservationId);

        verify(reservationDao, times(1)).deleteById(reservationId);
        verify(reservationDao, never()).insert(any(Reservation.class));
    }

    @Test
    void 존재하지_않는_예약은_관리자취소_예외발생() {
        Long notFoundId = 999L;
        given(reservationDao.lockById(notFoundId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteById(notFoundId))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }
}
