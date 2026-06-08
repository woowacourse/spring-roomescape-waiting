package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationSlotDuplicateException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.domain.exception.WaitingExistsForSlotException;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
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
            FIXED_CLOCK
        );
        final WaitingPromotionService waitingPromotionService = new WaitingPromotionService(
            new WaitingService(waitingRepository, FIXED_CLOCK),
            reservationService
        );
        new ReservationTimeService(reservationTimeRepository);
        reservationApplicationService = new ReservationApplicationService(
            reservationService,
            waitingPromotionService,
            new ReservationTimeService(reservationTimeRepository),
            new ThemeService(themeRepository, FIXED_CLOCK),
            new WaitingService(waitingRepository, FIXED_CLOCK),
            FIXED_CLOCK
        );
    }

    @Nested
    @DisplayName("예약자 이름으로 예약 및 대기 목록을 조회한다")
    class FindReservationsAndWaitings {

        private static final String CUSTOMER_NAME = "코로구";

        @Test
        void 현재_시간_이후의_예약과_대기만_조회된다() {
            // given
            final LocalDateTime oneHourBefore = NOW.minusHours(1);
            final LocalDateTime oneHourAfter = NOW.plusHours(1);

            reservationRepository.add(ReservationFixture.saved(
                1L, CUSTOMER_NAME, oneHourBefore.toLocalDate(),
                ReservationTimeFixture.saved(1L, oneHourBefore.toLocalTime()), THEME
            ));
            reservationRepository.add(ReservationFixture.saved(
                2L, CUSTOMER_NAME, oneHourAfter.toLocalDate(),
                ReservationTimeFixture.saved(2L, oneHourAfter.toLocalTime()), THEME
            ));
            waitingRepository.add(WaitingFixture.saved(1L, CUSTOMER_NAME,
                oneHourBefore.toLocalDate(), NOW,
                ReservationTimeFixture.saved(1L, oneHourBefore.toLocalTime()), THEME
            ));
            waitingRepository.add(WaitingFixture.saved(2L, CUSTOMER_NAME,
                oneHourAfter.toLocalDate(), NOW,
                ReservationTimeFixture.saved(2L, oneHourAfter.toLocalTime()), THEME
            ));

            // when
            final ReservationsAndWaitingsResponse responses =
                reservationApplicationService.findReservationsAndWaitingsByCustomerName(CUSTOMER_NAME);

            // then
            assertThat(responses.reservations()).hasSize(1);
            assertThat(responses.reservations().getFirst().name()).isEqualTo(CUSTOMER_NAME);
            assertThat(responses.waitings()).hasSize(1);
            assertThat(responses.waitings().getFirst().customerName()).isEqualTo(CUSTOMER_NAME);
        }

        @Test
        void 대기_순번은_같은_슬롯_내_생성일자_순서이다() {
            // given
            final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
            final LocalDate dayAfterTomorrow = NOW.plusDays(2).toLocalDate();

            waitingRepository.add(WaitingFixture.saved(1L, "other", tomorrow, NOW, TIME, THEME));
            waitingRepository.add(WaitingFixture.saved(2L, CUSTOMER_NAME, tomorrow, NOW.plusMinutes(1), TIME, THEME));
            waitingRepository.add(WaitingFixture.saved(3L, CUSTOMER_NAME, dayAfterTomorrow, NOW, TIME, THEME));

            // when
            final ReservationsAndWaitingsResponse responses =
                reservationApplicationService.findReservationsAndWaitingsByCustomerName(CUSTOMER_NAME);

            // then
            assertThat(responses.waitings()).hasSize(2);
            assertThat(responses.waitings().get(0).customerName()).isEqualTo(CUSTOMER_NAME);
            assertThat(responses.waitings().get(0).rank()).isEqualTo(2);
            assertThat(responses.waitings().get(1).customerName()).isEqualTo(CUSTOMER_NAME);
            assertThat(responses.waitings().get(1).rank()).isEqualTo(1);
        }

        @Test
        void 대기_순위는_같은_슬롯_내_createdAt_순서로_계산된다() {
            // given
            final LocalDate futureDate = NOW.plusDays(1).toLocalDate();

            waitingRepository.add(WaitingFixture.saved(1L, "크루", futureDate, NOW.minusMinutes(2), TIME, THEME));
            waitingRepository.add(WaitingFixture.saved(2L, "재키", futureDate, NOW.minusMinutes(1), TIME, THEME));
            waitingRepository.add(WaitingFixture.saved(3L, CUSTOMER_NAME, futureDate, NOW, TIME, THEME));

            // when
            final ReservationsAndWaitingsResponse responses =
                reservationApplicationService.findReservationsAndWaitingsByCustomerName(CUSTOMER_NAME);

            // then
            assertThat(responses.waitings()).hasSize(1);
            assertThat(responses.waitings().getFirst().rank()).isEqualTo(3);
        }
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
            final ReservationTime pastTime = ReservationTimeFixture.saved(1L, NOW.minusMinutes(1).toLocalTime());

            final Reservation reservation = ReservationFixture.saved(1L, "name", today, pastTime, THEME);
            reservationRepository.add(reservation);

            waitingRepository.add(WaitingFixture.saved(1L, "코로구", today, NOW.minusHours(2), pastTime, THEME));

            // when
            reservationApplicationService.deleteReservationById(reservation.getId());

            // then
            assertThat(reservationRepository.savedReservation()).isNull();
        }
    }

    @Nested
    @DisplayName("예약을 생성한다")
    class CreateReservation {

        @Test
        void 예약을_생성한다() {
            // given
            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                TIME.getId(),
                THEME.getId()
            );

            // when
            final ReservationResponse response = reservationApplicationService.create(request);

            // then
            assertThat(response.id()).isNotNull();
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.date()).isEqualTo(request.date());
            assertThat(response.time().id()).isEqualTo(request.timeId());
            assertThat(response.theme().id()).isEqualTo(request.themeId());

            assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo(request.name());
        }

        @Test
        void 현재_이전_시간으로_예약하면_예외가_발생한다() {
            // given
            final ReservationTime beforeOneHour = ReservationTimeFixture.saved(2L, NOW.minusHours(1).toLocalTime());
            reservationTimeRepository.add(beforeOneHour);

            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                beforeOneHour.getId(),
                THEME.getId()
            );

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("과거 시간");
        }

        @Test
        void 존재하지_않는_예약_시간으로_예약하면_예외가_발생한다() {
            // given
            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                999L,
                THEME.getId()
            );

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(ReservationTimeNotFoundException.class);
        }

        @Test
        void 존재하지_않는_테마로_예약하면_예외가_발생한다() {
            // given
            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                TIME.getId(),
                999L
            );

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(ThemeNotFoundException.class);
        }

        @Test
        void 이미_예약된_시간으로_예약하면_예외가_발생한다() {
            // given
            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                TIME.getId(),
                THEME.getId()
            );

            reservationRepository.failToSaveByDuplicatedReservation();

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(ReservationSlotDuplicateException.class);
        }

        @Test
        void 예약_시도_시점에_선택한_시간이나_테마가_삭제되면_예외가_발생한다() {
            // given
            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                NOW.toLocalDate(),
                TIME.getId(),
                THEME.getId()
            );

            reservationRepository.failToSaveByChangedOption();

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(ReservationOptionChangedException.class);
        }

        @Test
        void 대기가_있는_슬롯에_예약하면_예외가_발생한다() {
            // given
            final LocalDateTime reservationDateTime = NOW.plusDays(1);
            waitingRepository.add(WaitingFixture.saved(
                reservationDateTime.toLocalDate(),
                NOW,
                TIME,
                THEME
            ));

            final ReservationCreateRequest request = new ReservationCreateRequest(
                "name",
                reservationDateTime.toLocalDate(),
                TIME.getId(),
                THEME.getId()
            );

            // when & then
            assertThatThrownBy(() -> reservationApplicationService.create(request))
                .isInstanceOf(WaitingExistsForSlotException.class);
        }
    }
}
