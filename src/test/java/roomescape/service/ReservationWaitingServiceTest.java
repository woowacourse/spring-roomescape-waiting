package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.repository.ReservationWaitingSequence;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationWaitingQueryDao;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.repository.ThemeQueryDao;

@ExtendWith(MockitoExtension.class)
public class ReservationWaitingServiceTest {

    @Mock
    private ReservationWaitingUpdateDao reservationWaitingUpdatingDao;

    @Mock
    private ReservationWaitingQueryDao reservationWaitingQueryingDao;

    @Mock
    private ReservationQueryDao reservationQueryingDao;

    @Mock
    private ReservationTimeQueryDao reservationTimeQueryingDao;

    @Mock
    private ThemeQueryDao themeQueryingDao;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    private final static LocalDate now = LocalDate.now().plusDays(1);
    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "test", "설명", "url");

    @Test
    void 예약_대기열이_정상_생성된다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);
        ReservationWaiting createdWaiting = new ReservationWaiting(1L, "테스트", new ReservationSlot(now, reservationTime, theme), LocalDateTime.now());

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationWaitingQueryingDao.isExistByNameAndSlot(reservationWaitingRequest.name(), new ReservationSlot(reservationWaitingRequest.date(), reservationTime, theme))).thenReturn(false);
        when(reservationQueryingDao.findReservationBySlotForUpdate(new ReservationSlot(reservationWaitingRequest.date(), reservationTime, theme))).thenReturn(
                Optional.of(new Reservation("test2", new ReservationSlot(now, reservationTime, theme)))
        );
        when(reservationWaitingUpdatingDao.create(any())).thenReturn(1L);
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(new ReservationWaitingSequence(createdWaiting, 1L)));

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

        Reservation pastReservation = new Reservation(1L, "다른사람", new ReservationSlot(pastDate, reservationTime, theme), LocalDateTime.now());

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlotForUpdate(new ReservationSlot(pastDate, reservationTime, theme))).thenReturn(Optional.of(pastReservation));

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 예약이_존재하지_않는데_대기열에_추가를_시도하면_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlotForUpdate(new ReservationSlot(reservationWaitingRequest.date(), reservationTime, theme))).thenReturn(
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
        when(reservationQueryingDao.findReservationBySlotForUpdate(new ReservationSlot(now, reservationTime, theme)))
                .thenReturn(Optional.of(new Reservation("테스트", new ReservationSlot(now, reservationTime, theme))));

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 예약_대기열이_정상_삭제된다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "테스트", new ReservationSlot(now, reservationTime, theme), LocalDateTime.now());
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(new ReservationWaitingSequence(reservationWaiting, 1L)));

        assertThatCode(() -> reservationWaitingService.delete(reservationWaiting.getId(), "테스트")).doesNotThrowAnyException();
    }

    @Test
    void 과거_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(1L, "테스트", new ReservationSlot(LocalDate.now().minusDays(1), reservationTime, theme), LocalDateTime.now());
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.of(new ReservationWaitingSequence(reservationWaiting, 1L)));

        assertThatThrownBy(() -> reservationWaitingService.delete(reservationWaiting.getId(), "테스트"))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 존재하지_않는_예약_대기열_삭제를_시도하면_예외가_발생한다() {
        when(reservationWaitingQueryingDao.findReservationWaitingById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.delete(1L, "테스트"))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    void 중복_예약_대기열_생성_시도하면_예외가_발생한다() {
        ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트", now, 1L, 1L);

        when(reservationTimeQueryingDao.findReservationTimeById(reservationWaitingRequest.timeId())).thenReturn(Optional.of(reservationTime));
        when(themeQueryingDao.findThemeById(reservationWaitingRequest.themeId())).thenReturn(Optional.of(theme));
        when(reservationQueryingDao.findReservationBySlotForUpdate(new ReservationSlot(reservationWaitingRequest.date(), reservationTime, theme))).thenReturn(
                Optional.of(new Reservation("test2", new ReservationSlot(now, reservationTime, theme)))
        );
        when(reservationWaitingQueryingDao.isExistByNameAndSlot(reservationWaitingRequest.name(), new ReservationSlot(reservationWaitingRequest.date(), reservationTime, theme))).thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.create(reservationWaitingRequest)).isInstanceOf(
                InvalidInputException.class);
    }
}
