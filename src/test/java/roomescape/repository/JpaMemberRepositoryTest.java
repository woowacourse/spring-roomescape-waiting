package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.entity.Member;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    @DisplayName("이메일이 존재한다면 조회할 수 있다.")
    void findMemberByExistedEmail() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        jpaMemberRepository.save(member);

        String email = member.getEmail();
        Member expected = jpaMemberRepository.findByEmail(email);

        assertThat(expected.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("해당 이메일이 없다면 true를 반환한다.")
    void existsByEmail() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        jpaMemberRepository.save(member);

        assertThat(jpaMemberRepository.existsByEmail(member.getEmail())).isTrue();
    }

    @Test
    @DisplayName("해당 이메일이 없다면 false를 반환한다.")
    void notExistsByEmail() {
        Member member = Member.createUser("이름", "이메일", "비밀번호");

        assertThat(jpaMemberRepository.existsByEmail(member.getEmail())).isFalse();
    }
}
