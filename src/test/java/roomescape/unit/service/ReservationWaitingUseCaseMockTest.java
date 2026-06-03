package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static roomescape.fixture.ReservationFixture.reservation;
import static roomescape.fixture.ReservationFixture.slot;
import static roomescape.fixture.ReservationFixture.waiting;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.api.dto.ReservationWaitingRequest;
import roomescape.application.ReservationWaitingApplicationService;
import roomescape.application.command.ReservationWaitingCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ReservationWaitingQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.exception.ForbiddenException;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.repository.ReservationWaitingQueryRepository;
import roomescape.repository.ReservationWaitingRepository;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingUseCaseMockTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-05T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 8, 5);
    private static final ReservationTime RESERVATION_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
    private static final Slot SLOT = slot(RESERVATION_DATE, RESERVATION_TIME, THEME);
    private static final LocalDateTime WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    private ReservationWaitingQueryRepository reservationWaitingQueryRepository;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ReservationTimeQueryService reservationTimeQueryService;

    @Mock
    private ThemeQueryService themeQueryService;

    private ReservationWaitingQueryService reservationWaitingQueryService;
    private ReservationWaitingCommandService reservationWaitingCommandService;
    private ReservationWaitingApplicationService reservationWaitingApplicationService;

    @BeforeEach
    void setUp() {
        reservationWaitingQueryService = new ReservationWaitingQueryService(
                reservationWaitingRepository,
                reservationWaitingQueryRepository
        );
        reservationWaitingCommandService = new ReservationWaitingCommandService(
                reservationWaitingRepository,
                FIXED_CLOCK
        );
        reservationWaitingApplicationService = new ReservationWaitingApplicationService(
                reservationWaitingCommandService,
                reservationWaitingQueryService,
                reservationQueryService,
                reservationTimeQueryService,
                themeQueryService
        );
    }

    @Test
    void save는_저장소에_위임하고_저장된_대기를_반환한다() {
        Reservation reservation = reservation(1L, "티뉴", SLOT);
        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                RESERVATION_DATE,
                RESERVATION_TIME.getId(),
                THEME.getId()
        );
        ReservationWaiting saved = waiting(1L, "민욱", reservation.getSlot(), WAITING_CREATED_AT);
        ReservationWaitingWithOrder savedWithOrder = new ReservationWaitingWithOrder(
                saved.getId(),
                saved.getName(),
                saved.getSlot().date(),
                saved.getSlot().time(),
                saved.getSlot().theme(),
                1
        );
        given(reservationTimeQueryService.getById(RESERVATION_TIME.getId())).willReturn(RESERVATION_TIME);
        given(themeQueryService.getById(THEME.getId())).willReturn(THEME);
        given(reservationQueryService.findBySlot(SLOT))
                .willReturn(Optional.of(reservation));
        given(reservationWaitingRepository.save(any(ReservationWaiting.class))).willReturn(saved);
        given(reservationWaitingQueryRepository.findById(saved.getId())).willReturn(Optional.of(savedWithOrder));

        assertThat(reservationWaitingApplicationService.save(request)).isEqualTo(savedWithOrder);
    }

    @Test
    void save는_예약되지_않은_슬롯이면_ConflictException을_던진다() {
        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                RESERVATION_DATE,
                RESERVATION_TIME.getId(),
                THEME.getId()
        );
        given(reservationTimeQueryService.getById(RESERVATION_TIME.getId())).willReturn(RESERVATION_TIME);
        given(themeQueryService.getById(THEME.getId())).willReturn(THEME);
        given(reservationQueryService.findBySlot(SLOT))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingApplicationService.save(request))
                .isInstanceOf(ConflictException.class);
        verify(reservationWaitingRepository, never()).save(any(ReservationWaiting.class));
    }

    @Test
    void getById는_존재하지_않으면_NotFoundException을_던진다() {
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingQueryService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_존재하면_대기를_반환한다() {
        ReservationWaiting waiting = waiting(1L, "민욱", SLOT, WAITING_CREATED_AT);
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThat(reservationWaitingQueryService.getById(1L)).isEqualTo(waiting);
    }

    @Test
    void deleteMine은_본인_대기면_삭제를_위임한다() {
        ReservationWaiting waiting = waiting(1L, "민욱", SLOT, WAITING_CREATED_AT);
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        reservationWaitingApplicationService.deleteMine(1L, "민욱");

        verify(reservationWaitingRepository).deleteById(1L);
    }

    @Test
    void deleteMine은_본인_대기가_아니면_ForbiddenException을_던진다() {
        ReservationWaiting waiting = waiting(1L, "민욱", SLOT, WAITING_CREATED_AT);
        given(reservationWaitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatThrownBy(() -> reservationWaitingApplicationService.deleteMine(1L, "브라운"))
                .isInstanceOf(ForbiddenException.class);
        verify(reservationWaitingRepository, never()).deleteById(anyLong());
    }
}
