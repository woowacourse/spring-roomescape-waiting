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
        member = new Member(new MemberName("호기"), "hogi@naver.com", "asd");
    }

    @Test
    @DisplayName("id로 회원을 찾는다.")
    void findById() {
        Long memberId = memberRepository.save(member);
        Member findMember = memberRepository.findById(memberId).get();

        assertThat(findMember.getId()).isEqualTo(memberId);
    }

    @DisplayName("테이블에서 동일한 이름에 동일한 이메일이 있는지 확인한다.")
    @ParameterizedTest
    @CsvSource({"'hogigigigigigi@naver.com', false", "'hogi@naver.com', true"})
    void existNameOrEmail(String email, boolean exist) {
        memberRepository.save(member);
        Member hogi = new Member(new MemberName("hogi"), email, "1234");
        boolean existEmail = memberRepository.existEmail(hogi);

        assertThat(existEmail).isEqualTo(exist);
    }


    @Test
    @DisplayName("이메일과 비밀번호가 일치하는 회원을 찾는다.")
    void findByEmailAndPassword() {
        Long memberId = memberRepository.save(member);
        Member findMember = memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()).get();

        assertThat(findMember.getId()).isEqualTo(memberId);
    }
}
