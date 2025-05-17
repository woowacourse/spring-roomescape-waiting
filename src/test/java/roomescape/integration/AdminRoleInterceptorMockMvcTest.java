package roomescape.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AdminRoleInterceptorMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("권한이 없는 멤버가 어드민 페이지에 접근하면 403으로 응답한다")
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/theme", "/admin/time"})
    @ParameterizedTest
    void should_Response403_WhenNotAdminMemberAccessAdminPage(String uri) throws Exception {
        // when
        // then
        Cookie cookie = new Cookie("token", getUserToken());
        mockMvc.perform(get(uri)
                        .contentType("application/json")
                        .cookie(cookie)
                ).andExpect(status().isForbidden());
    }

    @DisplayName("관리자는 어드민 페이지에 접근할 수 있다")
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/theme", "/admin/time"})
    @ParameterizedTest
    void can_Access_WhenAdminAccessAdminPage(String uri) throws Exception {
        // when
        // then
        Cookie cookie = new Cookie("token", getAdminToken());
        mockMvc.perform(get(uri)
                .contentType("application/json")
                .cookie(cookie)
        ).andExpect(status().isOk());
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
