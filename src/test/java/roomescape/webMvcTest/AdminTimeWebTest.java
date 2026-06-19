package roomescape.webMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.controller.AdminTimeController;
import roomescape.dto.TimeResponse;
import roomescape.service.AdminTimeService;

@WebMvcTest(AdminTimeController.class)
class AdminTimeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTimeService adminTimeService;

    @DisplayName("예약 시간 등록 API")
    @Test
    void 예약_시간_등록_API() throws Exception {
        given(adminTimeService.save(any()))
                .willReturn(new TimeResponse(14L, LocalTime.of(23, 0)));

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "23:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(14))
                .andExpect(jsonPath("$.startAt").value("23:00"));
    }

    @DisplayName("예약 시간 등록 API - 이상값 예외 테스트")
    @Test
    void 예약_시간_등록_API_예외_테스트() throws Exception {
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "230"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("예약 시간 조회 API")
    @Test
    void 예약_시간_조회_API() throws Exception {
        given(adminTimeService.findAll())
                .willReturn(List.of(
                        new TimeResponse(1L, LocalTime.of(10, 0)),
                        new TimeResponse(2L, LocalTime.of(11, 0)),
                        new TimeResponse(3L, LocalTime.of(12, 0)),
                        new TimeResponse(4L, LocalTime.of(13, 0)),
                        new TimeResponse(5L, LocalTime.of(14, 0)),
                        new TimeResponse(6L, LocalTime.of(15, 0)),
                        new TimeResponse(7L, LocalTime.of(16, 0)),
                        new TimeResponse(8L, LocalTime.of(17, 0)),
                        new TimeResponse(9L, LocalTime.of(18, 0)),
                        new TimeResponse(10L, LocalTime.of(19, 0)),
                        new TimeResponse(11L, LocalTime.of(20, 0)),
                        new TimeResponse(12L, LocalTime.of(21, 0)),
                        new TimeResponse(13L, LocalTime.of(22, 0))
                ));

        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(13))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"))
                .andExpect(jsonPath("$[12].id").value(13))
                .andExpect(jsonPath("$[12].startAt").value("22:00"));
    }

    @DisplayName("API - 예약 시간 삭제")
    @Test
    void API_예약_시간_삭제() throws Exception {
        doNothing().when(adminTimeService).delete(14L);

        mockMvc.perform(delete("/admin/times/14"))
                .andExpect(status().isNoContent());
    }
}