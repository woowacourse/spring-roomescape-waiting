package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.api.dto.ReservationWaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.exception.UnauthorizedException;
import roomescape.repository.ReservationWaitingQueryRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.ReservationQueryService;
import roomescape.service.ReservationWaitingCommandService;
import roomescape.service.ReservationWaitingQueryService;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingUseCaseMockTest {

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    private ReservationWaitingQueryRepository reservationWaitingQueryRepository;

    @Mock
    private ReservationQueryService reservationQueryService;

    private ReservationWaitingQueryService reservationWaitingQueryService;
    private ReservationWaitingCommandService reservationWaitingCommandService;

    @BeforeEach
    void setUp() {
        reservationWaitingQueryService = new ReservationWaitingQueryService(
                reservationWaitingRepository,
                reservationWaitingQueryRepository
        );
        reservationWaitingCommandService = new ReservationWaitingCommandService(
                reservationWaitingRepository,
                reservationQueryService,
                reservationWaitingQueryService
        );
    }

    @Test
    void save는_저장소에_위임하고_저장된_대기를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
        Reservation reservation = new Reservation(1L, "티뉴", LocalDate.of(2026, 8, 5), time, theme);
        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", reservation.getId());
        ReservationWaiting saved = new ReservationWaiting(1L, "민욱", LocalDateTime.of(2026, 8, 1, 10, 0), reservation);
        given(reservationQueryService.getById(reservation.getId())).willReturn(reservation);
        given(reservationWaitingRepository.save(any(ReservationWaiting.class))).willReturn(saved);

        assertThat(reservationWaitingCommandService.save(request)).isEqualTo(saved);
    }

    @Test
    void getById는_존재하지_않으면_NotFoundException을_던진다() {
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingQueryService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_존재하면_대기를_반환한다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThat(reservationWaitingQueryService.getById(1L)).isEqualTo(waiting);
    }

    @Test
    void deleteMine은_본인_대기면_삭제를_위임한다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        reservationWaitingCommandService.deleteMine(1L, "민욱");

        verify(reservationWaitingRepository).deleteById(1L);
    }

    @Test
    void deleteMine은_본인_대기가_아니면_UnauthorizedException을_던진다() {
        ReservationWaiting waiting = waitingOwnedBy(1L, "민욱");
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatThrownBy(() -> reservationWaitingCommandService.deleteMine(1L, "브라운"))
                .isInstanceOf(UnauthorizedException.class);
        verify(reservationWaitingRepository, never()).deleteById(anyLong());
    }

    private ReservationWaiting waitingOwnedBy(Long id, String name) {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
        Reservation reservation = new Reservation(1L, "티뉴", LocalDate.of(2026, 8, 5), time, theme);
        return new ReservationWaiting(id, name, LocalDateTime.of(2026, 8, 1, 10, 0), reservation);
    }
}
