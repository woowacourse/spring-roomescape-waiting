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
import roomescape.common.exception.ConflictException;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
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
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = mockTime();

        Theme mockTheme = mockTheme();

        Reservation mockReservation = mockReservation(Status.WAITING, mockTime, mockTheme);

        when(reservationTimeRepository.getById(command.timeId()))
                .thenReturn(mockTime);
        when(themeRepository.getById(command.themeId()))
                .thenReturn(mockTheme);
        when(reservationRepository.existsActiveReservationByDateTimeAndTheme(command.timeId(), command.themeId(), command.date()))
                .thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        Assertions.assertThat(reservationService.create(command).status()).isEqualTo(Status.WAITING);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("변경하려는 시간에 Active인 예약이 있고, 시간을 변경하면 정상 동작한다.")
    void normalPendingTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = mockTime();

        Theme mockTheme = mockTheme();

        Reservation mockReservation = mockReservation(Status.RESERVED, mockTime, mockTheme);


        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsActiveReservationByDateTimeAndTheme(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date()))
                .thenReturn(true);

        when(reservationRepository.existsByUsernameAndDateTimeAndTheme(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date(),
                changeCommand.username()))
                .thenReturn(false);

        ReservationInfo reservationInfo = reservationService.modify(
                mockReservation.getId(),
                changeCommand);
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.WAITING);
        verify(reservationRepository, times(1)).update(any(Reservation.class));
    }

    @Test
    @DisplayName("Active인 예약이 존재하지 않으면 Reserved 상태로 변경한다.")
    void notFoundReservedReservationTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = mockTime();

        Theme mockTheme = mockTheme();

        Reservation mockReservation = mockReservation(Status.RESERVED, mockTime, mockTheme);

        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsActiveReservationByDateTimeAndTheme(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date()))
                .thenReturn(false);

        ReservationInfo reservationInfo = reservationService.modify(mockReservation.getId(), changeCommand);

        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.RESERVED);
        verify(reservationRepository, times(1)).update(any(Reservation.class));
    }

    @Test
    @DisplayName("동일 날짜와 시간대에 같은 이름의 예약이 존재하면 예외를 발생한다.")
    void duplicatedTest() {
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username("포비")
                .themeId(1L)
                .timeId(1L)
                .date(LocalDate.now(clock).plusDays(1))
                .build();

        ReservationTime mockTime = mockTime();

        Theme mockTheme = mockTheme();

        Reservation mockReservation = mockReservation(Status.RESERVED, mockTime, mockTheme);

        when(reservationTimeRepository.getById(1L)).thenReturn(mockTime);
        when(themeRepository.getById(1L)).thenReturn(mockTheme);
        when(reservationRepository.getById(1L)).thenReturn(mockReservation);

        when(reservationRepository.existsByUsernameAndDateTimeAndTheme(
                changeCommand.timeId(),
                changeCommand.themeId(),
                changeCommand.date(),
                changeCommand.username()))
                .thenReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.modify(mockReservation.getId(), changeCommand))
                .isInstanceOf(ConflictException.class);
    }

    private ReservationTime mockTime() {
        return ReservationTime.restore(1L, LocalTime.now(clock).plusHours(1), true);
    }

    private Theme mockTheme() {
        return Theme.restore(1L, "판타지", "https://example.com/theme.png", "설명", true);
    }

    private Reservation mockReservation(Status status, ReservationTime time, Theme theme) {
        return Reservation.restore(
                1L,
                "포비",
                LocalDate.now(clock).plusDays(1),
                time,
                theme,
                status,
                LocalDateTime.now(clock)
        );
    }
}
