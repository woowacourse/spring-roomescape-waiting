package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.time.exception.TimeErrorCode;

@WebMvcTest(ReservationTimeAdminController.class)
@Import(WebMvcConfig.class)
class ReservationTimeAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 성공적으로 생성한다.")
    void createTime_Success() throws Exception {
        // given
        Map<String, Object> request = Map.of("startAt", "10:00");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        given(reservationTimeService.save(any())).willReturn(ReservationTimeResult.from(time));

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
        Map<String, Object> request = new HashMap<>();

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("시간 형식이 올바르지 않습니다. 'HH:mm' 포맷에 맞춰 다시 입력하십시오."));
    }

    @Test
    @DisplayName("중복된 예약 시간 생성 시 409 에러를 반환한다.")
    void createTime_Duplicate_Conflict() throws Exception {
        // given
        Map<String, Object> request = Map.of("startAt", "10:00");
        given(reservationTimeService.save(any()))
                .willThrow(new ConflictException(TimeErrorCode.DUPLICATE_TIME.getMessage()));

        // when & then
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(TimeErrorCode.DUPLICATE_TIME.getMessage()));
    }

    @Test
    @DisplayName("예약 시간을 성공적으로 삭제한다.")
    void delete_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 삭제 시 404 에러를 반환한다.")
    void delete_NotFound_NotFound() throws Exception {
        // given
        willThrow(new NotFoundException(TimeErrorCode.TIME_NOT_FOUND.getMessage()))
                .given(reservationTimeService).deleteById(1L);

        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("사용 중인 예약 시간 삭제 시 409 에러를 반환한다.")
    void delete_InUse_Conflict() throws Exception {
        // given
        willThrow(new ConflictException(TimeErrorCode.TIME_IN_USE.getMessage()))
                .given(reservationTimeService).deleteById(1L);

        // when & then
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(TimeErrorCode.TIME_IN_USE.getMessage()));
    }
}
