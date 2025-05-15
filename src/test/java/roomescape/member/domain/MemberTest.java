package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 생성자_정상_동작() {
        Member member = Member.withRole("홍길동", "hong@example.com", "password", MemberRole.ADMIN);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(member.getName()).isEqualTo("홍길동");
            soft.assertThat(member.getEmail()).isEqualTo("hong@example.com");
            soft.assertThat(member.getPassword()).isEqualTo("password");
            soft.assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
        });
    }

    @Test
    void 기본_생성자_사용_시_role_은_MEMBER_id는_null() {
        Member member = Member.withoutRole("김철수", "kim@example.com", "pass123");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(member.getId()).isNull();
            soft.assertThat(member.getName()).isEqualTo("김철수");
            soft.assertThat(member.getEmail()).isEqualTo("kim@example.com");
            soft.assertThat(member.getPassword()).isEqualTo("pass123");
            soft.assertThat(member.getRole()).isEqualTo(MemberRole.MEMBER);
        });
    }

    @Test
    void matchesPassword가_올바르게_동작() {
        Member member = Member.withoutRole("호랑이", "park@example.com", "secret");

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(member.matchesPassword("secret")).isTrue();
            soft.assertThat(member.matchesPassword("wrong")).isFalse();
        });
    }

    @Test
    void null_값_입력_시_NullPointerException_발생() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThatThrownBy(() -> Member.withRole(null, "b", "c", MemberRole.MEMBER))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> Member.withRole("a", null, "c", MemberRole.MEMBER))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> Member.withRole("a", "b", null, MemberRole.MEMBER))
                    .isInstanceOf(NullPointerException.class);
            soft.assertThatThrownBy(() -> Member.withRole("a", "b", "c", null))
                    .isInstanceOf(NullPointerException.class);
        });
    }
}
