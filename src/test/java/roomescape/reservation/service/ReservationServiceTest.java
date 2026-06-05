package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;

import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;
import roomescape.theme.service.support.FakeThemeRepository;
import roomescape.waiting.service.support.FakeWaitingRepository;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);
    private static final Theme SAVED_THEME = Theme.of(1L, "링", "공포 테마", "http:~");
    private static final String CUSTOMER_NAME = "코로구";

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingRepository waitingRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = new FakeWaitingRepository();
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                waitingRepository,
                FIXED_CLOCK
        );
    }

    @Test
    void 예약_일정을_수정한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));

        // when
        Reservation reservation = reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 8, 6), 2L
        );

        // then
        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getCustomerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(reservation.getTime().getId()).isEqualTo(2L);
        assertThat(reservation.getTheme().getId()).isEqualTo(1L);
        assertThat(reservationRepository.findById(1L).get().getTime().getId()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_예약을_수정하면_예외가_발생한다() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 8, 5), 1L
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 존재하지_않는_예약_시간으로_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 8, 5), 999L
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 현재_이전_시간으로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(2L, LocalTime.of(11, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 5, 8), 1L
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약_일정을_수정할_수_없다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 5, 9), 2L
        ))
                .isInstanceOf(ReservationModificationException.class);
    }

    @Test
    void 관리자는_예약일_당일에도_예약_일정을_수정할_수_있다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when
        Reservation reservation = reservationService.updateByAdmin(
                1L,
                LocalDate.of(2026, 5, 9), 2L
        );

        // then
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 9));
        assertThat(reservation.getTime().getId()).isEqualTo(2L);
    }

    @Test
    void 이미_예약된_시간으로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 8, 6), 2L
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    void 예약_옵션이_변경된_상태로_예약_일정을_수정하면_예외가_발생한다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                LocalDate.of(2026, 8, 6), 2L
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    void 예약_가능_날짜와_테마를_조회한다() {
        // given
        themeRepository.add(SAVED_THEME);

        // when
        ReservationOptionResponse response = reservationService.getReservationOptions();

        // then
        assertThat(response.dates()).hasSize(14);
        assertThat(response.dates().getFirst()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(response.dates().getLast()).isEqualTo(LocalDate.of(2026, 5, 21));
        assertThat(response.themes())
                .extracting(theme -> theme.name())
                .containsExactly("링");
    }

    @Test
    void 존재하지_않는_예약을_취소하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_취소할_수_없다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(ReservationCancellationException.class);
    }

    @Test
    void 관리자는_예약일_당일에도_예약을_삭제할_수_있다() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
            CUSTOMER_NAME,
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
            SAVED_THEME
        ));

        // when
        reservationService.deleteById(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_관리자가_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
