package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import roomescape.domain.exception.ForbiddenException;
import roomescape.domain.exception.InvalidInputException;
import roomescape.domain.exception.PastReservationException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;
import roomescape.service.ReservationPaymentService;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@WebMvcTest
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @MockitoBean ReservationService reservationService;
    @MockitoBean ReservationPaymentService reservationPaymentService;
    @MockitoBean ReservationTimeService reservationTimeService;
    @MockitoBean ThemeService themeService;

    @Autowired
    MockMvc mockMvc;

    @RestController
    static class TestController {

        @GetMapping("/test/invalid-input")
        void invalidInput() {
            throw new InvalidInputException("잘못된 입력입니다.");
        }

        @GetMapping("/test/reservation-not-found")
        void reservationNotFound() {
            throw new ReservationNotFoundException("존재하지 않는 예약입니다.");
        }

        @GetMapping("/test/reservation-time-not-found")
        void reservationTimeNotFound() {
            throw new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다.");
        }

        @GetMapping("/test/theme-not-found")
        void themeNotFound() {
            throw new ThemeNotFoundException("존재하지 않는 테마입니다.");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }

        @GetMapping("/test/forbidden")
        void forbidden() {
            throw new ForbiddenException("본인의 예약 또는 대기만 관리할 수 있습니다.");
        }

        @GetMapping("/test/past")
        void past() {
            throw new PastReservationException("과거 날짜로는 예약할 수 없습니다.");
        }

        @GetMapping("/test/server-error")
        void serverError() {
            throw new IllegalArgumentException("예상치 못한 에러");
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody ValidationRequest request) {
        }

        @GetMapping("/test/no-resource")
        void noResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "/test/no-resource");
        }

        record ValidationRequest(@NotBlank String name) {
        }
    }

    @DisplayName("InvalidInputException → 400")
    @Test
    void invalidInput_400() throws Exception {
        mockMvc.perform(get("/test/invalid-input"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 입력입니다."));
    }

    @DisplayName("ReservationNotFoundException → 404")
    @Test
    void reservationNotFound_404() throws Exception {
        mockMvc.perform(get("/test/reservation-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 예약입니다."));
    }

    @DisplayName("ReservationTimeNotFoundException → 404")
    @Test
    void reservationTimeNotFound_404() throws Exception {
        mockMvc.perform(get("/test/reservation-time-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 예약 시간입니다."));
    }

    @DisplayName("ThemeNotFoundException → 404")
    @Test
    void themeNotFound_404() throws Exception {
        mockMvc.perform(get("/test/theme-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 테마입니다."));
    }

    @DisplayName("ReservationConflictException → 409")
    @Test
    void conflict_409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 예약된 시간입니다."));
    }

    @DisplayName("ForbiddenException → 403")
    @Test
    void forbidden_403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("본인의 예약 또는 대기만 관리할 수 있습니다."));
    }

    @DisplayName("PastReservationException → 422")
    @Test
    void past_422() throws Exception {
        mockMvc.perform(get("/test/past"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("과거 날짜로는 예약할 수 없습니다."));
    }

    @DisplayName("MethodArgumentNotValidException → 400")
    @Test
    void validation_400() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("HttpMessageNotReadableException → 400")
    @Test
    void malformedJson_400() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 요청 형식입니다."));
    }

    @DisplayName("NoResourceFoundException → 404")
    @Test
    void noResource_404() throws Exception {
        mockMvc.perform(get("/test/no-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."));
    }

    @DisplayName("IllegalArgumentException → 500")
    @Test
    void illegalArgument_500() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."));
    }
}
