package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 생성자_정상_동작() {
        Member member = new Member(1L, "홍길동", "hong@example.com", "password", "ADMIN");

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getEmail()).isEqualTo("hong@example.com");
        assertThat(member.getPassword()).isEqualTo("password");
        assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 기본_생성자_사용_시_role_은_MEMBER_id는_null() {
        Member member = new Member("김철수", "kim@example.com", "pass123");

        assertThat(member.getId()).isNull();
        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getEmail()).isEqualTo("kim@example.com");
        assertThat(member.getPassword()).isEqualTo("pass123");
        assertThat(member.getRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void matchesPassword가_올바르게_동작() {
        Member member = new Member("호랑이", "park@example.com", "secret");

        assertThat(member.matchesPassword("secret")).isTrue();
        assertThat(member.matchesPassword("wrong")).isFalse();
    }

    @Test
    void null_값_입력_시_NullPointerException_발생() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThatThrownBy(() -> new Member(1L, null, "b", "c", "MEMBER"))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> new Member(1L, "a", null, "c", "MEMBER"))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> new Member(1L, "a", "b", null, "MEMBER"))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> new Member(1L, "a", "b", "c", (String) null))
                    .isInstanceOf(NullPointerException.class);
        });
    }
}
