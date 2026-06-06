package roomescape.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationWaitingService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReservationWaitingController.class)
class AdminReservationWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 관리자_예약_대기를_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/waitings/1"))
                .andExpect(status().isNoContent());

        verify(reservationWaitingService, times(1)).deleteByAdmin(1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 삭제_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/waitings/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }
}
