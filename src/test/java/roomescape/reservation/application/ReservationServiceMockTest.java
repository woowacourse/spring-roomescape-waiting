package roomescape.reservation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.exception.DuplicatedReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceMockTest {

    @Mock
    private Clock clock;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setClock() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-05-26T16:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    @Test
    @DisplayName("이미 예약이 존재하는 곳에 새로운 예약을 추가하면 Pending 상태로 추가된다.")
    void addPendingReservationTest() {
        ReservationCreateCommand command = ReservationCreateCommand.builder()
                .name("포비")
                .timeId(1L)
                .themeId(1L)
                .date(LocalDate.now(clock))
                .build();

        ReservationTime mockTime = ReservationTime.builder()
                .id(1L)
                .startAt(LocalTime.now(clock))
                .build();

        Theme mockTheme = Theme.builder()
                .id(1L)
                .name("판타지")
                .description("설명")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build();

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .name("포비")
                .status(Status.PENDING)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();

        when(reservationTimeRepository.getById(command.timeId()))
                .thenReturn(mockTime);
        when(themeRepository.getById(command.themeId()))
                .thenReturn(mockTheme);
        when(reservationRepository.existsByReservationTimeAndThemeAndDate(command.timeId(), command.themeId(), command.date()))
                .thenReturn(true);
        when(reservationRepository.existsReservationByName(command.timeId(), command.themeId(), command.date(), command.name()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        Assertions.assertThat(reservationService.addReservation(command).status()).isEqualTo(Status.PENDING);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("변경하려는 시간에 이미 예약이 존재하면 Pending 상태로 변경된다.")
    void normalPendingTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = ReservationTime.builder()
                .id(1L)
                .startAt(LocalTime.now(clock))
                .build();

        Theme mockTheme = Theme.builder()
                .id(1L)
                .name("판타지")
                .description("설명")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build();

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .name("포비")
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();


        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsByReservationTimeAndThemeAndDateAndIdNot(
                mockReservation.getId(), changeCommand.timeId(), changeCommand.themeId(), changeCommand.date()))
                .thenReturn(true);
        when(reservationRepository.existsReservationByName(
                changeCommand.timeId(), changeCommand.themeId(), changeCommand.date(), changeCommand.name()))
                .thenReturn(false);

        ReservationInfo reservationInfo = reservationService.changeReservation(mockReservation.getId(), changeCommand);
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.PENDING);
        verify(reservationRepository, times(1)).updateDetails(eq(mockReservation.getId()), any(Reservation.class));
    }

    @Test
    @DisplayName("변경하려는 시간에 예약이 없으면 Active 상태로 변경된다.")
    void changeToActiveTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = ReservationTime.builder()
                .id(1L)
                .startAt(LocalTime.now(clock))
                .build();

        Theme mockTheme = Theme.builder()
                .id(1L)
                .name("판타지")
                .description("설명")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build();

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .name("포비")
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();

        when(reservationRepository.getById(1L)).thenReturn(mockReservation);
        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);

        when(reservationRepository.existsByReservationTimeAndThemeAndDateAndIdNot(
                mockReservation.getId(), changeCommand.timeId(), changeCommand.themeId(), changeCommand.date()))
                .thenReturn(false);

        ReservationInfo reservationInfo = reservationService.changeReservation(mockReservation.getId(), changeCommand);

        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.ACTIVE);
        verify(reservationRepository, times(1)).updateDetails(eq(mockReservation.getId()), any(Reservation.class));
    }

    @Test
    @DisplayName("변경하려는 시간에 이미 예약이 존재하고, 본인이 이미 대기 중이라면 예외가 발생한다.")
    void duplicatedTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = ReservationTime.builder()
                .id(1L)
                .startAt(LocalTime.now(clock))
                .build();

        Theme mockTheme = Theme.builder()
                .id(1L)
                .name("판타지")
                .description("설명")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build();

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .name("포비")
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();

        when(reservationRepository.getById(1L)).thenReturn(mockReservation);
        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);

        when(reservationRepository.existsByReservationTimeAndThemeAndDateAndIdNot(
                mockReservation.getId(), changeCommand.timeId(), changeCommand.themeId(), changeCommand.date()))
                .thenReturn(true);

        when(reservationRepository.existsReservationByName(
                changeCommand.timeId(), changeCommand.themeId(), changeCommand.date(), changeCommand.name()))
                .thenReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.changeReservation(mockReservation.getId(), changeCommand))
                .isInstanceOf(DuplicatedReservationException.class)
                .hasMessage("이미 예약 중입니다.");
    }
}
