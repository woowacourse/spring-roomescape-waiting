package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.client.TossConfirmResultUnknownException;
import roomescape.client.TossConnectionException;
import roomescape.client.TossPaymentException;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;
import roomescape.domain.PaymentResult;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.fixture.ReservationTimeFixture;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.RoomEscapeException;
import roomescape.query.ReservationQueryRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.command.ReservationCommand;
import roomescape.service.fake.FakePaymentOrderRepository;
import roomescape.service.fake.FakeReservationRepository;
import roomescape.service.fake.FakeReservationTimeRepository;
import roomescape.service.fake.FakeThemeRepository;
import roomescape.service.fixture.ReservationServiceFixture;
import roomescape.support.TestDateTimes;

class PaymentServiceTest {

    private static final String TEST_CLIENT_KEY = "test_ck_placeholder";

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakePaymentOrderRepository paymentOrderRepository;
    private PaymentGateway paymentGateway;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.paymentOrderRepository = new FakePaymentOrderRepository();
        this.paymentGateway = Mockito.mock(PaymentGateway.class);
        ReservationQueryRepository reservationQueryRepository = Mockito.mock(ReservationQueryRepository.class);
        ReservationService reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                reservationQueryRepository,
                TestDateTimes.fixedClock()
        );
        this.paymentService = new PaymentService(
                reservationRepository,
                themeRepository,
                reservationTimeRepository,
                paymentOrderRepository,
                paymentGateway,
                reservationService,
                reservationQueryRepository,
                TestDateTimes.fixedClock()
        );
        ReflectionTestUtils.setField(paymentService, "clientKey", TEST_CLIENT_KEY);
        // 운영 환경에서는 Spring이 self(트랜잭션 프록시)를 주입하지만, 여기서는 직접 생성하므로 자기 참조로 채운다.
        ReflectionTestUtils.setField(paymentService, "self", paymentService);
    }

    private long preparePendingEntry(String name, PaymentOrderRepository paymentOrderRepositoryToSave, String orderId, Long amount) {
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        Reservation slot = Reservation.createSlot(FIXED.plusDays(1).toLocalDate(), theme, time);
        slot.addPendingEntry(name, amount, FIXED);
        Reservation saved = reservationRepository.save(slot);
        long entryId = saved.findEntryByNameAndStatus(name, ReservationStatus.PENDING).getId();

        PaymentOrder paymentOrder = PaymentOrder.create(orderId, amount, entryId, FIXED);
        paymentOrderRepositoryToSave.save(paymentOrder);
        return entryId;
    }

    @Test
    void 결제_준비_응답에_orderId와_clientKey가_포함된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", date, theme.getId(), time.getId());

        // when
        PreparePaymentResponse response = paymentService.prepare(command);

        // then
        assertThat(response.orderId()).isNotBlank();
        assertThat(response.amount()).isEqualTo(ThemeFixture.DEFAULT_PRICE);
        assertThat(response.orderName()).contains(theme.getName());
        assertThat(response.clientKey()).isEqualTo(TEST_CLIENT_KEY);
    }

    @Test
    void 결제_준비_후_PENDING_상태의_엔트리가_저장된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", date, theme.getId(), time.getId());

        // when
        paymentService.prepare(command);

        // then
        Reservation saved = reservationRepository.findByDateAndThemeAndTimeForUpdate(command.toCondition()).orElseThrow();
        assertThat(saved.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.PENDING);
    }

    @Test
    void 결제_준비_후_PaymentOrder가_orderId로_조회된다() {
        // given
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", date, theme.getId(), time.getId());

        // when
        PreparePaymentResponse response = paymentService.prepare(command);

        // then
        assertThat(paymentOrderRepository.findByOrderId(response.orderId())).isPresent();
    }

    @Test
    void 존재하지_않는_테마로_결제_준비하면_예외가_발생한다() {
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", FIXED.plusDays(1).toLocalDate());

        assertThatThrownBy(() -> paymentService.prepare(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 테마 정보입니다.");
    }

    @Test
    void 존재하지_않는_시간으로_결제_준비하면_예외가_발생한다() {
        themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", FIXED.plusDays(1).toLocalDate());

        assertThatThrownBy(() -> paymentService.prepare(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 시간 정보입니다.");
    }

    @Test
    void 결제_금액이_테마_금액과_일치하지_않으면_예외가_발생한다() {
        reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        ReservationCommand command = new ReservationCommand("이프", FIXED.plusDays(1).toLocalDate(), theme.getId(), 1L, 9999L);

        assertThatThrownBy(() -> paymentService.prepare(command))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("결제 금액이 테마 금액과 일치하지 않습니다.");
    }

    @Test
    void 이미_예약된_슬롯에_결제_준비하면_예외가_발생한다() {
        // given: RESERVED 상태 슬롯이 이미 존재
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate date = FIXED.plusDays(1).toLocalDate();

        Reservation existing = Reservation.createSlot(date, theme, time);
        existing.reserve("기존예약자", FIXED);
        reservationRepository.save(existing);

        ReservationCommand command = ReservationServiceFixture.createReserveCommand("이프", date, theme.getId(), time.getId());

        assertThatThrownBy(() -> paymentService.prepare(command))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 결제 중인 날짜입니다.");
    }

    @Test
    void 이미_결제_중인_슬롯에_결제_준비하면_예외가_발생한다() {
        // given: 먼저 PENDING 상태로 선점된 슬롯이 존재
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.createDefault());
        Theme theme = themeRepository.save(ThemeFixture.createDefaultTheme());
        LocalDate date = FIXED.plusDays(1).toLocalDate();
        ReservationCommand firstCommand = ReservationServiceFixture.createReserveCommand("기존예약자", date, theme.getId(), time.getId());
        paymentService.prepare(firstCommand);

        ReservationCommand secondCommand = ReservationServiceFixture.createReserveCommand("이프", date, theme.getId(), time.getId());

        assertThatThrownBy(() -> paymentService.prepare(secondCommand))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 결제 중인 날짜입니다.");
    }

    @Test
    void 결제_승인에_성공하면_엔트리가_RESERVED로_확정되고_결과에_예약정보가_채워진다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        long entryId = preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        PaymentResult response = new PaymentResult("order-1", "DONE", amount, "2024-01-01T00:00:00");
        when(paymentGateway.confirm(any())).thenReturn(response);

        // when
        var result = paymentService.confirm("payment-key-1", "order-1", amount);

        // then
        assertThat(result.response()).isEqualTo(response);
        assertThat(result.themeName()).isEqualTo(ThemeFixture.createDefaultTheme().getName());

        Reservation reservation = reservationRepository.findByEntryId(entryId).orElseThrow();
        assertThat(reservation.findActiveEntry(entryId).getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 결제_승인에_성공하면_PaymentOrder가_CONFIRMED와_paymentKey로_영속화된다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        PaymentResult response = new PaymentResult("order-1", "DONE", amount, "2024-01-01T00:00:00");
        when(paymentGateway.confirm(any())).thenReturn(response);

        // when
        paymentService.confirm("payment-key-1", "order-1", amount);

        // then
        PaymentOrder saved = paymentOrderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(saved.getPaymentKey()).isEqualTo("payment-key-1");
    }

    @Test
    void 토스_승인_결과가_불명이면_PaymentOrder는_CONFIRM_RESULT_UNKNOWN으로_저장되고_엔트리는_PENDING으로_남으며_예외가_전파된다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        long entryId = preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        when(paymentGateway.confirm(any()))
                .thenThrow(new TossConfirmResultUnknownException(new RuntimeException("read timeout")));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm("payment-key-1", "order-1", amount))
                .isInstanceOf(TossConfirmResultUnknownException.class);

        PaymentOrder saved = paymentOrderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.CONFIRM_RESULT_UNKNOWN);

        Reservation reservation = reservationRepository.findByEntryId(entryId).orElseThrow();
        assertThat(reservation.findActiveEntry(entryId).getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 결제_실패_정리_시_PaymentOrder가_FAILED로_저장된다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);

        // when
        paymentService.cancel("order-1");

        // then
        PaymentOrder saved = paymentOrderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
    }

    @Test
    void 이미_확정된_결제는_뒤늦은_cancel_호출에도_FAILED로_덮어써지지_않는다() {
        // given: confirm()까지 성공해 RESERVED + CONFIRMED 상태
        Long amount = ThemeFixture.DEFAULT_PRICE;
        long entryId = preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        PaymentResult response = new PaymentResult("order-1", "DONE", amount, "2024-01-01T00:00:00");
        when(paymentGateway.confirm(any())).thenReturn(response);
        paymentService.confirm("payment-key-1", "order-1", amount);

        // when: 브라우저 뒤로가기 등으로 cancel이 뒤늦게 호출됨
        paymentService.cancel("order-1");

        // then: 결제/예약 상태가 그대로 유지된다
        PaymentOrder saved = paymentOrderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.CONFIRMED);

        Reservation reservation = reservationRepository.findByEntryId(entryId).orElseThrow();
        assertThat(reservation.findActiveEntry(entryId).getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 토스_연결이_실패하면_PaymentOrder는_FAILED로_저장되고_엔트리는_PENDING으로_남으며_예외가_전파된다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        long entryId = preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        when(paymentGateway.confirm(any()))
                .thenThrow(new TossConnectionException(new RuntimeException("connect timeout")));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm("payment-key-1", "order-1", amount))
                .isInstanceOf(TossConnectionException.class);

        PaymentOrder saved = paymentOrderRepository.findByOrderId("order-1").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);

        Reservation reservation = reservationRepository.findByEntryId(entryId).orElseThrow();
        assertThat(reservation.findActiveEntry(entryId).getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 존재하지_않는_orderId로_결제_승인하면_예외가_발생한다() {
        assertThatThrownBy(() -> paymentService.confirm("payment-key-1", "no-such-order", 30000L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("결제 정보를 찾을 수 없습니다.");
    }

    @Test
    void 저장된_금액과_요청_금액이_다르면_결제_승인이_거부된다() {
        // given
        Long savedAmount = ThemeFixture.DEFAULT_PRICE;
        preparePendingEntry("이프", paymentOrderRepository, "order-1", savedAmount);

        // when & then
        assertThatThrownBy(() -> paymentService.confirm("payment-key-1", "order-1", savedAmount + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 금액이 일치하지 않습니다.");
    }

    @Test
    void 토스_승인이_실패하면_엔트리는_PENDING으로_남고_예외가_전파된다() {
        // given
        Long amount = ThemeFixture.DEFAULT_PRICE;
        long entryId = preparePendingEntry("이프", paymentOrderRepository, "order-1", amount);
        when(paymentGateway.confirm(any()))
                .thenThrow(new TossPaymentException.AlreadyProcessed("이미 처리된 결제입니다."));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm("payment-key-1", "order-1", amount))
                .isInstanceOf(TossPaymentException.AlreadyProcessed.class);

        Reservation reservation = reservationRepository.findByEntryId(entryId).orElseThrow();
        assertThat(reservation.findActiveEntry(entryId).getStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
