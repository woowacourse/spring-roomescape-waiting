package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.adapter.payment.TossProperties;
import roomescape.application.ReservationService;
import roomescape.application.dto.result.ReservationOrderResult;
import roomescape.exception.client.ResourceNotFoundException;

/**
 * UserReservationController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>여기서만 검증할 수 있는 고유 책임: @RequestParam의 @NotBlank 검증.
 * 컨트롤러에 @Validated가 붙어 있어, name 파라미터가 누락/공백이면
 * 각각 MissingServletRequestParameterException / ConstraintViolationException이 발생하고
 * GlobalExceptionHandler가 이를 400 + 메시지로 변환한다.
 * 이 흐름은 서비스를 직접 호출하는 통합 테스트로는 절대 검증되지 않는다(HTTP 파라미터 바인딩 단계라서).
 */
@WebMvcTest(UserReservationController.class)
class UserReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private TossProperties tossProperties;

    @BeforeEach
    void stubDefault() {
        given(reservationService.findMyReservationsAndWaitings(any())).willReturn(List.of());
        given(tossProperties.clientKey()).willReturn("test_client_key");
    }

    @Nested
    @DisplayName("예약 생성 POST /user/reservations")
    class Create {

        @Test
        @DisplayName("정상 요청이면 201과 결제 준비 정보를 반환한다")
        void 결제_준비_응답() throws Exception {
            given(reservationService.reserveWithPayment(any()))
                    .willReturn(new ReservationOrderResult(1L, "order_1", 1000L, "테마A"));

            mockMvc.perform(post("/user/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"브라운","date":"2050-12-31","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reservationId").value(1))
                    .andExpect(jsonPath("$.orderId").value("order_1"))
                    .andExpect(jsonPath("$.amount").value(1000))
                    .andExpect(jsonPath("$.orderName").value("테마A"))
                    .andExpect(jsonPath("$.clientKey").value("test_client_key"));
        }

        @Test
        @DisplayName("[예외 변환] 존재하지 않는 테마면 404")
        void 존재하지_않는_테마_404() throws Exception {
            given(reservationService.reserveWithPayment(any()))
                    .willThrow(new ResourceNotFoundException("존재하지 않는 테마입니다."));

            mockMvc.perform(post("/user/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"브라운","date":"2050-12-31","timeId":1,"themeId":9999}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 테마입니다."));
        }
    }

    @Nested
    @DisplayName("내 예약 조회 GET /user/reservations")
    class MyList {

        @Test
        @DisplayName("name이 있으면 200")
        void 정상_조회() throws Exception {
            mockMvc.perform(get("/user/reservations").param("name", "브라운"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[입력 게이트] name 파라미터 누락 → 400 + 메시지")
        void name_누락_400() throws Exception {
            mockMvc.perform(get("/user/reservations"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
        }

        @Test
        @DisplayName("[입력 게이트] name 파라미터 공백 → 400 + 메시지")
        void name_공백_400() throws Exception {
            mockMvc.perform(get("/user/reservations").param("name", "   "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이름은 비어 있을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("내 예약 취소 DELETE /user/reservations/{id}")
    class Cancel {

        @Test
        @DisplayName("정상 취소면 204")
        void 정상_취소() throws Exception {
            mockMvc.perform(delete("/user/reservations/1").param("name", "브라운"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[입력 게이트] name 파라미터 누락 → 400")
        void name_누락_400() throws Exception {
            mockMvc.perform(delete("/user/reservations/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
        }

        @Test
        @DisplayName("[예외 변환] 서비스가 리소스 부재를 던지면 404")
        void 존재하지_않는_예약_404() throws Exception {
            doThrow(new ResourceNotFoundException("존재하지 않는 예약입니다."))
                    .when(reservationService).deleteByOwner(any(), any());

            mockMvc.perform(delete("/user/reservations/9999").param("name", "브라운"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 예약입니다."));
        }
    }
}
