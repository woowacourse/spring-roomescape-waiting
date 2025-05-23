package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void 생성자_정상_동작() {
        Member member = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .password(Password.createForMember("password"))
                .role(MemberRole.ADMIN)
                .build();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(member.getName()).isEqualTo("홍길동");
            soft.assertThat(member.getEmail()).isEqualTo("hong@example.com");
            soft.assertThat(member.getPassword()).isEqualTo("password");
            soft.assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
        });
    }

    @Test
    void 기본_생성자_사용_시_role_은_MEMBER_id는_null() {
        Member member = Member.builder()
                .name("김철수")
                .email("kim@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();

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
        Member member = Member.builder()
                .name("김철수")
                .email("kim@example.com")
                .password(Password.createForMember("secret"))
                .role(MemberRole.MEMBER)
                .build();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(member.getPassword()).isEqualTo("secret");
            soft.assertThat(member.matchesPassword("secret")).isTrue();
            soft.assertThat(member.matchesPassword("wrong")).isFalse();
        });
    }

    @Test
    void null_값_입력_시_NullPointerException_발생() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThatThrownBy(() -> Member.builder()
                    .name(null)
                    .email("b")
                    .password(Password.createForMember("c"))
                    .role(MemberRole.MEMBER)
                    .build()
            ).isInstanceOf(NullPointerException.class);

            soft.assertThatThrownBy(() -> Member.builder()
                    .name("a")
                    .email(null)
                    .password(Password.createForMember("c"))
                    .role(MemberRole.MEMBER)
                    .build()
            ).isInstanceOf(NullPointerException.class);

            soft.assertThatThrownBy(() -> Member.builder()
                    .name("a")
                    .email("b")
                    .password(null)
                    .role(MemberRole.MEMBER)
                    .build()
            ).isInstanceOf(NullPointerException.class);

            soft.assertThatThrownBy(() -> Member.builder()
                    .name("a")
                    .email("b")
                    .password(Password.createForMember("c"))
                    .role(null)
                    .build()
            ).isInstanceOf(NullPointerException.class);
        });
    }
}
