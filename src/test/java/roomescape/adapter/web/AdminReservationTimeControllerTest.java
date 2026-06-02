package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.ReservationTimeService;
import roomescape.application.dto.result.ReservationTimeResult;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * AdminReservationTimeController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>고유 책임: 시간 생성 요청 본문의 @Valid(@NotNull) 검증, HH:mm 포맷 파싱, 삭제 예외 변환.
 */
@WebMvcTest(AdminReservationTimeController.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @Nested
    @DisplayName("시간 생성 POST /admin/times")
    class Create {

        @Test
        @DisplayName("정상 요청이면 201")
        void 정상_생성() throws Exception {
            given(reservationTimeService.create(any()))
                    .willReturn(new ReservationTimeResult(1L, LocalTime.of(10, 0)));

            mockMvc.perform(post("/admin/times")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"startAt":"10:00"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.startAt").value("10:00"));
        }

        @Test
        @DisplayName("[입력 게이트] startAt이 누락되면 400")
        void startAt_누락_400() throws Exception {
            mockMvc.perform(post("/admin/times")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("시간을 입력해 주세요."));
        }

        @Test
        @DisplayName("[파싱] 잘못된 시간 형식이면 400")
        void 잘못된_시간형식_400() throws Exception {
            mockMvc.perform(post("/admin/times")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"startAt":"25:99"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("시간 삭제 DELETE /admin/times/{id}")
    class Delete {

        @Test
        @DisplayName("정상 삭제면 204")
        void 정상_삭제() throws Exception {
            mockMvc.perform(delete("/admin/times/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[예외 변환] 예약이 존재하는 시간 삭제 시도 → 400 + 메시지")
        void 사용중_시간_400() throws Exception {
            doThrow(new BusinessRuleViolationException("예약이 존재하는 시간은 삭제할 수 없습니다."))
                    .when(reservationTimeService).delete(any());

            mockMvc.perform(delete("/admin/times/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("예약이 존재하는 시간은 삭제할 수 없습니다."));
        }
    }
}
