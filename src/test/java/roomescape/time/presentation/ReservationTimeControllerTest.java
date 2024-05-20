package roomescape.time.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.time.fixture.DateTimeFixture.DAY_AFTER_TOMORROW;
import static roomescape.time.fixture.DateTimeFixture.TIME_11_00;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AdminHandlerInterceptor;
import roomescape.auth.AuthenticatedMemberArgumentResolver;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.service.ReservationTimeService;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @MockBean
    private AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;

    @MockBean
    private AdminHandlerInterceptor adminHandlerInterceptor;

    @DisplayName("전체 시간을 읽는 요청을 처리할 수 있다")
    @Test
    void should_handle_get_times_request_when_requested() throws Exception {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(TIME_11_00);

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @DisplayName("예약 시간 추가 요청을 처리할 수 있다")
    @Test
    void should_handle_post_times_when_requested() throws Exception {
        ReservationTimeAddRequest reservationTimeAddRequest = new ReservationTimeAddRequest(TIME_11_00);
        ReservationTimeResponse mockResponse = new ReservationTimeResponse(1L, TIME_11_00);

        when(reservationTimeService.saveReservationTime(reservationTimeAddRequest)).thenReturn(mockResponse);

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationTimeAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/" + mockResponse.id()));
    }

    @DisplayName("날짜와 테마를 기반으로 예약 상태를 담은 전체 시간 요청을 처리할 수 있다")
    @Test
    void should_handle_get_times_with_book_status_when_requested() throws Exception {
        when(reservationTimeService.findAllWithReservationStatus(DAY_AFTER_TOMORROW, 1L)).thenReturn(
                Collections.emptyList());

        mockMvc.perform(get("/times/available")
                        .param("date", DAY_AFTER_TOMORROW.toString())
                        .param("themeId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @DisplayName("존재하는 리소스에 대한 삭제 요청시, 204 no content를 응답한다.")
    @Test
    void should_handle_delete_time_when_requested() throws Exception {
        mockMvc.perform(delete("/times/{id}", 1))
                .andExpect(status().isNoContent());
    }
}
