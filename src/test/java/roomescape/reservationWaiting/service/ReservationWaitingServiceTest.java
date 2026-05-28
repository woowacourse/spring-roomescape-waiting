package roomescape.reservationWaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.AuthorizationException;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.exception.AlreadyReservedSameSlotException;
import roomescape.reservationWaiting.exception.DuplicateReservationWaitingException;
import roomescape.reservationWaiting.exception.ReservationWaitingNotFoundException;
import roomescape.reservationWaiting.exception.ReservationWaitingTargetNotFoundException;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @Mock
    ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @Mock
    ThemeRepository themeRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    Clock clock;

    @InjectMocks
    ReservationWaitingService reservationWaitingService;

    @DisplayName("새로운 에약 대기를 신청한다.")
    @Test
    void makeReservationWaitingTest() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(time));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        when(themeRepository.findById(any()))
                .thenReturn(Optional.of(theme));

        when(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).thenReturn(Optional.of(new Reservation(1L, "포비", LocalDate.of(2026, 5, 15), time, theme)));

        when(reservationWaitingRepository.save(any()))
                .thenReturn(new ReservationWaiting(1L, "브라운", LocalDate.of(2026, 5, 15), time, theme));

        //when
        ReservationWaiting reservationWaiting = reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        );

        //then
        assertThat(reservationWaiting.getId()).isNotNull();
    }

    @DisplayName("예약 대기 생성 시, 기존에 이미 동일한 예약 대기가 있으면 예외가 발생한다.")
    @Test
    void makeReservationWaitingTest_duplicate() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(DuplicateReservationWaitingException.class);
    }

    @DisplayName("예약 대기 생성 시, timeId에 해당하는 시간이 없으면 예외가 발생한다.")
    @Test
    void makeReservationWaitingTest_no_timeId() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(false);

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약 대기 생성 시, themeId에 해당하는 테마가 없으면 예외가 발생한다.")
    @Test
    void makeReservationWaitingTest_no_themId() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(false);

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(themeRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("예약 대기 생성 시, 해당 슬롯의 예약이 없으면 예외가 발생한다.")
    @Test
    void makeReservationWaitingTest_no_reservation() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(false);

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(themeRepository.findById(any()))
                .thenReturn(Optional.of(new Theme(1L, "이름", "설명", "thumbnailUrl")));

        when(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(ReservationWaitingTargetNotFoundException.class);
    }

    @DisplayName("예약 대기 생성 시, 해당 슬롯으로 자신이 예약을 했었다면 예외가 발생한다.")
    @Test
    void makeReservationWaitingTest_already_reserved() {
        //given
        when(reservationWaitingRepository.existByDateAndTimeIdAndThemeIdAndName(any(), any(), any(), anyString()))
                .thenReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(time));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        when(themeRepository.findById(any()))
                .thenReturn(Optional.of(theme));

        when(reservationRepository.findByDateAndTimeIdAndThemeId(
                any(), any(), any())
        ).thenReturn(Optional.of(new Reservation(1L, "브라운", LocalDate.of(2026, 5, 15), time, theme)));

        //when & then
        assertThatThrownBy(() -> reservationWaitingService.makeReservationWaiting(
                new ReservationWaitingCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(AlreadyReservedSameSlotException.class);
    }

    @Test
    @DisplayName("예약 대기를 아이디 기반으로 잘 삭제한다")
    void deleteReservationWaiting_ById_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        when(reservationWaitingRepository.findById(any())).thenReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 1), time, theme
                )));
        when(reservationWaitingRepository.deleteById(any())).thenReturn(0);
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when, then
        assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaitingById(1L, "브라운")).isInstanceOf(
                ReservationWaitingNotFoundException.class);
    }

    @Test
    @DisplayName("예약 대기 삭제 대상이 없는 경우 예외가 발생한다")
    void deleteReservationWaiting_ById_fail_not_exist() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        when(reservationWaitingRepository.findById(any())).thenReturn(
                Optional.empty());

        // when, then
        assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaitingById(1L, "브라운")).isInstanceOf(
                ReservationWaitingNotFoundException.class);
    }

    @Test
    @DisplayName("예약 대기가 유효한 지 검증한다-과거이면 오류 발생")
    void deleteReservationWaiting_ById_past() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        when(reservationWaitingRepository.findById(any())).thenReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 1), time, theme
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when,then
        assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaitingById(1L, "브라운")).isInstanceOf(
                InvalidReservationDateValueException.class);
    }

    @Test
    @DisplayName("예약 대기가 유효한 지 검증한다- 미래 오류 발생 X")
    void deleteReservationWaiting_ById_future() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        when(reservationWaitingRepository.findById(any())).thenReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 14), time, theme
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(reservationWaitingRepository.deleteById(any())).thenReturn(1);

        // when,then
        assertDoesNotThrow(
                () -> reservationWaitingService.deleteReservationWaitingById(1L, "브라운")
        );
    }

    @Test
    @DisplayName("예약 대기자와 삭제 신청자 이름이 다르면 오류가 발생한다")
    void deleteReservationWaiting_ById_not_authorized() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        when(reservationWaitingRepository.findById(any())).thenReturn(
                Optional.of(new ReservationWaiting(
                        1L, "브라운", LocalDate.of(2026, 5, 14), time, theme
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // when,then
        assertThatThrownBy(() -> reservationWaitingService.deleteReservationWaitingById(1L, "검프")).isInstanceOf(
                AuthorizationException.class);
    }
}
