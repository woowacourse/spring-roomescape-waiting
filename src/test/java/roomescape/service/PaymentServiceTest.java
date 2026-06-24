package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.Member;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PaymentServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    private Long savedReservationId;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
        Member member = memberRepository.save(Member.createWithoutId("테스터"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("방탈출1", "설명", "https://thumb.com"));
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId(member, new ReservationSlot(LocalDate.now().plusDays(1), time, theme))
        );
        savedReservationId = reservation.getId();
        paymentOrderRepository.save(PaymentOrder.createWithoutId("order-1", 10000L, reservation));
    }

    @Test
    void 저장_금액과_다른_amount면_승인_전에_차단되고_게이트웨이는_호출되지_않는다() {
        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-1", 9000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).getErrorCode())
                        .isEqualTo(PaymentErrorCode.AMOUNT_MISMATCH));

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_게이트웨이를_호출하고_예약이_CONFIRMED_된다() {
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", "order-1", PaymentStatus.DONE, 10000L));

        PaymentResult result = paymentService.confirm("test_pk_1", "order-1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        verify(paymentGateway).confirm(any(PaymentConfirmation.class));

        Reservation updated = reservationRepository.findById(savedReservationId).orElseThrow();
        assertThat(updated.getReservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 존재하지_않는_orderId면_404를_반환한다() {
        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-not-exist", 10000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(paymentGateway, never()).confirm(any());
    }
}
