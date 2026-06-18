package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.common.config.FixedClockConfig;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.infrastructure.SlotManager;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationDetailResults;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.event.ReservationChangeEvent;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    private final String name = "브라운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 1L;
    private final Theme theme = new Theme(themeId, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));
    private final EventSlot eventSlot = new EventSlot(futureDate, time, theme);
    private final Clock clock = new FixedClockConfig().testClock();

    private ReservationService reservationService;

    @Mock
    private ReservationDao reservationDao;
    @Mock
    private ReservationTimeDao reservationTimeDao;
    @Mock
    private ThemeDao themeDao;
    @Mock
    private WaitingDao waitingDao;
    @Mock
    private ReservationRejectLogger reservationRejectLogger;
    @Mock
    private SlotManager slotManager;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationDao, reservationTimeDao, themeDao, waitingDao,
                reservationRejectLogger, slotManager, eventPublisher, clock);
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void registerReservation_Success() {
        ReservationCommand command = new ReservationCommand(name, futureDate, timeId, themeId);
        Reservation confirmed = Reservation.restoreConfirmed(1L, UserName.parse(name), eventSlot);

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(slotManager.tryAcquire(any(EventSlot.class))).willReturn(true);
        given(reservationDao.save(any(Reservation.class))).willReturn(confirmed);

        ReservationResult saved = reservationService.registerReservation(command);
        System.out.println(saved);
        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo(name);
        assertThat(saved.date()).isEqualTo(futureDate);
        assertThat(saved.timeResult().id()).isEqualTo(timeId);
        assertThat(saved.themeResult().id()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("존재하지 않는 시간으로 예약하면 예외가 발생한다.")
    void registerReservation_WhenNonExistTime_ThrowsNotFoundException() {
        Long invalidTimeId = 999L;
        ReservationCommand command = new ReservationCommand(name, futureDate, invalidTimeId, themeId);

        given(reservationTimeDao.findById(invalidTimeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다.")
    void registerReservation_WhenNonExistTheme_ThrowsNotFoundException() {
        Long invalidThemeId = 999L;
        ReservationCommand command = new ReservationCommand(name, futureDate, timeId, invalidThemeId);

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(invalidThemeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 테마입니다.");
    }

    @Test
    @DisplayName("이미 예약된 슬롯으로 예약하면 예외가 발생한다.")
    void registerReservation_WhenAlreadyBookedSlot_ThrowsConflictException() {
        ReservationCommand command = new ReservationCommand(name, futureDate, timeId, themeId);

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(slotManager.tryAcquire(any(EventSlot.class))).willReturn(false);

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("다른 사용자가 예약했습니다. 다시 시도해주세요.");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void findReservations_Success() {
        given(reservationDao.findAll()).willReturn(
                List.of(Reservation.restore(1L, UserName.parse(name), futureDate, time, theme,
                        ReservationStatus.CONFIRMED)));

        List<ReservationResult> reservations = reservationService.findReservations();

        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("사용자 이름으로 예약과 대기를 조회한다.")
    void findReservationsByUserName_Success() {
        given(reservationDao.findByUserName(name)).willReturn(
                List.of(Reservation.restore(1L, UserName.parse(name), futureDate, time, theme,
                        ReservationStatus.CONFIRMED)));
        given(waitingDao.findByUserName(name)).willReturn(Collections.emptyList());

        ReservationDetailResults results = reservationService.findReservationsByUserName(name);

        assertThat(results.reservationResults()).hasSize(1);
        assertThat(results.waitingResults()).isEmpty();
    }

    @Test
    @DisplayName("예약을 변경하고 대기 승인 이벤트를 발행한다.")
    void changeDateTimeReservation_Success() {
        LocalDate newDate = futureDate.plusDays(1);
        Long reservationId = 1L;
        Reservation originReservation = Reservation.restore(reservationId, UserName.parse(name), futureDate, time,
                theme,
                ReservationStatus.CONFIRMED);
        ReservationCommand command = new ReservationCommand(name, newDate, time.getId(), theme.getId());
        EventSlot modifiedEventSlot = new EventSlot(newDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(originReservation));
        given(reservationTimeDao.findById(command.timeId())).willReturn(Optional.of(time));
        given(slotManager.tryAcquire(modifiedEventSlot)).willReturn(true);
        given(reservationDao.update(any(Reservation.class))).willReturn(true);

        ReservationResult result = reservationService.changeDateTime(reservationId, command);

        then(slotManager).should(times(1)).tryAcquire(modifiedEventSlot);
        then(reservationDao).should(times(1)).update(any(Reservation.class));
        then(eventPublisher).should(times(1)).publishEvent(any(ReservationChangeEvent.class));

        assertThat(result.date()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("동시에 같은 테마로 변경하면 예외가 발생한다.")
    void changeDateTimeReservation_WhenConflict_ThrowsConflictException() {
        LocalDate newDate = futureDate.plusDays(1);
        Long reservationId = 1L;
        Reservation originReservation = Reservation.restore(reservationId, UserName.parse(name), futureDate, time,
                theme,
                ReservationStatus.CONFIRMED);
        ReservationCommand command = new ReservationCommand(name, newDate, time.getId(), theme.getId());
        EventSlot modifiedEventSlot = new EventSlot(newDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(originReservation));
        given(reservationTimeDao.findById(command.timeId())).willReturn(Optional.of(time));
        given(slotManager.tryAcquire(modifiedEventSlot)).willReturn(false);

        assertThatThrownBy(() -> reservationService.changeDateTime(reservationId, command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("다른 사용자가 예약했습니다. 다시 시도해주세요.");

        then(reservationDao).should(never()).update(any());
        then(eventPublisher).should(never()).publishEvent(any());
    }

    @Test
    @DisplayName("예약을 삭제하고 대기 승인 이벤트를 발행한다.")
    void deleteReservation_Success() {
        Long reservationId = 1L;
        Reservation reservation = Reservation.restore(reservationId, UserName.parse(name), futureDate, time, theme,
                ReservationStatus.CONFIRMED);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(reservation));

        assertDoesNotThrow(() -> reservationService.deleteReservation(reservationId, name));

        then(reservationDao).should(times(1)).update(any(Reservation.class));
        then(eventPublisher).should(times(1)).publishEvent(any(ReservationChangeEvent.class));
    }
}
