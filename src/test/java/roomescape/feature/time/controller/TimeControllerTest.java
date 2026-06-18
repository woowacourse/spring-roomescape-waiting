package roomescape.feature.time.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.error.type.TimeErrorType;
import roomescape.feature.time.service.TimeService;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralParametersException;
import roomescape.support.WebMvcControllerTest;

@WebMvcControllerTest(controllers = TimeController.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeService timeService;

    @Nested
    class 예약_가능_시간_조회 {

        @Test
        void 날짜와_테마_ID로_가용_시간_목록을_조회한다() throws Exception {
            LocalDate date = LocalDate.of(2099, 5, 1);
            when(timeService.getTimeAvailabilities(date, 1L)).thenReturn(List.of(
                new TimeAvailabilityResponseDto(1L, LocalTime.of(10, 0), true),
                new TimeAvailabilityResponseDto(2L, LocalTime.of(15, 30), false)
            ));

            mockMvc.perform(get("/api/times")
                    .queryParam("date", "2099-05-01")
                    .queryParam("themeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].available", equalTo(true)))
                .andExpect(jsonPath("$[1].available", equalTo(false)));
        }

        @Test
        void date_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/times")
                    .queryParam("themeId", "1"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void themeId_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/times")
                    .queryParam("date", "2099-05-01"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void date_형식이_올바르지_않으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/times")
                    .queryParam("date", "not-a-date")
                    .queryParam("themeId", "1"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 음수_themeId이면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/api/times")
                    .queryParam("date", "2099-05-01")
                    .queryParam("themeId", "-1"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 존재하지_않는_themeId이면_4xx를_반환한다() throws Exception {
            when(timeService.getTimeAvailabilities(LocalDate.of(2099, 5, 1), 999L))
                .thenThrow(new GeneralParametersException(
                    TimeErrorType.FIELD_RESOURCE_NOT_FOUND,
                    List.of(new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다."))
                ));

            mockMvc.perform(get("/api/times")
                    .queryParam("date", "2099-05-01")
                    .queryParam("themeId", "999"))
                .andExpect(status().is4xxClientError());
        }
    }
}
