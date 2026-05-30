package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;

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
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.config.FixedClockConfig;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.infrastructure.SlotManager;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationDetailResults;
import roomescape.service.dto.result.ReservationResult;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    private final String name = "브라운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 1L;
    private final Theme theme = new Theme(themeId, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));
    private final Slot slot = new Slot(futureDate, time, theme);
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
    private SlotManager slotManager;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationDao, reservationTimeDao, themeDao, waitingDao,
                slotManager, clock);
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void registerReservation() {
        ReservationCommand command = new ReservationCommand(name, futureDate, timeId, themeId);
        Reservation reservation = new Reservation(1L, UserName.parse(name), futureDate, time, theme);

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(slotManager.tryAcquire(any(Slot.class))).willReturn(true);
        given(reservationDao.save(any(Reservation.class))).willReturn(reservation);

        ReservationResult saved = reservationService.registerReservation(command);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo(name);
        assertThat(saved.date()).isEqualTo(futureDate);
        assertThat(saved.timeResult().id()).isEqualTo(timeId);
        assertThat(saved.themeResult().id()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("존재하지 않는 시간으로 예약하면 예외가 발생한다.")
    void registerReservationWithNonExistentTime() {
        Long invalidTimeId = 999L;
        ReservationCommand command = new ReservationCommand(name, futureDate, invalidTimeId, themeId);

        given(reservationTimeDao.findById(invalidTimeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 시간입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다.")
    void registerReservationWithNonExistentTheme() {
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
    void registerReservationWithAlreadyBookedSlot() {
        ReservationCommand command = new ReservationCommand(name, futureDate, timeId, themeId);

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(slotManager.tryAcquire(any(Slot.class))).willReturn(false);

        assertThatThrownBy(() -> reservationService.registerReservation(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 존재하는 예약 건입니다.");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void findReservations() {
        given(reservationDao.findAll()).willReturn(
                List.of(new Reservation(1L, UserName.parse(name), futureDate, time, theme)));

        List<ReservationResult> reservations = reservationService.findReservations();

        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("사용자 이름으로 예약과 대기를 조회한다.")
    void findReservationsByUserName() {
        given(reservationDao.findByUserName(name)).willReturn(
                List.of(new Reservation(1L, UserName.parse(name), futureDate, time, theme)));
        given(waitingDao.findByUserName(name)).willReturn(Collections.emptyList());

        ReservationDetailResults results = reservationService.findReservationsByUserName(name);

        assertThat(results.reservationResults()).hasSize(1);
        assertThat(results.waitingResults()).isEmpty();
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        Long reservationId = 1L;
        assertDoesNotThrow(() -> reservationService.deleteReservation(reservationId));
    }
}
