package roomescape.auth.session;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class SessionRegistryTest {

    private SessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        sessionRegistry = new SessionRegistry();
    }

    @Test
    @DisplayName("같은 memberId로 두 번 로그인하면 첫 번째 세션이 무효화된다")
    void invalidatesExistingSessionOnSecondLogin() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session1, "memberId", 1L));
        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session2, "memberId", 1L));

        assertThat(session1.isInvalid()).isTrue();
        assertThat(session2.isInvalid()).isFalse();
    }

    @Test
    @DisplayName("다른 memberId로 로그인하면 서로의 세션에 영향을 주지 않는다")
    void doesNotInvalidateSessionsOfDifferentMembers() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session1, "memberId", 1L));
        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session2, "memberId", 2L));

        assertThat(session1.isInvalid()).isFalse();
        assertThat(session2.isInvalid()).isFalse();
    }

    @Test
    @DisplayName("세션이 소멸되면 레지스트리에서 제거되어 이후 같은 memberId 재로그인 시 영향을 주지 않는다")
    void removesSessionFromRegistryOnDestroy() {
        MockHttpSession session1 = new MockHttpSession();
        session1.setAttribute("memberId", 1L);

        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session1, "memberId", 1L));
        sessionRegistry.sessionDestroyed(new HttpSessionEvent(session1));

        MockHttpSession session2 = new MockHttpSession();
        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session2, "memberId", 1L));

        assertThat(session2.isInvalid()).isFalse();
    }

    @Test
    @DisplayName("memberId가 아닌 다른 속성이 추가되면 세션에 영향을 주지 않는다")
    void ignoresNonMemberIdAttributes() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session1, "memberId", 1L));
        sessionRegistry.attributeAdded(new HttpSessionBindingEvent(session2, "otherAttribute", 1L));

        assertThat(session1.isInvalid()).isFalse();
    }
}
