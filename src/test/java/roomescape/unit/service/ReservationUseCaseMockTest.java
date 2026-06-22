package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static roomescape.fixture.ReservationFixture.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.ReservationApplicationService;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.config.FixedClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.NotFoundException;
import roomescape.presentation.dto.ReservationRequest;

@ExtendWith(MockitoExtension.class)
class ReservationUseCaseMockTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeQueryService reservationTimeQueryService;

    @Mock
    private ThemeQueryService themeQueryService;

    private ReservationQueryService reservationQueryService;
    private ReservationCommandService reservationCommandService;
    private ReservationApplicationService reservationApplicationService;
    private LocalDate today;
    private LocalDate future;

    @BeforeEach
    void setUp() {
        Clock fixedClock = new FixedClockConfig().fixedClock();
        today = LocalDate.now(fixedClock);
        future = today.plusDays(1);
        reservationQueryService = new ReservationQueryService(reservationRepository);
        reservationCommandService = new ReservationCommandService(reservationRepository, fixedClock);
        reservationApplicationService = new ReservationApplicationService(
                reservationCommandService,
                reservationQueryService,
                reservationTimeQueryService,
                themeQueryService
        );
    }

    @Test
    void save는_현재_시각_직전_예약이면_BusinessRuleViolationException을_던진다() {
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.of(9, 59));
        given(reservationTimeQueryService.getById(pastTime.getId())).willReturn(pastTime);
        given(themeQueryService.getById(THEME.getId())).willReturn(THEME);

        assertThatThrownBy(() -> reservationApplicationService.save(
                new ReservationRequest(
                        "민욱",
                        today,
                        pastTime.getId(),
                        THEME.getId()
                )
        )).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void save는_현재_시각과_같은_예약을_과거로_판단하지_않는다() {
        ReservationTime currentTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationRequest request = new ReservationRequest(
                "민욱",
                today,
                currentTime.getId(),
                THEME.getId()
        );
        Reservation saved = reservation(1L, request.name(), request.date(), currentTime, THEME);
        given(reservationTimeQueryService.getById(currentTime.getId())).willReturn(currentTime);
        given(themeQueryService.getById(THEME.getId())).willReturn(THEME);
        given(reservationRepository.save(any(Reservation.class))).willReturn(saved);

        assertThat(reservationApplicationService.save(request)).isEqualTo(saved);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void getById는_예약이_없으면_NotFoundException을_던진다() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationQueryService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_예약이_있으면_예약을_반환한다() {
        Reservation reservation = reservation(1L, "민욱", future, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThat(reservationQueryService.getById(1L)).isEqualTo(reservation);
    }

    @Test
    void findPage는_page와_size로_offset을_계산해_조회한다() {
        given(reservationRepository.findAll(10, 5)).willReturn(List.of());
        given(reservationRepository.count()).willReturn(0L);

        reservationQueryService.findPage(2, 5);

        verify(reservationRepository).findAll(10, 5);
    }
}
