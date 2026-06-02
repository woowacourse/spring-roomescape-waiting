package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberTest {

    @Nested
    class IsAdmin {

        @Test
        @DisplayName("ADMIN 역할이면 true를 반환한다")
        void returnsTrueForAdmin() {
            Member member = new Member(1L, "어드민", "admin@test.com", "password", MemberRole.ADMIN);

            assertThat(member.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("ADMIN이 아니면 false를 반환한다")
        void returnsFalseForNonAdmin() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

            assertThat(member.isAdmin()).isFalse();
        }
    }

    @Nested
    class IsManager {

        @Test
        @DisplayName("MANAGER 역할이면 true를 반환한다")
        void returnsTrueForManager() {
            Member member = new Member(1L, "매니저", "manager@test.com", "password", MemberRole.MANAGER, 1L);

            assertThat(member.isManager()).isTrue();
        }

        @Test
        @DisplayName("MANAGER가 아니면 false를 반환한다")
        void returnsFalseForNonManager() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

            assertThat(member.isManager()).isFalse();
        }
    }

    @Nested
    class MatchesPassword {

        private final PasswordEncoder encoder = new BCryptPasswordEncoder();

        @Test
        @DisplayName("인코딩된 비밀번호와 원문이 일치하면 true를 반환한다")
        void returnsTrueWhenPasswordMatches() {
            String encoded = encoder.encode("rawPassword");
            Member member = new Member(1L, "유저", "user@test.com", encoded, MemberRole.USER);

            assertThat(member.matchesPassword("rawPassword", encoder)).isTrue();
        }

        @Test
        @DisplayName("비밀번호가 다르면 false를 반환한다")
        void returnsFalseWhenPasswordDiffers() {
            String encoded = encoder.encode("rawPassword");
            Member member = new Member(1L, "유저", "user@test.com", encoded, MemberRole.USER);

            assertThat(member.matchesPassword("wrongPassword", encoder)).isFalse();
        }
    }

    @Nested
    class Equality {

        @Test
        @DisplayName("id가 같으면 다른 필드가 달라도 동등하다")
        void equalWhenSameId() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            Member sameId = new Member(1L, "다른이름", "other@test.com", "different", MemberRole.ADMIN);

            assertThat(member).isEqualTo(sameId);
            assertThat(member).hasSameHashCodeAs(sameId);
        }

        @Test
        @DisplayName("id가 다르면 동등하지 않다")
        void notEqualWhenDifferentId() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);
            Member differentId = new Member(2L, "유저", "user@test.com", "password", MemberRole.USER);

            assertThat(member).isNotEqualTo(differentId);
        }

        @Test
        @DisplayName("타입이 다르면 동등하지 않다")
        void notEqualWhenDifferentType() {
            Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

            assertThat(member).isNotEqualTo("not a member");
        }
    }
}
