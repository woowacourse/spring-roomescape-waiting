package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);
    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(2L, "test", "설명", "url");

    @Test
    void 예약_대기열이_정상_생성된다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(2L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(2L, tomorrow, 1L)).thenReturn(
                Optional.of(Reservation.create("test2", tomorrow, reservationTime, theme))
        );
        when(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId("테스트", tomorrow, 1L, 2L)).thenReturn(false);
        when(reservationWaitingUpdatingDao.create(any())).thenReturn(1L);
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(
                Optional.of(ReservationWaiting.restore(1L, "테스트", tomorrow, reservationTime, theme, 1L, LocalDateTime.now()))
        );

        ReservationWaitingResponse response = reservationWaitingService.create(request);

        assertThat(response.name()).isEqualTo("테스트");
        verify(reservationWaitingQueryingDao).isExistByNameAndDateAndTimeIdAndThemeId("테스트", tomorrow, 1L, 2L);
        verify(reservationQueryingDao).findReservationByThemeAndDateAndTime(2L, tomorrow, 1L);
    }

    @Test
    void 잘못된_시간_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("test", tomorrow, 999L, 2L);

        when(reservationTimeQueryingDao.findReservationTimeById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.create(request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 잘못된_테마_id를_넣은_경우_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("test", tomorrow, 1L, 999L);

        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.create(request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 예약이_존재하지_않는데_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(2L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(2L, tomorrow, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 예약이_존재하는데_같은_이름으로_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(2L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(2L, tomorrow, 1L))
                .thenReturn(Optional.of(Reservation.create("테스트", tomorrow, reservationTime, theme)));

        assertThatThrownBy(() -> reservationWaitingService.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 중복_예약_대기열_생성_시도하면_예외가_발생한다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest("테스트", tomorrow, 1L, 2L);

        when(reservationTimeQueryingDao.findReservationTimeById(1L)).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(2L)).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationByThemeAndDateAndTime(2L, tomorrow, 1L)).thenReturn(
                Optional.of(Reservation.create("test2", tomorrow, reservationTime, theme))
        );
        when(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId("테스트", tomorrow, 1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.create(request))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        ReservationWaiting reservationWaiting = ReservationWaiting.restore(1L, "테스트", tomorrow, reservationTime, theme, 1L, LocalDateTime.now());
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(reservationWaiting));

        assertThatCode(() -> reservationWaitingService.delete(1L)).doesNotThrowAnyException();
    }

    @Test
    void 존재하지_않는_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
