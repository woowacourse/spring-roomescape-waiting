package roomescape.presentation.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixtures.anyTimeSlotWithId;
import static roomescape.TestFixtures.anyUserWithId;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.TimeSlotService;
import roomescape.exception.InUseException;
import roomescape.exception.NotFoundException;
import roomescape.presentation.GlobalExceptionHandler;
import roomescape.presentation.StubUserArgumentResolver;

class TimeSlotControllerTest {

    private final TimeSlotService timeSlotService = Mockito.mock(TimeSlotService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new TimeSlotController(timeSlotService))
        .setCustomArgumentResolvers(new StubUserArgumentResolver(anyUserWithId()))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("예약 시간 추가 요청시, id를 포함한 예약 시간과 CREATED를 응답한다.")
    void register() throws Exception {
        Mockito.when(timeSlotService.register(any()))
            .thenReturn(anyTimeSlotWithId());

        mockMvc.perform(post("/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "startAt": "10:00"
                    }
                    """))
            .andExpect(jsonPath("$..['id','startAt']").exists())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 시간 조회 요청시, 존재하는 모든 예약 시간과 OK를 응답한다.")
    void getAllTimeSlots() throws Exception{
        var expectedList = List.of(anyTimeSlotWithId(), anyTimeSlotWithId(), anyTimeSlotWithId());
        Mockito.when(timeSlotService.findAllTimeSlots()).thenReturn(expectedList);

        mockMvc.perform(get("/times"))
            .andExpect(jsonPath("$..['id','startAt']").exists())
            .andExpect(jsonPath("$", hasSize(expectedList.size())))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 시간 삭제 요청시, 주어진 아이디에 해당하는 예약 시간이 있다면 삭제하고 NO CONTENT를 응답한다.")
    void deleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/times/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(timeSlotService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("예약 시간 삭제 요청시, 주어진 아이디에 해당하는 예약 시간이 없다면 NOT FOUND를 응답한다.")
    void deleteWhenNotFound() throws Exception {
        Mockito.doThrow(new NotFoundException("time slot not found"))
            .when(timeSlotService).removeById(eq(999L));

        mockMvc.perform(delete("/times/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("예약 시간 삭제 요청시, 주어진 아이디에 해당하는 예약 시간이 사용 중이라면 CONFLICT를 응답한다.")
    void deleteWhenConflict() throws Exception {
        Mockito.doThrow(new InUseException("some reservation is referencing this time slot"))
            .when(timeSlotService).removeById(eq(999L));

        mockMvc.perform(delete("/times/999"))
            .andExpect(status().isConflict());
    }
}
