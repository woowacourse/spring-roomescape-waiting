package roomescape.member.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @DisplayName("인덱스를 입력하면 해당 아이디를 가진 Member 객체를 생성해서 반환한다.")
    @Test
    void initializeIndex() {
        // Given
        final MemberRole memberRole = MemberRole.USER;
        final String password = "password";
        final String name = "켈리";
        final String email = "kelly6bf@gmail.com";
        final Member memberWithoutId = Member.createMemberWithoutId(memberRole, password, name, email);

        final Long initialIndex = 1L;

        // When
        final Member memberWithId = memberWithoutId.initializeIndex(initialIndex);

        // Then
        assertThat(memberWithId.getId()).isEqualTo(initialIndex);
    }
}
