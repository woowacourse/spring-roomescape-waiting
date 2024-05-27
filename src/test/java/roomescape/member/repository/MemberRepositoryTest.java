package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.KAKI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("id로 회원을 찾는다.")
    @Test
    void findById() {
        Member kaki = memberRepository.save(KAKI);
        Member findMember = memberRepository.findById(kaki.getId()).get();

        assertAll(
                () -> assertThat(findMember.getName()).isEqualTo(kaki.getName()),
                () -> assertThat(findMember.getEmail()).isEqualTo(kaki.getEmail()),
                () -> assertThat(findMember.getPassword()).isEqualTo(kaki.getPassword())
        );
    }

    @DisplayName("테이블에서 동일한 이메일이 있는지 확인한다.")
    @ParameterizedTest
    @CsvSource({"'카키', 'test@email.com', false", "'카키', 'kaki@email.com', true"})
    void existNameOrEmail(String name, String email, boolean exist) {
        Member kaki = memberRepository.save(KAKI);
        memberRepository.save(kaki);

        Member newMember = Member.createMemberByUserRole(new MemberName(name), email, "1234");
        boolean existNameOrEmail = memberRepository.existsByEmail(newMember.getEmail());

        assertThat(existNameOrEmail).isEqualTo(exist);
    }

    @DisplayName("이메일과 비밀번호가 일치하는 회원을 찾는다.")
    @Test
    void findByEmailAndPassword() {
        Member kaki = memberRepository.save(KAKI);
        Member findMember = memberRepository.findByEmailAndPassword(kaki.getEmail(), kaki.getPassword()).get();

        assertAll(
                () -> assertThat(findMember.getName()).isEqualTo(kaki.getName()),
                () -> assertThat(findMember.getEmail()).isEqualTo(kaki.getEmail()),
                () -> assertThat(findMember.getPassword()).isEqualTo(kaki.getPassword())
        );
    }
}
