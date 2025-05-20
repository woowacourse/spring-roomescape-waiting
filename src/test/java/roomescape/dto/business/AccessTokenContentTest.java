package roomescape.dto.business;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Role;

class AccessTokenContentTest {

    @Nested
    @DisplayName("어드민 토큰 내용인지 확인할 수 있다.")
    public class isAdminToken {

        @DisplayName("어드민 토큰일 경우 true 반환")
        @Test
        void isAdmin() {
            // given
            AccessTokenContent adminTokenContent = new AccessTokenContent(1L, Role.ADMIN, "관리자");

            // when & then
            assertThat(adminTokenContent.isAdminToken()).isTrue();
        }

        @DisplayName("일반 사용자 토큰일 경우 false 반환")
        @Test
        void isMember() {
            // given
            AccessTokenContent memberTokenContent = new AccessTokenContent(1L, Role.GENERAL, "관리자");

            // when & then
            assertThat(memberTokenContent.isAdminToken()).isFalse();
        }
    }

}
