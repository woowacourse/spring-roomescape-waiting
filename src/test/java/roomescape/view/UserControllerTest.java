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

@WebMvcTest(UserController.class)
@Import({JwtTokenProvider.class, CookieAuthorizationExtractor.class, TestCookieFixture.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestCookieFixture testCookieFixture;

    @Nested
    @DisplayName("예약 메인 페이지")
    class ReservationPage {
        @Test
        @DisplayName("ADMIN 권한은 예약 메인 페이지를 성공적으로 반환한다")
        void adminCanAccessReservation() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get("/reservation").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/reservation"));
        }

        @Test
        @DisplayName("USER 권한은 예약 메인 페이지를 성공적으로 반환한다")
        void userCanAccessReservation() throws Exception {
            Cookie userCookie = testCookieFixture.createCookieWith(Role.USER);

            mockMvc.perform(get("/reservation").cookie(userCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/reservation"));
        }

        @Test
        @DisplayName("GUEST 권한은 예약 메인 페이지를 접근할 수 없다")
        void guestCannotAccessReservation() throws Exception {
            Cookie guestCookie = testCookieFixture.createCookieWith(Role.GUEST);

            mockMvc.perform(get("/reservation").cookie(guestCookie))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("로그인 페이지")
    class LoginPage {
        @Test
        @DisplayName("GUEST 권한은 로그인 페이지를 성공적으로 반환한다")
        void guestCanAccessLogin() throws Exception {
            Cookie guestCookie = testCookieFixture.createCookieWith(Role.GUEST);

            mockMvc.perform(get("/login").cookie(guestCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/login"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자도 로그인 페이지를 접근할 수 있다")
        void unauthorizedCanAccessLogin() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/login"));
        }
    }

    @Nested
    @DisplayName("회원가입 페이지")
    class SignupPage {
        @Test
        @DisplayName("GUEST 권한은 회원가입 페이지를 성공적으로 반환한다")
        void guestCanAccessSignup() throws Exception {
            Cookie guestCookie = testCookieFixture.createCookieWith(Role.GUEST);

            mockMvc.perform(get("/signup").cookie(guestCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/signup"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자도 회원가입 페이지를 접근할 수 있다")
        void unauthorizedCanAccessSignup() throws Exception {
            mockMvc.perform(get("/signup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/signup"));
        }
    }

    @Nested
    @DisplayName("메인 페이지")
    class IndexPage {
        @Test
        @DisplayName("GUEST 권한은 메인 페이지를 성공적으로 반환한다")
        void guestCanAccessIndex() throws Exception {
            Cookie guestCookie = testCookieFixture.createCookieWith(Role.GUEST);

            mockMvc.perform(get("/").cookie(guestCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/index"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자도 메인 페이지를 접근할 수 있다")
        void unauthorizedCanAccessIndex() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/index"));
        }
    }

    @Nested
    @DisplayName("내 예약 조회 페이지")
    class ReservationMinePage {
        @Test
        @DisplayName("ADMIN 권한은 내 예약 조회 페이지를 성공적으로 반환한다")
        void adminCanAccessReservationMine() throws Exception {
            Cookie adminCookie = testCookieFixture.createCookieWith(Role.ADMIN);

            mockMvc.perform(get("/reservation-mine").cookie(adminCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("/reservation-mine"));
        }
    }
}
