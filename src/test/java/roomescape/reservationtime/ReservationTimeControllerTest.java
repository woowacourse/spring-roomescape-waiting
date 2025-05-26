package roomescape.reservationtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtProvider;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReservationTimeService reservationTimeService() {
            return mock(ReservationTimeService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("예약 시간 생성 요청에 성공할 경우 201을 응답한다")
    void create() throws Exception {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        ReservationTimeResponse response = new ReservationTimeResponse(1L, LocalTime.of(10, 0));
        given(reservationTimeService.create(request)).willReturn(response);

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("10:00"));
    }

    @Test
    @DisplayName("예약 시간 생성 시 필수 필드가 누락되면 400을 응답한다")
    void createWithInvalidRequest() throws Exception {
        // given
        String invalidJson = "{\"startAt\": null}";

        // when & then
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("모든 예약 시간을 조회하면 200과 예약 시간 목록을 응답한다")
    void findAll() throws Exception {
        // given
        List<ReservationTimeResponse> responses = List.of(
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ReservationTimeResponse(2L, LocalTime.of(12, 0))
        );
        given(reservationTimeService.getAll()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].startAt").value("12:00"));
    }

    @Test
    @DisplayName("예약 시간이 없을 경우 빈 목록을 응답한다")
    void findAllEmpty() throws Exception {
        // given
        given(reservationTimeService.getAll()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("특정 테마와 날짜에 대한 가능한 예약 시간을 조회하면 200과 가능한 예약 시간 목록을 응답한다")
    void findAllAvailableTimes() throws Exception {
        // given
        List<AvailableReservationTimeResponse> responses = List.of(
                new AvailableReservationTimeResponse(1L, LocalTime.of(10, 0), false),
                new AvailableReservationTimeResponse(2L, LocalTime.of(12, 0), true)
        );
        given(reservationTimeService.getAllAvailableTimes(anyLong(), any(LocalDate.class))).willReturn(responses);

        // when & then
        mockMvc.perform(get("/times/available-time")
                        .param("themeId", "1")
                        .param("date", "2023-08-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"))
                .andExpect(jsonPath("$[0].alreadyBooked").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].startAt").value("12:00"))
                .andExpect(jsonPath("$[1].alreadyBooked").value(true));
    }

    @Test
    @DisplayName("예약 시간 삭제 요청에 성공할 경우 204를 응답한다")
    void deleteById() throws Exception {
        // when & then
        mockMvc.perform(delete("/times/1"))
                .andExpect(status().isNoContent());

        verify(reservationTimeService).deleteById(1L);
    }
}
