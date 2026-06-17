package roomescape.feature.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.global.domain.EntityStatus;
import roomescape.global.error.exception.GeneralException;

class ReservationTest {

    private static final ReserverName DEFAULT_RESERVER_NAME = new ReserverName("예약자");
    private static final ReserverName OTHER_RESERVER_NAME = new ReserverName("다른예약자");
    private static final Time DEFAULT_TIME = TimeFixture.VALID_10_00.createInstance();
    private static final Theme DEFAULT_THEME = ThemeFixture.VALID.createInstance();
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    private static final LocalDate PAST_DATE = LocalDate.now().minusYears(1);
    private static final long DEFAULT_AMOUNT = ReservationFixture.DEFAULT_AMOUNT;

    @Nested
    class 생성한다 {

        @Test
        void 미래_일정으로_생성하면_정상_생성된다() {
            assertThatNoException().isThrownBy(() ->
                    Reservation.create(DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME,
                            ReservationStatus.ACTIVE, DEFAULT_AMOUNT)
            );
        }

        @Test
        void 과거_일정으로_생성하면_예외를_던진다() {
            assertThatThrownBy(() ->
                    Reservation.create(DEFAULT_RESERVER_NAME, PAST_DATE, DEFAULT_TIME, DEFAULT_THEME,
                            ReservationStatus.ACTIVE, DEFAULT_AMOUNT)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 생성할 수 없습니다");
        }

        @Test
        void WAITING_상태로_생성하면_WAITING_상태의_예약이_반환된다() {
            Reservation reservation = Reservation.create(DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME,
                    DEFAULT_THEME, ReservationStatus.WAITING, DEFAULT_AMOUNT);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
        }
    }

    @Nested
    class 수정한다 {

        private Reservation activeReservation() {
            return ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);
        }

        @Test
        void 정상_조건이면_수정된_예약이_반환된다() {
            Reservation reservation = activeReservation();
            Time newTime = TimeFixture.VALID_15_30.createInstance();
            Theme newTheme = ThemeFixture.VALID_ANOTHER.createInstance();

            assertThatNoException().isThrownBy(() ->
                    reservation.update(DEFAULT_RESERVER_NAME, FUTURE_DATE, newTime, newTheme)
            );
        }

        @Test
        void 날짜_시간_테마가_모두_그대로이면_예외를_던진다() {
            Time time = Time.reconstruct(1L, DEFAULT_TIME.getStartAt(), EntityStatus.ACTIVE);
            Theme theme = Theme.reconstruct(1L, "테마 이름", "테마 설명", "https://example.com/theme.png",
                    EntityStatus.ACTIVE);
            Reservation reservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, time, theme, ReservationStatus.ACTIVE);

            assertThatThrownBy(() ->
                    reservation.update(DEFAULT_RESERVER_NAME, FUTURE_DATE, time, theme)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("변경할 내용이 없습니다.");
        }

