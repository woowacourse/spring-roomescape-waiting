package roomescape.auth.dto.response;

import org.junit.jupiter.api.Test;
import roomescape.member.model.Member;
import roomescape.member.model.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AuthenticationCheckResponseTest {

    @Test
    void Member_엔티티로부터_파싱한다() {
        // Given
        Member member = new Member(1L, "프리", "a@a.com", "password", Role.NORMAL);

        // When
        AuthenticationCheckResponse authenticationCheckResponse = AuthenticationCheckResponse.from(member);

        // Then
        assertAll(() -> {
            assertThat(authenticationCheckResponse).isNotNull();
            assertThat(authenticationCheckResponse.name()).isEqualTo("프리");
        });
    }
}
