package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.UserWaitingService;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
    "INSERT INTO member (name, email, password, role) VALUES ('테스트', 'test_user@naver.com', '1234', 'USER')",
})
@Transactional
class UserWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserWaitingService userWaitingService;

    @Test
    @DisplayName("POST /reservations-wait 요청에 올바르게 응답한다")
    void createWaitingTest() throws Exception {
        WaitingServiceResponse response = new WaitingServiceResponse(1L, "테스트",
            LocalDate.of(2025, 5, 24), LocalTime.of(10, 0), "테마");
        given(userWaitingService.create(any())).willReturn(response);

        String token = login();
        Cookie cookie = new Cookie("token", token);
        mockMvc.perform(post("/reservations-wait")
                .contentType("application/json")
                .content("""
                        {
                            "themeId": 1,
                            "date": "2025-05-24",
                            "timeId": 1
                        }
                    """)
                .cookie(cookie))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /reservations-wait/{id} 요청에 올바르게 응답한다")
    void deleteWaitingTest() throws Exception {
        Long id = 1L;
        doNothing().when(userWaitingService).deleteMyWaitingById(id, 1L);

        String token = login();
        Cookie cookie = new Cookie("token", token);
        mockMvc.perform(delete("/reservations-wait/{id}", id)
                .cookie(cookie))
            .andExpect(status().isNoContent());
    }

    private String login() throws Exception {
        RequestBuilder request = post("/login")
            .contentType("application/json")
            .content("""
                {
                    "email": "test_user@naver.com",
                    "password": "1234"
                }
                """);
        return mockMvc.perform(request).andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getCookie("token")
            .getValue();
    }
} 
