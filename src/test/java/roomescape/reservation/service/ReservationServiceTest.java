package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private final Theme theme = Theme.builder()
            .id(1L).name("theme name").description("theme description").thumbnailImgUrl("theme img url")
            .build();

    private final ReservationTime time1 = ReservationTime.builder()
            .id(1L).startAt(LocalTime.of(10, 0))
            .build();

    private final ReservationTime time2 = ReservationTime.builder()
            .id(2L).startAt(LocalTime.of(11, 0))
            .build();

    @DisplayName("사용자의 방탈출 예약 시간 추가를 테스트합니다.")
    @Test
    void save_user_reservation_successfully() {
        Reservation saved = Reservation.builder()
                .id(1L).name("스타크").date(LocalDate.of(2026, 5, 6)).themeId(1L).timeId(1L)
                .build();

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time1));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndThemeAndTime(any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationResponse result = reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.name()).isEqualTo("스타크");
            softly.assertThat(result.date()).isEqualTo(LocalDate.of(2026, 5, 6));
            softly.assertThat(result.timeId()).isEqualTo(1L);
            softly.assertThat(result.startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(result.themeId()).isEqualTo(1L);
            softly.assertThat(result.themeName()).isEqualTo("theme name");
        });
    }

    @DisplayName("오늘보다 이전 날짜 혹은 시간 예약 시도 시 예외 발생을 테스트합니다.")
    @Test
    void validate_throw_exception_when_reserving_past_date_or_time() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time1));

        assertThatThrownBy(() -> reservationService.save(
                new ReservationCreateCommand("스타크", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2026, 5, 6, 11, 0)))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("이름으로 본인 예약 목록 조회를 테스트합니다.")
    @Test
    void find_reservations_by_name() {
        Reservation r1 = Reservation.builder().id(1L).name("스타크").date(LocalDate.of(2026, 5, 6)).themeId(1L).timeId(1L).build();
        Reservation r2 = Reservation.builder().id(2L).name("스타크").date(LocalDate.of(2026, 5, 7)).themeId(1L).timeId(2L).build();
        Reservation r3 = Reservation.builder().id(3L).name("카야").date(LocalDate.of(2026, 5, 8)).themeId(1L).timeId(2L).build();

        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time1));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(time2));
        when(reservationRepository.findByName("스타크")).thenReturn(List.of(r1, r2));
        when(reservationRepository.findByName("카야")).thenReturn(List.of(r3));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationService.findAllByName("스타크")).hasSize(2);
            softly.assertThat(reservationService.findAllByName("카야")).hasSize(1);
        });
    }

    @DisplayName("본인 예약의 날짜와 시간을 변경합니다.")
    @Test
    void update_reservation() {
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0));
        Reservation updated = Reservation.builder().id(1L).name("스타크").date(LocalDate.of(2026, 5, 7)).themeId(1L).timeId(2L).build();

        when(reservationRepository.findDetailById(1L)).thenReturn(Optional.of(detail));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(time2));
        when(reservationRepository.existsByDateAndThemeAndTimeExcludingId(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.update(any())).thenReturn(updated);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(timeRepository.findById(2L)).thenReturn(Optional.of(time2));

        ReservationResponse result = reservationService.update(
                new ReservationUpdateCommand(1L, "스타크", LocalDate.of(2026, 5, 7), 2L),
                LocalDateTime.of(2000, 1, 1, 0, 0));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.date()).isEqualTo(LocalDate.of(2026, 5, 7));
            softly.assertThat(result.timeId()).isEqualTo(2L);
            softly.assertThat(result.startAt()).isEqualTo(LocalTime.of(11, 0));
        });
    }

    @DisplayName("본인 이름이 아닌 경우 예약 변경 시 예외가 발생합니다.")
    @Test
    void update_other_users_reservation() {
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0));

        when(reservationRepository.findDetailById(1L)).thenReturn(Optional.of(detail));

        assertThatThrownBy(() -> reservationService.update(
                new ReservationUpdateCommand(1L, "카야", LocalDate.of(2026, 5, 7), 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
    }

    @DisplayName("이미 예약된 날짜/테마/시간에 예약 요청 시 대기로 저장되어야 한다.")
    @Test
    void save_as_waiting_when_reservation_already_exists() {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time1));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndThemeAndTime(any(), any(), any())).thenReturn(true);
        when(waitingRepository.save(any())).thenReturn(
                roomescape.reservation.domain.Waiting.of(1L, "카야", LocalDate.of(2026, 5, 6), 1L, 1L));

        ReservationResponse result = reservationService.save(
                new ReservationCreateCommand("카야", LocalDate.of(2026, 5, 6), 1L, 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("카야");
            softly.assertThat(result.date()).isEqualTo(LocalDate.of(2026, 5, 6));
            softly.assertThat(result.timeId()).isEqualTo(1L);
            softly.assertThat(result.startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(result.themeId()).isEqualTo(1L);
            softly.assertThat(result.themeName()).isEqualTo("theme name");
        });
    }

    @DisplayName("지난 예약은 취소할 수 없습니다.")
    @Test
    void cancel_past_reservation() {
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0));

        when(reservationRepository.findDetailById(1L)).thenReturn(Optional.of(detail));

        assertThatThrownBy(() -> reservationService.delete(1L, "스타크", LocalDateTime.of(2026, 5, 6, 11, 0)))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("지난 예약은 변경하거나 취소할 수 없습니다.");
    }
}
