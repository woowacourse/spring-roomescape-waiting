package roomescape.reservation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.FakeReservationDateRepository;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.controller.dto.response.ReservationWithSlotDetailDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.fixture.FakeReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.fixture.FakeReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.FakeThemeRepository;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.FakeReservationTimeRepository;
import roomescape.time.fixture.ReservationTimeFixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static roomescape.reservation.domain.ReservationStatus.PENDING_PAYMENT;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.exception.ReservationErrorInformation.*;

class ReservationServiceTest {

    private final String name = "한다";
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private ReservationDate reservationDate1;
    private ReservationDate reservationDate2;
    private ReservationSlot slot1;
    private ReservationSlot slot2;
    private Theme theme1;

    private FakeReservationRepository reservationRepository;
    private FakeReservationSlotRepository reservationSlotRepository;
    private PaymentService paymentService;

    private ReservationService reservationService;

    @BeforeEach
    void setup() {
        reservationRepository = new FakeReservationRepository();
        FakeReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
        FakeReservationDateRepository reservationDateRepository = new FakeReservationDateRepository();
        FakeThemeRepository themeRepository = new FakeThemeRepository();
        reservationSlotRepository = new FakeReservationSlotRepository();
        paymentService = mock(PaymentService.class);
        given(paymentService.createPendingPayment(anyLong(), anyLong(), anyLong()))
                .willReturn(Payment.load(1L, 1L, 1L, "test-order-id", null, 1000L, PaymentStatus.PENDING));

        reservationService = new ReservationService(reservationRepository, reservationSlotRepository, paymentService);

        reservationTime1 = reservationTimeRepository.save(ReservationTimeFixture.time15());
        reservationTime2 = reservationTimeRepository.save(ReservationTimeFixture.time16());

        reservationDate1 = reservationDateRepository.save(ReservationDateFixture.oneWeekLater());
        reservationDate2 = reservationDateRepository.save(ReservationDateFixture.twoWeeksLater());

        theme1 = themeRepository.save(ThemeFixture.theme("테마1"));

        slot1 = reservationSlotRepository.save(ReservationSlot.of(reservationDate1, reservationTime1, theme1));
        slot2 = reservationSlotRepository.save(ReservationSlot.of(reservationDate2, reservationTime2, theme1));
    }

    @Test
    @DisplayName("전체 예약 정보를 가져온다.")
    void readAll() {
        // given
        reservationRepository.saveAll(List.of(
                Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()),
                Reservation.reserve(name, slot2.getId(), RESERVED, LocalDateTime.now())
        ));

        // when
        List<ReservationWithSlotInformation> actual = reservationService.readAll();

        // then
        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void reserve() {
        // when
        reservationService.reserve(name, slot1.getId());

        // then
        assertThat(reservationService.readAll()).hasSize(1);
    }

    @Test
    @DisplayName("빈 슬롯에 예약하면 결제 대기 상태가 되고, 주문이 생성된다.")
    void reserve_pending_payment_and_creates_order() {
        // when
        ReservationWithSlotDetailDto actual = reservationService.reserve(name, slot1.getId());

        // then
        assertThat(actual.status())
                .isEqualTo(PENDING_PAYMENT);

        verify(paymentService)
                .createPendingPayment(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 다른 사람이 예약하면 대기 예약이 된다.")
    void reserved_duplicated() {
        // given
        String anotherName = "다른사람";
        reservationRepository.save(Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()));

        // when
        ReservationWithSlotDetailDto actual = reservationService.reserve(anotherName, slot1.getId());

        // then
        assertThat(actual.status())
                .isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("취소된 예약이 있을 때 동일한 사람이 새로 예약할 수 있다.")
    void reserved_when_cancel_same_name() {
        // given
        Reservation reservation = reservationRepository.save(Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()));
        reservation.cancel(name);
        reservationRepository.updateStatus(reservation);

        // when
        ReservationWithSlotDetailDto actual = reservationService.reserve(name, slot1.getId());

        // then
        Assertions.assertThat(actual.status())
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("관리자 전용으로 예약을 취소하면 CANCELED 상태가 된다.")
    void cancelByManager() {
        // given
        Reservation savedReservation = reservationRepository.save(Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()));

        // when
        Reservation actual = reservationService.cancelByManager(slot1.getId(), savedReservation.getId());

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("본인의 예약을 취소할 수 있다.")
    void cancel() {
        // given
        Reservation saved = reservationRepository.save(Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()));

        // when
        Reservation actual = reservationService.cancel(slot1.getId(), saved.getId(), saved.getName());

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("슬롯에 본인 예약이 없는데 취소하면 NOT_FOUND 예외가 발생한다.")
    void cancel_no_reservation_in_slot() {
        // given
        Reservation saved = reservationRepository.save(Reservation.reserve(name, slot1.getId(), RESERVED, LocalDateTime.now()));
        String anotherName = "다른사람";

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(slot1.getId(), saved.getId(), anotherName))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_NOT_OWNER.getMessage());
    }

    @Test
    @DisplayName("이미 지난 슬롯의 예약을 취소하면 예외가 발생한다.")
    void cancel_past() {
        // given
        ReservationDate pastDate = ReservationDate.load(99L, LocalDate.now().minusDays(1), true);
        ReservationSlot pastSlot = reservationSlotRepository.save(ReservationSlot.of(pastDate, reservationTime1, theme1));
        Reservation saved = reservationRepository.save(Reservation.reserve(name, pastSlot.getId(), RESERVED, LocalDateTime.now()));

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(pastSlot.getId(), saved.getId(), saved.getName()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

}
