package roomescape.repository;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;

@SpringBootTest
@Transactional
class JpaMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일과 암호화된 비밀번호로 회원을 잘 조회하는지 확인")
    void findByEmailAndEncryptedPassword() {
        Member savedMember = memberRepository.save(DEFAULT_MEMBER);
        String email = savedMember.getEmail();
        String encryptedPassword = savedMember.getEncryptedPassword();
        Member foundMember = memberRepository.findByEmailAndEncryptedPassword(
                new Email(email), new Password(encryptedPassword))
                .orElseThrow();

        Assertions.assertThat(foundMember.getId()).isEqualTo(DEFAULT_MEMBER.getId());
    }

    @Test
    @DisplayName("회원 아이디로 회원을 잘 조회하는지 확인")
    void findById() {
        Member savedMember = memberRepository.save(DEFAULT_MEMBER);
        Member foundMember = memberRepository.findById(savedMember.getId())
                .orElseThrow();

        Assertions.assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
    }

    @Test
    @DisplayName("전체 회원을 잘 조회하는지 확인")
    void findAll() {
        Member savedMember = memberRepository.save(DEFAULT_MEMBER);
        List<Member> all = memberRepository.findAll();

        System.out.println(all.size());

        Assertions.assertThat(all)
                .extracting(Member::getId)
                .containsExactlyInAnyOrder(savedMember.getId());
    }
}
