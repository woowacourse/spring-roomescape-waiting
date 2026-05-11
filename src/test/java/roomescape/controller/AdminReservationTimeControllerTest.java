package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.controller.dto.ReservationTimeCreateRequest;
import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.service.ReservationTimeService;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminReservationTimeController.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 생성하는 요청을 하면 생성된 예약 시간 정보가 응답으로 반환된다.")
    public void create_success() throws Exception {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        given(reservationTimeService.create(any()))
                .willReturn(reservationTime);

        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        // when then
        MvcResult result = mockMvc.perform(
                        post("/admin/times")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ReservationTimeResponse reservationTimeResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationTimeResponse.class
        );

        assertTimeRespose(reservationTimeResponse, reservationTime);

        then(reservationTimeService).should().create(request.startAt());
    }

    private static void assertTimeRespose(ReservationTimeResponse reservationTimeResponse, ReservationTime reservationTime) {
        assertThat(reservationTimeResponse).extracting(
                ReservationTimeResponse::id,
                ReservationTimeResponse::startAt
        ).containsExactly(reservationTime.getId(), reservationTime.getStartAt().toString());
    }

    @Test
    @DisplayName("예약 시간을 생성하는 요청을 할 때 요청값이 비어있으면 에러가 발생한다.")
    public void create_fail1() throws Exception {
        // given
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(null);

        // when then
        mockMvc.perform(
                        post("/admin/times")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 시간을 생성하는 요청을 할 때 시간의 형식이 HH:mm이 아니면 에러가 발생한다.")
    public void create_fail2() throws Exception {
        // given
        String request = """
                {
                    "startAt": "10:30:14"
                }
                """;

        // when then
        mockMvc.perform(
                        post("/admin/times")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("특정 예약 시간을 삭제하는 요청을 한다.")
    public void delete() throws Exception {
        // when then
        long id = 1L;
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/admin/times/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(reservationTimeService).should().delete(id);
    }
}
