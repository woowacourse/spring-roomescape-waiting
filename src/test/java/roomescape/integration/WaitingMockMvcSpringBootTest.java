package roomescape.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import roomescape.CurrentDateTime;
import roomescape.waiting.controller.dto.WaitingAddRequest;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.WaitingAddCommand;
import roomescape.waiting.service.dto.WaitingInfo;

@ActiveProfiles({"test", "auth"})
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class WaitingMockMvcSpringBootTest {

    @Autowired
    WaitingService waitingService;
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CurrentDateTime currentDateTime;
    LocalDate today;

    @BeforeEach
    void init() {
        today = LocalDate.of(2025, 4, 28);
        when(currentDateTime.getTime()).thenReturn(LocalTime.of(16, 1));
        when(currentDateTime.getDate()).thenReturn(today);
    }

    @DisplayName("예약 대기를 생성할 수 있다")
    @Test
    void aa() throws Exception {
        // given
        String userToken = getUserToken();
        WaitingAddRequest request = new WaitingAddRequest(today.plusDays(1), 11L, 1L);
        Cookie cookie = new Cookie("token", userToken);

        // when
        // then
        mockMvc.perform(post("/waiting")
                        .contentType("application/json")
                        .content(new ObjectMapper().registerModule(new JavaTimeModule())
                                .writeValueAsString(request))
                        .cookie(cookie)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.theme").value("테마11"))
                .andExpect(jsonPath("$.date").value(today.plusDays(1).toString()))
                .andExpect(jsonPath("$.time").value("10:00"))
                .andExpect(jsonPath("$.status").value("대기"));
    }

    @DisplayName("예약 대기를 취소할 수 있다")
    @Test
    void aaa() throws Exception {
        // given
        String userToken = getUserToken();
        WaitingAddRequest request = new WaitingAddRequest(today.plusDays(1), 11L, 1L);
        Cookie cookie = new Cookie("token", userToken);
        WaitingInfo waitingInfo = waitingService.addWaiting(new WaitingAddCommand(today.plusDays(1), 1L, 11L, 2L));

        // when
        // then
        mockMvc.perform(delete("/waiting/" + waitingInfo.id()).cookie(cookie))
                .andExpect(status().isNoContent());
    }

    @DisplayName("예약 대기 목록을 조회할 수 있다")
    @Test
    void aaaa() throws Exception {
        // given
        String userToken = getUserToken();
        Cookie cookie = new Cookie("token", userToken);
        waitingService.addWaiting(new WaitingAddCommand(today.plusDays(1), 1L, 11L, 2L));
        waitingService.addWaiting(new WaitingAddCommand(today.plusDays(2), 1L, 11L, 2L));

        // when
        // then
        mockMvc.perform(get("/waiting").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private String getAdminToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "admin@gmail.com",
                                    "password": "qwer!"
                                }
                                """)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("token")
                .getValue();
    }

    private String getUserToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "user@gmail.com",
                                    "password": "qwer!"
                                }
                                """)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("token")
                .getValue();
    }
}
