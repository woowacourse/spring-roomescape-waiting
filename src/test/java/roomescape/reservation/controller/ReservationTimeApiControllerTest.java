package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.config.ControllerConfig;
import roomescape.reservation.dto.TimeCreateRequest;
import roomescape.reservation.dto.TimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@WebMvcTest(ReservationTimeApiController.class)
@Import(ControllerConfig.class)
class ReservationTimeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("모든 시간 조회 성공 시 200 응답을 받는다.")
    void findAllTest() throws Exception {
        doReturn(new ArrayList<>()).when(reservationTimeService).findAll();

        mockMvc.perform(get("/times")
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 성공 시 200응답을 받는다.")
    void findAvailableTimes() throws Exception {
        LocalDate date = LocalDate.now();
        Long themeId = 1L;
        doReturn(new ArrayList<>()).when(reservationTimeService).findAvailableTimes(date, themeId);

        mockMvc.perform(get("/times/available")
                        .param("date", date.toString())
                        .param("theme-id", themeId.toString())
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("시간 정보를 저장 성공 시 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    void createSuccessTest() throws Exception {
        TimeCreateRequest timeCreateRequest = new TimeCreateRequest(LocalTime.now());
        TimeResponse timeResponse = new TimeResponse(1L, timeCreateRequest.startAt());

        doReturn(1L).when(reservationTimeService)
                .save(any(TimeCreateRequest.class));

        doReturn(timeResponse).when(reservationTimeService)
                .findById(1L);

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeCreateRequest))
                        .cookie(new Cookie("token", "cookieValue"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/1"))
                .andExpect(jsonPath("$.id").value(timeResponse.id()));
    }

    @Test
    @DisplayName("시간 삭제 성공시 204 응답을 받는다.")
    void deleteByIdSuccessTest() throws Exception {
        mockMvc.perform(delete("/times/{id}", 1L)
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
