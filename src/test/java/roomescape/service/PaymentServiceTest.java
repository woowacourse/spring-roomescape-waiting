package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.support.TestDateTimes.FIXED;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
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
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        this.reservationRepository = new FakeReservationRepository();
        this.reservationTimeRepository = new FakeReservationTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.paymentOrderRepository = new FakePaymentOrderRepository();
        this.paymentService = new PaymentService(
                reservationRepository,
                themeRepository,
                reservationTimeRepository,
                paymentOrderRepository,
                null, // TossPaymentGateway — prepare() 테스트에서는 사용되지 않음
                null, // ReservationService — prepare() 테스트에서는 사용되지 않음
                TestDateTimes.fixedClock()
        );
        ReflectionTestUtils.setField(paymentService, "clientKey", TEST_CLIENT_KEY);
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
}
