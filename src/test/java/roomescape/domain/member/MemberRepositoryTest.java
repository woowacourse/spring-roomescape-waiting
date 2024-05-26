package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.exception.DomainNotFoundException;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("아이디로 회원을 조회한다.")
    void getById() {
        Member savedMember = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        Member member = memberRepository.getById(savedMember.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(member.getId()).isNotNull();
            softly.assertThat(member.getEmail()).isEqualTo("ex@gmail.com");
            softly.assertThat(member.getPassword()).isEqualTo("password");
            softly.assertThat(member.getName()).isEqualTo("구름");
            softly.assertThat(member.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("아이디로 회원을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        assertThatThrownBy(() -> memberRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage("해당 id의 회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void getByEmail() {
        memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        Member member = memberRepository.getByEmail("ex@gmail.com");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(member.getId()).isNotNull();
            softly.assertThat(member.getEmail()).isEqualTo("ex@gmail.com");
            softly.assertThat(member.getPassword()).isEqualTo("password");
            softly.assertThat(member.getName()).isEqualTo("구름");
            softly.assertThat(member.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("이메일로 회원을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByEmailWhenNotExist() {
        memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        assertThatThrownBy(() -> memberRepository.getByEmail("not-exists"))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 이메일의 회원이 존재하지 않습니다. (email: %s)", "not-exists"));
    }
}
