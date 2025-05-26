package roomescape.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtProvider;
import roomescape.auth.TokenBody;
import roomescape.member.MemberRole;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.schedule.dto.ScheduleRequest;
import roomescape.schedule.dto.ScheduleResponse;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ScheduleService scheduleService() {
            return mock(ScheduleService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("스케줄 생성 요청에 성공할 경우 201을 응답한다")
    void create1() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        ScheduleRequest request = new ScheduleRequest(LocalDate.now(), 1L, 1L);
        ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, LocalTime.of(10, 0));
        ThemeResponse themeResponse = new ThemeResponse(1L, "테마명", "테마 설명", "http://example.com/thumbnail.jpg");
        ScheduleResponse response = new ScheduleResponse(1L, LocalDate.now(), timeResponse, themeResponse);
        given(scheduleService.create(request)).willReturn(response);

        // when & then
        mockMvc.perform(post("/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테마명"));
    }
}
