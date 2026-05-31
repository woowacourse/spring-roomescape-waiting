package roomescape.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationTimeService;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReservationTimeController.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약_시간_목록을_조회한다() throws Exception {
        // given
        given(reservationTimeService.findAll())
                .willReturn(List.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        // when & then
        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"));

        verify(reservationTimeService, times(1)).findAll();
        verifyNoMoreInteractions(reservationTimeService);
    }

    @Test
    void 예약_시간을_생성한다() throws Exception {
        // given
        given(reservationTimeService.create(LocalTime.of(10, 0)))
                .willReturn(new ReservationTime(1L, LocalTime.of(10, 0)));

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "10:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/admin/times/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("10:00:00"));

        verify(reservationTimeService, times(1)).create(LocalTime.of(10, 0));
        verifyNoMoreInteractions(reservationTimeService);
    }

    @Test
    void 예약_시간_생성_요청값이_유효하지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("startAt은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationTimeService);
    }

    @Test
    void 예약_시간을_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNoContent());

        verify(reservationTimeService, times(1)).delete(1L);
        verifyNoMoreInteractions(reservationTimeService);
    }

    @Test
    void 삭제_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/times/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationTimeService);
    }

    @Test
    void 예약이_존재하는_시간은_삭제시_에러_응답() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 시간은 삭제할 수 없습니다."))
                .when(reservationTimeService)
                .delete(1L);

        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"))
                .andExpect(jsonPath("$.detail").value("예약이 존재하는 시간은 삭제할 수 없습니다."));

        verify(reservationTimeService, times(1)).delete(1L);
        verifyNoMoreInteractions(reservationTimeService);
    }
}
