package roomescape.feature.time.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.time.dto.command.TimeCreateCommand;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.feature.time.error.type.TimeErrorType;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.service.TimeService;
import roomescape.global.error.exception.GeneralException;
import roomescape.support.WebMvcControllerTest;

@WebMvcControllerTest(controllers = AdminTimeController.class)
class AdminTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeService timeService;

    @MockitoBean
    private TimeMapper timeMapper;

    @Nested
    class 예약_시간_목록_조회 {

        @Test
        void 예약_시간_목록을_조회한다() throws Exception {
            when(timeService.getTimes()).thenReturn(List.of(
                new TimeResponseDto(1L, LocalTime.of(10, 0), false),
                new TimeResponseDto(2L, LocalTime.of(15, 30), true)
            ));

            mockMvc.perform(get("/api/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        void 예약_시간이_없으면_빈_목록을_반환한다() throws Exception {
            when(timeService.getTimes()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class 예약_시간_생성 {

        @Test
        void 예약_시간을_생성한다() throws Exception {
            when(timeMapper.toCreateCommand(any())).thenReturn(new TimeCreateCommand(LocalTime.of(10, 0)));
            when(timeService.saveTime(any())).thenReturn(new TimeResponseDto(1L, LocalTime.of(10, 0), false));

            mockMvc.perform(post("/api/admin/times")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"startAt\": \"10:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.startAt", equalTo("10:00")));
        }

        @Test
        void startAt이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/admin/times")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 이미_등록된_예약_시간이면_4xx를_반환한다() throws Exception {
            when(timeMapper.toCreateCommand(any())).thenReturn(new TimeCreateCommand(LocalTime.of(10, 0)));
            when(timeService.saveTime(any())).thenThrow(new GeneralException(TimeErrorType.ALREADY_EXIST_TIME));

            mockMvc.perform(post("/api/admin/times")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"startAt\": \"10:00\"}"))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 예약_시간_삭제 {

        @Test
        void 예약_시간을_삭제한다() throws Exception {
            doNothing().when(timeService).deleteTimeById(1L);

            mockMvc.perform(delete("/api/admin/times/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        void 존재하지_않는_예약_시간_ID이면_4xx를_반환한다() throws Exception {
            doThrow(new GeneralException(TimeErrorType.TIME_NOT_FOUND)).when(timeService).deleteTimeById(999L);

            mockMvc.perform(delete("/api/admin/times/999"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 음수_ID이면_4xx를_반환한다() throws Exception {
            mockMvc.perform(delete("/api/admin/times/-1"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 문자열_ID이면_4xx를_반환한다() throws Exception {
            mockMvc.perform(delete("/api/admin/times/abc"))
                .andExpect(status().is4xxClientError());
        }
    }
}
