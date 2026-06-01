package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@WebMvcTest(ReservationTimeAdminController.class)
class ReservationTimeAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationTimeService reservationTimeService;

    @DisplayName("ReservationTimeRequest을 메시지 바디로 받아 새로운 예약 사건을 생성하고 201을 반환한다.")
    @Test
    void createTimes_success() throws Exception {
        //given
        when(reservationTimeService.registerReservationTime(any()))
                .thenReturn(new ReservationTime(1L, LocalTime.of(10, 0)));

        String body = """
                { "startAt": "10:00"}
                """;

        //when & then
        mockMvc.perform(
                post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isCreated());
    }

    @DisplayName("예약 시간 생성 시 startAt이 유효하지 않으면 400을 발생한다.")
    @Test
    void createTimes_no_ReservationTimeRequest() throws Exception {
        //given
        String noStartAt = """
                {}
                """;

        String invalidStartAt = """
                {"startAt": "invlaid"}
                """;

        //when & then
        mockMvc.perform(
                post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noStartAt)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStartAt)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("id를 받아 예약 시간을 삭제하고 200을 반환한다.")
    @Test
    void deleteTime_success() throws Exception {
        mockMvc.perform(
                delete("/admin/times/{id}", 1)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("예약 시간 삭제 시, id 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void deleteTime_fail() throws Exception {
        mockMvc.perform(
                delete("/admin/times/{id}", "invalid")
        ).andExpect(status().isBadRequest());
    }
}
