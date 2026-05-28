package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.controller.dto.ReservationTimeRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@WebMvcTest(ReservationTimeAdminController.class)
@Import(WebMvcConfig.class)
class ReservationTimeAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 시간을 성공적으로 생성한다.")
    void createTime_Success() throws Exception {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        given(reservationTimeService.save(any())).willReturn(time);

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("10:00:00"));
    }

    @Test
    @DisplayName("시간 생성 시 startAt이 누락되면 400 에러를 반환한다.")
    void createTime_MissingStartAt_BadRequest() throws Exception {
        // given
        String requestBody = "{}";

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 시간 요청 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("예약 시간을 성공적으로 삭제한다.")
    void deleteTime_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNoContent());
    }
}
