package roomescape.presentation;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.domain.exception.DomainValidationException;
import roomescape.exception.AccessDeniedException;
import roomescape.exception.BadRequestException;
import roomescape.exception.TokenException;
import roomescape.exception.UnauthorizedException;
import roomescape.presentation.GlobalExceptionHandlerTest.TestController;
import roomescape.presentation.GlobalExceptionHandlerTest.TestController.TestRequest;

@WebMvcTest(
        controllers = TestController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
class GlobalExceptionHandlerTest {

    @MockBean
    private TestController testController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String EXCEPTION_URL = "/exception";

    @Test
    @DisplayName("필수 값 검증에 실패한 경우 예외를 처리한다.")
    void handleMethodArgumentNotValid() throws Exception {
        TestRequest request = new TestRequest(null);

        mockMvc.perform(post(EXCEPTION_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)
                        ))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: 이름은 필수 값입니다."));
    }

    @Test
    @DisplayName("요청을 읽을 수 없는 경우 예외를 처리한다.")
    void handleHttpMessageNotReadable() throws Exception {
        mockMvc.perform(post(EXCEPTION_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("invalid"))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청을 읽을 수 없습니다."));
    }

    @Test
    @DisplayName("메소드의 인자 타입이 일치하지 않는 경우 예외를 처리한다.")
    void handleMethodArgumentTypeMismatchException() throws Exception {
        TestRequest request = new TestRequest("test");

        mockMvc.perform(post(EXCEPTION_URL + "/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 값이 잘못되었습니다."));
    }

    @Test
    @DisplayName("도메인 검증에서 발생한 예외를 처리한다.")
    void handleValidationException() throws Exception {
        String message = "이름은 필수 값입니다.";
        doThrow(new DomainValidationException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @DisplayName("도메인을 조회할 때 찾을 수 없는 경우 예외를 처리한다.")
    void handleNotFoundException() throws Exception {
        String message = "해당 id의 회원을 찾을 수 없습니다.";
        doThrow(new DomainValidationException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @DisplayName("권한이 없는 경우 예외를 처리한다.")
    void handleUnauthorizedException() throws Exception {
        String message = "권한이 없습니다.";
        doThrow(new UnauthorizedException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @DisplayName("토큰이 유효하지 않은 경우 예외를 처리한다.")
    void handleTokenException() throws Exception {
        String message = "토큰이 유효하지 않습니다.";
        doThrow(new TokenException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @DisplayName("접근 권한이 없는 경우 예외를 처리한다.")
    void handleAccessDeniedException() throws Exception {
        String message = "어드민 권한이 필요합니다.";
        doThrow(new AccessDeniedException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    @DisplayName("잘못된 요청인 경우 예외를 처리한다.")
    void handleBadRequestException() throws Exception {
        String message = "잘못된 요청입니다.";
        doThrow(new BadRequestException(message)).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            RuntimeException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            NoSuchElementException.class
    })
    @DisplayName("예상치 못한 예외들을 처리한다.")
    void handleException(Class<? extends Exception> exception) throws Exception {
        doThrow(exception).when(testController).get();

        mockMvc.perform(get(EXCEPTION_URL))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 내부 에러가 발생했습니다."));
    }

    @Controller
    static class TestController {

        @GetMapping(EXCEPTION_URL)
        public ResponseEntity<String> get() {
            return ResponseEntity.ok("ok");
        }

        @PostMapping(EXCEPTION_URL + "/{id}")
        public ResponseEntity<Void> post(
                @PathVariable Long id,
                @RequestBody @Valid TestRequest request
        ) {
            return ResponseEntity.ok().build();
        }

        public record TestRequest(@NotNull(message = "이름은 필수 값입니다.") String name) {
        }
    }
}
