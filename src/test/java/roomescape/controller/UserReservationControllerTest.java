package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.PaymentConfirmRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationUpdateRequest;
import roomescape.payment.client.TossProperties;
import roomescape.service.UserReservationService;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.PaymentOrderResult;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.dto.ThemeResult;

@WebMvcTest(UserReservationController.class)
class UserReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserReservationService userReservationService;

    @MockitoBean
    private TossProperties tossProperties;

    @Test
    @DisplayName("GET /user/reservations - 이름으로 예약 목록을 반환한다")
    void list() throws Exception {
        given(userReservationService.findByReserverName("브라운")).willReturn(List.of(sampleResult()));

        mockMvc.perform(get("/user/reservations").param("reserverName", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].reserverName").value("브라운"));
    }

    @Test
    @DisplayName("POST /user/reservations - 유효한 요청이면 결제 대기 주문 정보를 반환한다")
    void createOrder() throws Exception {
        given(userReservationService.createOrder(any()))
                .willReturn(new PaymentOrderResult("order-1", 1000L, "방탈출 예약"));
        given(tossProperties.clientKey()).willReturn("test_ck_docs");
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2099, 12, 31), 1L, 1L);

        mockMvc.perform(post("/user/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.clientKey").isNotEmpty());
    }

    @Test
    @DisplayName("POST /user/reservations/confirm - 결제가 승인되면 예약을 반환한다")
    void confirm() throws Exception {
        given(userReservationService.confirm(any())).willReturn(sampleResult());
        PaymentConfirmRequest request = new PaymentConfirmRequest("test_pk_1", "order-1", 1000L);

        mockMvc.perform(post("/user/reservations/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserverName").value("브라운"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /user/reservations - 동시 중복 등 무결성 제약 위반이면 409를 반환한다")
    void createOrder_duplicate_conflict() throws Exception {
        given(userReservationService.createOrder(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2099, 12, 31), 1L, 1L);

        mockMvc.perform(post("/user/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /user/reservations - 이름이 비어있으면 400을 반환한다")
    void createOrder_invalid() throws Exception {
        ReservationRequest request = new ReservationRequest("", LocalDate.of(2099, 12, 31), 1L, 1L);

        mockMvc.perform(post("/user/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /user/reservations/{id} - 유효한 요청이면 예약을 변경한다")
    void update() throws Exception {
        given(userReservationService.update(any())).willReturn(sampleResult());
        ReservationUpdateRequest request = new ReservationUpdateRequest("브라운", LocalDate.of(2099, 12, 31), 1L);

        mockMvc.perform(patch("/user/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserverName").value("브라운"));
    }

    @Test
    @DisplayName("DELETE /user/reservations/{id} - 예약을 취소하면 204를 반환한다")
    void cancel() throws Exception {
        mockMvc.perform(delete("/user/reservations/1").param("reserverName", "브라운"))
                .andExpect(status().isNoContent());
    }

    private ReservationResult sampleResult() {
        return new ReservationResult(
                1L, "브라운", LocalDate.of(2099, 12, 31),
                new ReservationTimeResult(1L, LocalTime.of(10, 0)),
                new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg"),
                0L,
                ReservationStatus.CONFIRMED
        );
    }
}
