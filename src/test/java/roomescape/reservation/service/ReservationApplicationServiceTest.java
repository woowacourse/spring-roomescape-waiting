package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.WaitingFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.support.FakeThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.support.FakeWaitingRepository;

class ReservationApplicationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        LocalDate.of(2026, 5, 8)
            .atTime(10, 30)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant(),
        ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);
    private static final ReservationTime TIME = ReservationTimeFixture.SAVED;
    private static final Theme THEME = ThemeFixture.SAVED;

    FakeReservationRepository reservationRepository;
    FakeWaitingRepository waitingRepository;
    FakeReservationTimeRepository reservationTimeRepository;
    FakeThemeRepository themeRepository;
    ReservationApplicationService reservationApplicationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        waitingRepository = new FakeWaitingRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();

        reservationTimeRepository.add(TIME);
        themeRepository.add(THEME);

        final ReservationService reservationService = new ReservationService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository,
            waitingRepository,
            FIXED_CLOCK
        );
        final WaitingPromotionService waitingPromotionService = new WaitingPromotionService(
            new WaitingService(waitingRepository, FIXED_CLOCK),
            reservationService
        );
        reservationApplicationService = new ReservationApplicationService(
            reservationService,
            waitingPromotionService,
            FIXED_CLOCK
        );
    }

    @Nested
    @DisplayName("예약 취소시 대기를 승격한다")
    class CancelReservationAndPromoteWaiting {

        @Test
        void 예약_취소_시_해당_슬롯의_가장_빠른_대기가_예약으로_전환된다() {
            // given
            final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
            final Reservation reservation = ReservationFixture.saved(1L, "name", tomorrow, TIME, THEME);
            reservationRepository.add(reservation);

            final Waiting firstWaiting = WaitingFixture.saved(1L, "코로구", tomorrow, NOW.minusMinutes(2), TIME, THEME);
            final Waiting secondWaiting = WaitingFixture.saved(2L, "재키", tomorrow, NOW.minusMinutes(1), TIME, THEME);
            waitingRepository.add(firstWaiting);
            waitingRepository.add(secondWaiting);

            // when
            reservationApplicationService.cancelReservationByIdAndPromoteWaiting(reservation.getId());

            // then
            final String reservationCustomerName = reservationRepository.savedReservation().getCustomerName();
            assertThat(reservationCustomerName).isEqualTo(firstWaiting.getCustomerNameValue());
        }

        @Test
        void 예약_취소_시_대기가_없으면_예약만_삭제된다() {
            // given
            final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
            final Reservation reservation = ReservationFixture.saved(1L, "name", tomorrow, TIME, THEME);
            reservationRepository.add(reservation);

            // when
            reservationApplicationService.cancelReservationByIdAndPromoteWaiting(reservation.getId());

            // then
            assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
            assertThat(reservationRepository.savedReservation()).isNull();
        }
    }

    @Nested
    @DisplayName("관리자의 예약 삭제시 대기를 승격한다")
    class DeleteReservationAndPromoteWaiting {

        @Test
        void 오늘_이후의_예약_삭제_시_해당_슬롯의_가장_빠른_대기가_예약으로_전환된다() {
            // given
            final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
            final Reservation reservation = ReservationFixture.saved(1L, "name", tomorrow, TIME, THEME);
            reservationRepository.add(reservation);

            final Waiting firstWaiting = WaitingFixture.saved(1L, "코로구", tomorrow, NOW.minusMinutes(2), TIME, THEME);
            final Waiting secondWaiting = WaitingFixture.saved(2L, "재키", tomorrow, NOW.minusMinutes(1), TIME, THEME);
            waitingRepository.add(firstWaiting);
            waitingRepository.add(secondWaiting);

            // when
            reservationApplicationService.deleteReservationById(reservation.getId());

            // then
            final String reservationCustomerName = reservationRepository.savedReservation().getCustomerName();
            assertThat(reservationCustomerName).isEqualTo(firstWaiting.getCustomerNameValue());
        }

        @Test
        void 오늘_이후의_예약_삭제_시_대기가_없으면_예약만_삭제된다() {
            // given
            final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
            final Reservation reservation = ReservationFixture.saved(1L, "name", tomorrow, TIME, THEME);
            reservationRepository.add(reservation);

            // when
            reservationApplicationService.deleteReservationById(reservation.getId());

            // then
            assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
            assertThat(reservationRepository.savedReservation()).isNull();
        }

        @Test
        void 과거_슬롯의_예약_삭제_시_대기가_예약으로_전환되지_않는다() {
            // given
            final LocalDate today = NOW.toLocalDate();
            final ReservationTime pastTime = ReservationTimeFixture.savedWith(1L, NOW.minusMinutes(1).toLocalTime());

            final Reservation reservation = ReservationFixture.saved(1L, "name", today, pastTime, THEME);
            reservationRepository.add(reservation);

            waitingRepository.add(WaitingFixture.saved(1L, "코로구", today, NOW.minusHours(2), pastTime, THEME));

            // when
            reservationApplicationService.deleteReservationById(reservation.getId());

            // then
            assertThat(reservationRepository.savedReservation()).isNull();
        }
    }
}
