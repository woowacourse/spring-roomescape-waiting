package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.exception.UnauthorizedException;
import roomescape.repository.ReservationRepository;
import roomescape.service.ReservationCommandService;
import roomescape.service.ReservationQueryService;
import roomescape.service.ReservationTimeQueryService;
import roomescape.service.ThemeQueryService;

@ExtendWith(MockitoExtension.class)
class ReservationUseCaseMockTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
    private static final LocalDate FUTURE = LocalDate.of(2999, 1, 1);
    private static final LocalDate PAST = LocalDate.of(2020, 1, 1);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeQueryService reservationTimeQueryService;

    @Mock
    private ThemeQueryService themeQueryService;

    private ReservationQueryService reservationQueryService;
    private ReservationCommandService reservationCommandService;

    @BeforeEach
    void setUp() {
        reservationQueryService = new ReservationQueryService(reservationRepository);
        reservationCommandService = new ReservationCommandService(
                reservationRepository,
                reservationQueryService,
                reservationTimeQueryService,
                themeQueryService
        );
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
    void deleteMine은_본인_예약이_아니면_UnauthorizedException을_던진다() {
        Reservation reservation = new Reservation(1L, "티뉴", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationCommandService.deleteMine(1L, "민욱"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void deleteMine은_지난_예약이면_BusinessRuleViolationException을_던진다() {
        Reservation reservation = new Reservation(1L, "민욱", PAST, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationCommandService.deleteMine(1L, "민욱"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void deleteMine은_미래_예약이면_삭제를_위임한다() {
        Reservation reservation = new Reservation(1L, "민욱", FUTURE, TIME, THEME);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        reservationCommandService.deleteMine(1L, "민욱");

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
