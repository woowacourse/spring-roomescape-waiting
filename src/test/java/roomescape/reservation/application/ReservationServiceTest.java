package roomescape.reservation.application;

import static org.mockito.ArgumentMatchers.any;
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
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

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
        when(reservationRepository.existsPendingReservationByName(command.timeId(), command.themeId(), command.date(), command.name()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        Assertions.assertThat(reservationService.addReservation(command).status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("변경하려는 시간에 Active인 예약이 있고, 시간을 변경하면 정상 동작한다.")
    void normalPendingTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
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
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();


        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsActiveReservationByThemeAndTime(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date()))
                .thenReturn(true);

        when(reservationRepository.existsPendingReservationByName(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date(),
                changeCommand.username()))
                .thenReturn(false);

        ReservationInfo reservationInfo = reservationService.changeReservationPendingStatus(
                mockReservation.getId(),
                changeCommand);
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("Active인 예약이 존재하지 않으면 예외를 발생한다.")
    void notFoundActiveReservationTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
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
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();

        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsActiveReservationByThemeAndTime(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date()))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() -> reservationService.changeReservationPendingStatus(mockReservation.getId(), changeCommand))
                .isInstanceOf(IllegalStateReservationException.class);
    }

    @Test
    @DisplayName("active인 예약이 존재하고, 이미 대기중이라면 예외를 발생한다.")
    void duplicatedTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
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
                .status(Status.ACTIVE)
                .date(LocalDate.now(clock))
                .time(mockTime)
                .theme(mockTheme)
                .createdAt(LocalDateTime.now(clock))
                .build();

        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsActiveReservationByThemeAndTime(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date()))
                .thenReturn(true);

        when(reservationRepository.existsPendingReservationByName(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date(),
                changeCommand.username()))
                .thenReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.changeReservationPendingStatus(mockReservation.getId(), changeCommand))
                .isInstanceOf(DuplicatedReservationException.class);
    }
}
