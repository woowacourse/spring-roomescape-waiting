package roomescape.reservation.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import roomescape.reservation.event.schema.WaitingSaved;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.exception.BadRequestException;
import roomescape.reservation.application.exception.ConflictException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WaitingService waitingService;

    private final ReservationTime time = ReservationTime.builder()
            .id(1L).startAt(LocalTime.of(10, 0))
            .build();

    private final Theme theme = Theme.builder()
            .id(1L).name("테마").description("설명").thumbnailImgUrl("img.jpg")
            .build();

    @DisplayName("대기를 성공적으로 저장하고 이벤트를 발행한다.")
    @Test
    void save_successfully() {
        Waiting saved = Waiting.of(1L, "카야", LocalDate.of(2028, 5, 6), 1L, 1L);

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByNameAndDateAndThemeAndTime(any(), any(), any(), any())).thenReturn(false);
        when(waitingRepository.existsByNameAndDateAndThemeIdAndTimeId(any(), any(), any(), any())).thenReturn(false);
        when(waitingRepository.save(any())).thenReturn(saved);

        waitingService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2028, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0));

        verify(waitingRepository).save(any());
        verify(eventPublisher).publishEvent(any(WaitingSaved.class));
    }

    @DisplayName("지난 날짜/시간으로 대기 요청 시 예외가 발생한다.")
    @Test
    void save_throws_on_past_datetime() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));

        assertThatThrownBy(() -> waitingService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2026, 5, 6, 11, 0)))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @DisplayName("동일 이름/날짜/테마/시간으로 중복 대기 요청 시 예외가 발생한다.")
    @Test
    void save_throws_on_duplicate_waiting() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByNameAndDateAndThemeAndTime(any(), any(), any(), any())).thenReturn(false);
        when(waitingRepository.existsByNameAndDateAndThemeIdAndTimeId(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2028, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)))
                .isExactlyInstanceOf(ConflictException.class);
    }

    @DisplayName("이미 예약이 있는 슬롯에 대기 신청 시 예외가 발생한다.")
    @Test
    void save_throws_when_reservation_already_exists() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByNameAndDateAndThemeAndTime(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> waitingService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2028, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)))
                .isExactlyInstanceOf(ConflictException.class);
    }

    @DisplayName("대기 취소를 할 수 있어야 한다.")
    @Test
    void cancel_user_waiting() {
        Long id = 1L;
        Waiting waiting = Waiting.of(id, "타스", LocalDate.of(2026, 5, 27), 1L, 1L);

        when(waitingRepository.findById(id)).thenReturn(Optional.of(waiting));
        when(waitingRepository.delete(id)).thenReturn(1);

        waitingService.delete(id, "타스");

        verify(waitingRepository).delete(id);
    }

    @DisplayName("취소하려는 대기 ID의 예약자 명이 다르다면 예외를 발생시켜야 한다.")
    @Test
    void exception_when_name_is_unmatched() {
        Long id = 1L;
        Waiting waiting = Waiting.of(id, "타스", LocalDate.of(2026, 5, 27), 1L, 1L);

        when(waitingRepository.findById(id)).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> waitingService.delete(id, "카야"))
                .isExactlyInstanceOf(RoomEscapeException.class)
                .hasMessage(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS.message());
    }
}
