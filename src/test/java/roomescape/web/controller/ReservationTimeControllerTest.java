package roomescape.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.request.time.ReservationTimeRequest;
import roomescape.service.dto.response.time.ReservationTimeResponse;

@WebMvcTest(controllers = ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약시간 저장 시 모든 필드에 유효한 값이라면 201 Created를 반환한다.")
    void saveReservationTime_ShouldReturn201CreatedStatusCode_WhenInsertAllValidField() throws Exception {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(1, 0));

        when(reservationTimeService.saveReservationTime(request))
                .thenReturn(new ReservationTimeResponse(1L, LocalTime.of(1, 0)));

        // when, then
        mockMvc.perform(post("/times")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약시간 저장 시 모든 필드가 유효한 값이라면 location 헤더가 추가된다.")
    void saveReservationTime_ShouldRedirect_WhenInsertAllValidateField() throws Exception {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(1, 0));
        when(reservationTimeService.saveReservationTime(request))
                .thenReturn(new ReservationTimeResponse(1L, LocalTime.of(1, 0)));

        // when, then
        mockMvc.perform(post("/times")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(header().string(LOCATION, "/times/1"));
    }

    @Test
    @DisplayName("예약시간 저장 시 시간이 빈값이면 400 BadRequest를 반환한다.")
    void saveReservation_ShouldReturn400StatusCode_WhenInsertLessThen0ThemeId() throws Exception {
        ReservationTimeRequest request = new ReservationTimeRequest(null);
        when(reservationTimeService.saveReservationTime(request))
                .thenReturn(new ReservationTimeResponse(1L, LocalTime.of(1, 0)));

        // when, then
        mockMvc.perform(post("/times")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("시간은 빈값을 허용하지 않습니다.")));
    }

}
