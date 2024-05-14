package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;

@JdbcTest
@Import(MemberRepository.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    private Member member;

    @BeforeEach
    void init() {
        member = new Member(new MemberName("카키"), "kaki@email.com", "1234");
    }

    @DisplayName("id로 회원을 찾는다.")
    @Test
    void findById() {
        Long memberId = memberRepository.save(member);
        Member findMember = memberRepository.findById(memberId).get();

        assertThat(findMember.getId()).isEqualTo(memberId);
    }

    @DisplayName("테이블에서 동일한 이름에 동일한 이메일이 있는지 확인한다.")
    @ParameterizedTest
    @CsvSource({"'카키', 'test@email.com', false", "'카키', 'kaki@email.com', true"})
    void existNameOrEmail(String name, String email, boolean exist) {
        memberRepository.save(member);

        Member newMember = new Member(new MemberName(name), email, "1234");
        boolean existNameOrEmail = memberRepository.existNameOrEmail(newMember);

        assertThat(existNameOrEmail).isEqualTo(exist);
    }

    @DisplayName("이메일과 비밀번호가 일치하는 회원을 찾는다.")
    @Test
    void findByEmailAndPassword() {
        Long memberId = memberRepository.save(member);

        Member findMember = memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()).get();

        assertThat(findMember.getId()).isEqualTo(memberId);
    }
}
