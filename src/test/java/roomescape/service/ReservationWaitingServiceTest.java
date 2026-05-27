package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationWaitingQueryingDao;
import roomescape.repository.ReservationWaitingUpdatingDao;
import roomescape.repository.ThemeQueryingDao;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    private ReservationWaitingUpdatingDao reservationWaitingUpdatingDao;

    @Mock
    private ReservationWaitingQueryingDao reservationWaitingQueryingDao;

    @Mock
    private ReservationQueryingDao reservationQueryingDao;

    @Mock
    private ReservationTimeQueryingDao reservationTimeQueryingDao;

    @Mock
    private ThemeQueryingDao themeQueryingDao;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    private final static LocalDate now = LocalDate.now().plusDays(1);
    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "test", "설명", "url");

    @Test
    void 예약_대기열이_정상_생성된다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId(reservationWaitingRequest.name(), reservationWaitingRequest.date(), reservationWaitingRequest.timeId(), reservationWaitingRequest.timeId())).thenReturn(false);
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(reservationWaitingRequest.themeId(), reservationWaitingRequest.date(), reservationWaitingRequest.timeId())).thenReturn(
                Optional.of(new Reservation("test2", now, reservationTime, theme))
        );

        ReservationWaitingResponse reservationWaitingResponse = reservationWaitingService.create(reservationWaitingRequest);

        assertThat(reservationWaitingResponse.getName()).isEqualTo("테스트");
    }

    @Test
    void 잘못된_시간_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("test", LocalDate.now(), 999L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 잘못된_테마_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("test", LocalDate.now(), 1L, 999L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 타겟_예약이_과거인_경우_예외가_발생한다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("test",
                pastDate, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 예약이_존재하지_않는데_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(reservationWaitingRequest.themeId(), reservationWaitingRequest.date(), reservationWaitingRequest.timeId())).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약이_존재하는데_같은_이름으로_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(1L, now, 1L))
                .thenReturn(Optional.of(new Reservation("테스트", now, reservationTime, theme)));

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "테스트", now, reservationTime, theme, 1L, LocalDateTime.now());
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(reservationWaiting));

        assertThatCode(() -> reservationWaitingService.delete(reservationWaiting.getId())).doesNotThrowAnyException();
    }

    @Test
    void 과거_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "테스트", LocalDate.now().minusDays(1), reservationTime, theme, 1L, LocalDateTime.now());
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(reservationWaiting));

        assertThatThrownBy(() -> reservationWaitingService.delete(reservationWaiting.getId()))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 존재하지_않는_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 중복_예약_대기열_생성_시도하면_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(reservationWaitingRequest.themeId(), reservationWaitingRequest.date(), reservationWaitingRequest.timeId())).thenReturn(
                Optional.of(new Reservation("test2", now, reservationTime, theme))
        );
        when(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId(reservationWaitingRequest.name(), reservationWaitingRequest.date(), reservationWaitingRequest.timeId(), reservationWaitingRequest.timeId())).thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest)).isInstanceOf(
                InvalidInputException.class);

    }

}
