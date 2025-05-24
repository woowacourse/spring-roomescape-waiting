package roomescape.reservation.controller;

import static org.mockito.BDDMockito.given;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import roomescape.reservation.application.MyBookingService;
import roomescape.reservation.application.dto.response.MyBookingServiceResponse;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
    "INSERT INTO member (name, email, password, role) VALUES ('테스트', 'test_email@naver.com', '1234', 'USER')",
})
class MyBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyBookingService myBookingService;

    @Test
    @DisplayName("GET /reservations-mine 요청에 대해 올바르게 응답한다")
    void reservationMineTest() throws Exception {
        MyBookingServiceResponse response1 = new MyBookingServiceResponse(
            1L, "테마1", LocalDate.of(2025, 5, 5),
            LocalTime.of(13, 5), "예약");
        MyBookingServiceResponse response2 = new MyBookingServiceResponse(
            2L, "테마2", LocalDate.of(2025, 5, 5),
            LocalTime.of(13, 5), "예약");
        List<MyBookingServiceResponse> responses = List.of(response1, response2);
        given(myBookingService.getAllByMemberId(1L)).willReturn(
            responses);

        String token = login();
        Cookie cookie = new Cookie("token", token);
        MockHttpServletRequestBuilder request = get("/reservations-mine")
            .cookie(cookie);
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    private String login() throws Exception {
        RequestBuilder request = post("/login")
            .contentType("application/json")
            .content("""
                {
                    "email": "test_email@naver.com",
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
