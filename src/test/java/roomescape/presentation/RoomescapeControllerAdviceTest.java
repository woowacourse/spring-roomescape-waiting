package roomescape.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.auth.TokenManager;
import roomescape.presentation.auth.CredentialContext;
import roomescape.presentation.view.AdminController;

@WebMvcTest(AdminController.class)
class RoomescapeControllerAdviceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mvc;

    @MockBean
    private AdminController controller;

    @MockBean
    private CredentialContext context;

    @MockBean
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RoomescapeControllerAdvice())
                .build();
    }

    @Test
    @DisplayName("어플리케이션에서 처리할 수 없는 예외의 경우, 500 서버 오류를 응답한다.")
    void shouldHandleExceptionWithInternalError() throws Exception {
        when(controller.reservationPage()).thenThrow(RuntimeException.class);
        MockHttpServletResponse response = mvc.perform(get("/admin/reservation"))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse();
        mapProblemDetailAndAssert(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ProblemDetail mapProblemDetailAndAssert(MockHttpServletResponse response, HttpStatus status)
            throws Exception {
        ProblemDetail problemDetail = objectMapper.readValue(response.getContentAsString(), ProblemDetail.class);
        int statusCode = problemDetail.getStatus();
        assertThat(statusCode).isEqualTo(status.value());
        return problemDetail;
    }
}
