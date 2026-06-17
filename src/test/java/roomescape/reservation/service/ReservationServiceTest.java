package roomescape.reservation.service;

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
import roomescape.global.BadRequestException;
import roomescape.global.ForbiddenException;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
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
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0), ReservationStatus.CONFIRMED, 30000L);
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
        });
    }

    @DisplayName("본인 이름이 아닌 경우 예약 변경 시 예외가 발생합니다.")
    @Test
    void update_other_users_reservation() {
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0), ReservationStatus.CONFIRMED, 30000L);

        when(reservationRepository.findDetailById(1L)).thenReturn(Optional.of(detail));

        assertThatThrownBy(() -> reservationService.update(
                new ReservationUpdateCommand(1L, "카야", LocalDate.of(2026, 5, 7), 1L),
                LocalDateTime.of(2000, 1, 1, 0, 0)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
    }

    @DisplayName("지난 예약은 취소할 수 없습니다.")
    @Test
    void cancel_past_reservation() {
        ReservationDetail detail = new ReservationDetail(1L, "스타크", LocalDate.of(2026, 5, 6), 1L, "theme name", "theme description", "theme img url", 1L, LocalTime.of(10, 0), ReservationStatus.CONFIRMED, 30000L);

        when(reservationRepository.findDetailById(1L)).thenReturn(Optional.of(detail));

        assertThatThrownBy(() -> reservationService.delete(1L, "스타크", LocalDateTime.of(2026, 5, 6, 11, 0)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("지난 예약은 변경하거나 취소할 수 없습니다.");
    }
}
