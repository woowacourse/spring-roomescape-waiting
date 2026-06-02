package roomescape.waiting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthInterceptor;
import roomescape.auth.OwnerOnlyArgumentResolver;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.waiting.service.ReservationWaitingService;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@WebMvcTest(ReservationWaitingController.class)
@Import({WebMvcConfig.class, AuthInterceptor.class, OwnerOnlyArgumentResolver.class})
class ReservationWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 대기를 성공적으로 생성한다.")
    void create_Success() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "name", "브라운",
                "date", "2026-05-05",
                "timeId", 1L,
                "themeId", 1L
        );
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaitingResult result = new ReservationWaitingResult(
                1L, "브라운", LocalDate.of(2026, 5, 5), ReservationTimeResult.from(time), ThemeResult.from(theme)
        );

        given(reservationWaitingService.save(any())).willReturn(result);

        // when & then
        mockMvc.perform(post("/reservations-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations-waitings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    @DisplayName("예약 대기 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void create_InvalidRequest_BadRequest() throws Exception {
        // given
        Map<String, Object> requestBody = Map.of(
                "name", "",
                "date", "2026-05-05",
                "timeId", 1L,
                "themeId", 1L
        );

        // when & then
        mockMvc.perform(post("/reservations-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다. 안내된 양식에 맞춰 다시 입력해 주세요."));
    }

    @Test
    @DisplayName("인증 헤더가 없으면 예약 대기 삭제 시 401을 반환한다.")
    void delete_MissingAuthHeader_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations-waitings/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 삭제한다.")
    void delete_Success() throws Exception {
        // given
        willDoNothing().given(reservationWaitingService).deleteById(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/reservations-waitings/1")
                        .header("Authorization", "브라운"))
                .andExpect(status().isNoContent());
    }
}
