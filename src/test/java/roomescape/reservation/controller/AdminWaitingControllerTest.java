package roomescape.reservation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.reservation.application.AdminWaitingService;
import roomescape.reservation.application.dto.response.MyWaitingServiceResponse;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
    "INSERT INTO member (name, email, password, role) VALUES ('어드민', 'test_admin@naver.com', '1234', 'ADMIN')",
})
class AdminWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminWaitingService adminWaitingService;

    @Test
    @DisplayName("GET /admin/waitings 요청에 올바르게 응답한다")
    void getAllWaitingsTest() throws Exception {
        given(adminWaitingService.getAllWaitings()).willReturn(List.of(
            new MyWaitingServiceResponse(1L, "두리", "테마", LocalDate.of(2025, 5, 24),
                LocalTime.of(10, 0))
        ));

        String token = adminLogin();
        Cookie cookie = new Cookie("token", token);
        mockMvc.perform(get("/admin/waitings")
                .cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("DELETE /admin/waitings/{id} 삭제시 204를 반환한다")
    void removeWaitingTest() throws Exception {
        Long id = 1L;
        doNothing().when(adminWaitingService).deleteById(id);

        String token = adminLogin();
        Cookie cookie = new Cookie("token", token);
        mockMvc.perform(delete("/admin/waitings/{id}", id)
                .cookie(cookie))
            .andExpect(status().isNoContent());
    }

    private String adminLogin() throws Exception {
        RequestBuilder request = post("/login")
            .contentType("application/json")
            .content("""
                {
                    "email": "test_admin@naver.com",
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
