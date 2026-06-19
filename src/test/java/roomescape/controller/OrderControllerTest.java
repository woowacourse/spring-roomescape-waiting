package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Order;
import roomescape.domain.OrderId;
import roomescape.domain.PaymentStatus;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.OrderService;

@WebMvcTest(controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
@Import(LoginUserIdTestResolverConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("GET /order - reservationId로 주문 단건을 응답한다")
    void getOrderByReservationIdRespondsWithSingleOrder() throws Exception {
        Order order = new Order(10L, OrderId.of("order-12345"), 1L, 50000L, PaymentStatus.READY, null);
        given(orderService.getByReservationId(1L)).willReturn(order);

        mockMvc.perform(get("/order").param("reservationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.orderId").value("order-12345"))
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    @DisplayName("GET /order - 주문이 없으면 404과 메시지를 반환한다")
    void getOrderReturns404WhenNotFound() throws Exception {
        given(orderService.getByReservationId(9999L))
                .willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "주문을(를) 찾을 수 없습니다. reservationId=9999"));

        mockMvc.perform(get("/order").param("reservationId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /order - reservationId가 숫자가 아니면 400과 메시지를 반환한다")
    void getOrderReturns400WhenReservationIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/order").param("reservationId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
