package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;
import roomescape.domain.order.PaymentStatus;
import roomescape.infrastructure.payment.PaymentConfirmation;
import roomescape.service.dto.result.PaymentResult;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        orderRepository.save(new Order("order-1", 10000L, 1L));
    }

    @Test
    void 저장금액과_다른_amount면_확인전에_차단되고_게이트웨이는_호출되지_않는다() {
        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-1", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_게이트웨이를_호출한다() {
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", "order-1", PaymentStatus.DONE, 10000L));

        var result = paymentService.confirm("test_pk_1", "order-1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        verify(paymentGateway).confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L));
    }

}