        @Test
        void 예약자명이_다르면_예외를_던진다() {
            Reservation reservation = activeReservation();

            assertThatThrownBy(() ->
                    reservation.update(OTHER_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 변경할 권한이 없습니다.");
        }

        @Test
        void ACTIVE가_아닌_상태이면_예외를_던진다() {
            Reservation waitingReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThatThrownBy(() ->
                    waitingReservation.update(DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("활성된 예약이 아닙니다.");
        }

        @Test
        void 현재_일정이_과거이면_예외를_던진다() {
            Reservation pastReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, PAST_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.ACTIVE);

            assertThatThrownBy(() ->
                    pastReservation.update(DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 생성할 수 없습니다");
        }

        @Test
        void 새_일정이_과거이면_예외를_던진다() {
            Reservation reservation = activeReservation();

            assertThatThrownBy(() ->
                    reservation.update(DEFAULT_RESERVER_NAME, PAST_DATE, DEFAULT_TIME, DEFAULT_THEME)
            ).isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 생성할 수 없습니다");
        }
    }

    @Nested
    class 취소한다 {

        @Test
        void 정상_조건이면_CANCELED_상태의_예약이_반환된다() {
            Reservation reservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            assertThat(reservation.cancelActive(DEFAULT_RESERVER_NAME).getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        void 예약자명이_다르면_예외를_던진다() {
            Reservation reservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            assertThatThrownBy(() -> reservation.cancelActive(OTHER_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 취소할 권한이 없습니다.");
        }

        @Test
        void ACTIVE가_아닌_상태이면_예외를_던진다() {
            Reservation waitingReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThatThrownBy(() -> waitingReservation.cancelActive(DEFAULT_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("활성된 예약이 아닙니다.");
        }

        @Test
        void 과거_일정이면_예외를_던진다() {
            Reservation pastReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, PAST_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.ACTIVE);

            assertThatThrownBy(() -> pastReservation.cancelActive(DEFAULT_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 취소할 수 없습니다.");
        }
    }

    @Nested
    class 대기_취소한다 {

        @Test
        void 정상_조건이면_CANCELED_상태의_예약이_반환된다() {
            Reservation waiting = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThat(waiting.cancelWaiting(DEFAULT_RESERVER_NAME).getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        void 예약자명이_다르면_예외를_던진다() {
            Reservation waiting = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThatThrownBy(() -> waiting.cancelWaiting(OTHER_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약을 취소할 권한이 없습니다.");
        }

        @Test
        void WAITING이_아닌_상태이면_예외를_던진다() {
            Reservation activeReservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            assertThatThrownBy(() -> activeReservation.cancelWaiting(DEFAULT_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("대기중인 예약이 아닙니다.");
        }

        @Test
        void 과거_일정이면_예외를_던진다() {
            Reservation pastWaiting = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, PAST_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThatThrownBy(() -> pastWaiting.cancelWaiting(DEFAULT_RESERVER_NAME))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("지난 예약은 취소할 수 없습니다.");
        }
    }

    @Nested
    class 대기를_확정한다 {

        @Test
        void WAITING_상태이면_ACTIVE_상태의_예약이_반환된다() {
            Reservation waiting = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            assertThat(waiting.confirmWaiting().getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        }

        @Test
        void WAITING이_아닌_상태이면_예외를_던진다() {
            Reservation active = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            assertThatThrownBy(active::confirmWaiting)
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("대기중인 예약이 아닙니다.");
        }
    }

    @Nested
    class 주문을_확정한다 {

        @Test
        void ACTIVE이고_PENDING이고_금액이_일치하면_CONFIRMED_상태로_전이된다() {
            // given
            Reservation activeReservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            // when
            Reservation confirmed = activeReservation.confirmOrder(DEFAULT_AMOUNT);

            // then
            assertThat(confirmed.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        void ACTIVE가_아니면_예외를_던진다() {
            // given
            Reservation waitingReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

            // when & then
            assertThatThrownBy(() -> waitingReservation.confirmOrder(DEFAULT_AMOUNT))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("활성된 예약이 아닙니다.");
        }

        @Test
        void 이미_주문이_확정된_상태이면_예외를_던진다() {
            // given
            Reservation confirmedReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME,
                    ReservationStatus.ACTIVE, OrderStatus.CONFIRMED, 0L);

            // when & then
            assertThatThrownBy(() -> confirmedReservation.confirmOrder(DEFAULT_AMOUNT))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 주문이 확정된 예약입니다.");
        }

        @Test
        void 결제_금액이_주문_금액과_다르면_예외를_던진다() {
            // given
            Reservation activeReservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);
            long tamperedAmount = DEFAULT_AMOUNT + 1;

            // when & then
            assertThatThrownBy(() -> activeReservation.confirmOrder(tamperedAmount))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("결제 금액이 올바르지 않습니다.");
        }

        @Test
        void CONFIRMATION_REQUIRED_상태에서도_확정할_수_있다() {
            // given: 결과 불명(확인 필요) 상태의 예약을 재확인하는 상황
            Reservation confirmationRequired = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME,
                    ReservationStatus.ACTIVE, OrderStatus.CONFIRMATION_REQUIRED, DEFAULT_AMOUNT, 0L);

            // when
            Reservation confirmed = confirmationRequired.confirmOrder(DEFAULT_AMOUNT);

            // then
            assertThat(confirmed.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    class 확인_필요로_표시한다 {

        @Test
        void PENDING이면_CONFIRMATION_REQUIRED로_전이된다() {
            // given
            Reservation activeReservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

            // when
            Reservation marked = activeReservation.markConfirmationRequired();

            // then
            assertThat(marked.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMATION_REQUIRED);
        }

        @Test
        void 이미_확정된_주문이면_예외를_던진다() {
            // given
            Reservation confirmedReservation = Reservation.reconstruct(
                    1L, DEFAULT_RESERVER_NAME, FUTURE_DATE, DEFAULT_TIME, DEFAULT_THEME,
                    ReservationStatus.ACTIVE, OrderStatus.CONFIRMED, 0L);

            // when & then
            assertThatThrownBy(confirmedReservation::markConfirmationRequired)
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 주문이 확정된 예약입니다.");
        }
    }
}
