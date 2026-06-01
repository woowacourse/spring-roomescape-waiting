package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.api.dto.ReservationRequest;
import roomescape.application.ReservationApplicationService;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;
import roomescape.domain.exception.NotFoundException;
import roomescape.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationUseCaseMockTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-05T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);
    private static final LocalDate FUTURE = TODAY.plusDays(1);
    private static final LocalDate PAST = TODAY.minusDays(1);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeQueryService reservationTimeQueryService;

    @Mock
    private ThemeQueryService themeQueryService;

    private ReservationQueryService reservationQueryService;
    private ReservationCommandService reservationCommandService;
    private ReservationApplicationService reservationApplicationService;

    @BeforeEach
    void setUp() {
        reservationQueryService = new ReservationQueryService(reservationRepository);
        reservationCommandService = new ReservationCommandService(
                reservationRepository,
                FIXED_CLOCK
        );
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
                        TODAY,
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
                TODAY,
                currentTime.getId(),
                THEME.getId()
        );
        Reservation saved = new Reservation(1L, request.name(), request.date(), currentTime, THEME);
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
        Reservation reservation = new Reservation(1L, "민욱", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThat(reservationQueryService.getById(1L)).isEqualTo(reservation);
    }

    @Test
    void deleteMine은_본인_예약이_아니면_ForbiddenException을_던진다() {
        Reservation reservation = new Reservation(1L, "티뉴", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationApplicationService.deleteMine(1L, "민욱"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteMine은_지난_예약이면_BusinessRuleViolationException을_던진다() {
        Reservation reservation = new Reservation(1L, "민욱", PAST, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationApplicationService.deleteMine(1L, "민욱"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void deleteMine은_미래_예약이면_삭제를_위임한다() {
        Reservation reservation = new Reservation(1L, "민욱", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        reservationApplicationService.deleteMine(1L, "민욱");

        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void findPage는_page와_size로_offset을_계산해_조회한다() {
        given(reservationRepository.findAll(10, 5)).willReturn(List.of());
        given(reservationRepository.count()).willReturn(0L);

        reservationQueryService.findPage(2, 5);

        verify(reservationRepository).findAll(10, 5);
    }
}
