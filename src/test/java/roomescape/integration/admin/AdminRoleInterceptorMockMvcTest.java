package roomescape.integration.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class AdminRoleInterceptorMockMvcTest {

    private static final String COOKIE_NAME = "token";

    @Autowired
    private MockMvc mockMvc;

    private Cookie adminCookie;
    private Cookie userCookie;

    @BeforeEach
    void setUp() throws Exception {
        this.adminCookie = loginAndGetCookie("admin1@email.com", "adminpw1");
        this.userCookie = loginAndGetCookie("user1@email.com", "userpw1");
    }

    @ParameterizedTest(name = "권한 없는 사용자가 \"{0}\"에 접근 시 403 반환")
    @DisplayName("권한이 없는 멤버가 어드민 페이지에 접근하면 403으로 응답한다")
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/theme", "/admin/time"})
    void should_Response403_WhenNotAdminMemberAccessAdminPage(final String uri) throws Exception {
        performGetWithCookie(uri, userCookie)
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "관리자가 \"{0}\"에 접근하면 200 반환")
    @DisplayName("관리자는 어드민 페이지에 접근할 수 있다")
    @ValueSource(strings = {"/admin", "/admin/reservation", "/admin/theme", "/admin/time", "/admin/waiting"})
    void can_Access_WhenAdminAccessAdminPage(final String uri) throws Exception {
        performGetWithCookie(uri, adminCookie)
                .andExpect(status().isOk());
    }

    private Cookie loginAndGetCookie(final String email, final String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "%s"
                            }
                            """.formatted(email, password))
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(COOKIE_NAME);
    }

    private ResultActions performGetWithCookie(final String uri, final Cookie cookie) throws Exception {
        return mockMvc.perform(get(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookie));
    }
}
