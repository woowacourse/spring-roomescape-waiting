package roomescape.view;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.jwt.CookieAuthorizationExtractor;
import roomescape.global.jwt.JwtTokenProvider;
import roomescape.member.domain.Role;

@WebMvcTest(AdminController.class)
@Import({JwtTokenProvider.class, CookieAuthorizationExtractor.class, TestCookieFixture.class})
class AdminControllerTest {

    private static final String ADMIN_BASE_URL = "/admin";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestCookieFixture testCookieFixture;

    @Nested
    @DisplayName("관리자 메인 페이지 테스트")
    class AdminMainPage {
        @Test
        @DisplayName("ADMIN 권한은 관리 메인 페이지를 성공적으로 반환한다.")
        void adminTest() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get(ADMIN_BASE_URL).cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/admin/index"));
        }

        @Test
        @DisplayName("USER 권한은 관리 메인 페이지를 접근할 수 없다.")
        void userCannotAccessAdmin() throws Exception {
            Cookie userCookie = testCookieFixture.createCookieWith(Role.USER);

            mockMvc.perform(get(ADMIN_BASE_URL).cookie(userCookie))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GUEST 권한은 관리 메인 페이지를 접근할 수 없다.")
        void guestCannotAccessAdmin() throws Exception {
            Cookie guestCookie = testCookieFixture.createCookieWith(Role.GUEST);

            mockMvc.perform(get(ADMIN_BASE_URL).cookie(guestCookie))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 관리 메인 페이지를 접근할 수 없다.")
        void unauthorizedCannotAccessAdmin() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("예약 관리 페이지 테스트")
    class AdminReservationPage {
        @Test
        @DisplayName("ADMIN 권한은 예약 관리 페이지를 성공적으로 반환한다.")
        void adminCanAccessReservation() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get(ADMIN_BASE_URL + "/reservation").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/admin/reservation-new"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 예약 관리 페이지를 접근할 수 없다.")
        void unauthorizedCannotAccessReservation() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/reservation"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("시간 관리 페이지 테스트")
    class AdminTimePage {
        @Test
        @DisplayName("ADMIN 권한은 시간 관리 페이지를 성공적으로 반환한다.")
        void adminCanAccessTime() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get(ADMIN_BASE_URL + "/time").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/admin/time"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 시간 관리 페이지를 접근할 수 없다.")
        void unauthorizedCannotAccessTime() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/time"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("테마 관리 페이지 테스트")
    class AdminThemePage {
        @Test
        @DisplayName("ADMIN 권한은 테마 관리 페이지를 성공적으로 반환한다.")
        void adminCanAccessTheme() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get(ADMIN_BASE_URL + "/theme").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/admin/theme"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 테마 관리 페이지를 접근할 수 없다.")
        void unauthorizedCannotAccessTheme() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/theme"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("대기 관리 페이지 테스트")
    class AdminWaitingPage {
        @Test
        @DisplayName("ADMIN 권한은 대기 관리 페이지를 성공적으로 반환한다.")
        void adminCanAccessWaiting() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get(ADMIN_BASE_URL + "/waiting").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/admin/waiting"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 대기 관리 페이지를 접근할 수 없다.")
        void unauthorizedCannotAccessWaiting() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/waiting"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
