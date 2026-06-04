package roomescape.waiting.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminInterceptor;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;

@WebMvcTest(WaitingController.class)
@Import({AdminInterceptor.class, WaitingControllerTest.TestWebConfig.class})
class WaitingControllerTest {

    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Autowired
        private AdminInterceptor adminInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(adminInterceptor).addPathPatterns("/**");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    @DisplayName("대기 생성 요청이 성공하면 생성 응답을 반환한다")
    void createWaiting_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "테스트-테마", "설명", "https://thumbnail.com");
        Waiting waiting = Waiting.createRow(1L, "브라운", date, time, theme, 1L, LocalDateTime.now());
        WaitingCreateCommand command = new WaitingCreateCommand("브라운", date, 1L, 1L);
        given(waitingService.save(command)).willReturn(waiting);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/waiting/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2026-05-05"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테스트-테마"))
                .andExpect(jsonPath("$.rank").value(1));

        then(waitingService).should().save(command);
    }

    @Test
    @DisplayName("대기 생성 요청에서 예약자 이름이 비어 있으면 에러 응답을 반환한다")
    void createWaiting_fail_with_empty_name() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", " ");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약자 이름을 입력해 주세요."));
    }

    @Test
    @DisplayName("대기 생성 요청에서 예약 날짜 형식이 잘못되면 에러 응답을 반환한다")
    void createWaiting_fail_with_invalid_date_format() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "invalid-date");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("date 값의 형식이 올바르지 않습니다. yyyy-MM-dd 형식으로 입력해 주세요."));
    }

    @Test
    @DisplayName("대기 생성 요청에서 예약 시간이 비어 있으면 에러 응답을 반환한다")
    void createWaiting_fail_with_empty_time_id() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", null);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 시간을 선택해 주세요."));
    }

    @Test
    @DisplayName("대기 생성 요청에서 서비스 정책을 위반하면 에러 응답을 반환한다")
    void createWaiting_fail_with_business_exception() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        WaitingCreateCommand command = new WaitingCreateCommand("브라운", date, 1L, 1L);
        willThrow(new BusinessException(WaitingErrorCode.WAITING_TIME_INVALID))
                .given(waitingService)
                .save(command);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("존재하지 않는 예약 시간입니다."));
    }

    @Test
    @DisplayName("대기 생성 요청에서 예약이 없는 슬롯이면 에러 응답을 반환한다")
    void createWaiting_fail_with_not_found_reservation() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        WaitingCreateCommand command = new WaitingCreateCommand("브라운", date, 1L, 1L);
        willThrow(new BusinessException(WaitingErrorCode.WAITING_NOT_EXIST_RESERVATION))
                .given(waitingService)
                .save(command);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약이 존재하지 않으면, 대기요청을 할 수 없습니다."));
    }

    @Test
    @DisplayName("본인의 예약 대기를 취소하면 성공 응답을 반환한다")
    void deleteUserWaiting_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/waiting/me/{id}", 1L)
                        .param("name", "브라운"))
                .andExpect(status().isNoContent());

        then(waitingService).should().cancelWaiting(1L, "브라운");
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 취소하면 에러 응답을 반환한다")
    void deleteUserWaiting_fail_with_not_found_waiting() throws Exception {
        // given
        willThrow(new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, 999L))
                .given(waitingService)
                .cancelWaiting(999L, "브라운");

        // when & then
        mockMvc.perform(delete("/waiting/me/{id}", 999L)
                        .param("name", "브라운"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을 찾을 수 없습니다. id: 999"));
    }

    @Test
    @DisplayName("다른 사용자의 예약 대기를 취소하면 에러 응답을 반환한다")
    void deleteUserWaiting_fail_with_owner_mismatch() throws Exception {
        // given
        willThrow(new BusinessException(WaitingErrorCode.WAITING_OWNER_MISMATCH))
                .given(waitingService)
                .cancelWaiting(1L, "다른사람");

        // when & then
        mockMvc.perform(delete("/waiting/me/{id}", 1L)
                        .param("name", "다른사람"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("수정할 수 있는 권한이 없습니다."));
    }
}
