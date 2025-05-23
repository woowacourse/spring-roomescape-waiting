package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.entity.Member;
import roomescape.member.repository.JpaMemberRepository;


import java.util.Optional;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    @DisplayName("이메일이 존재한다면 조회할 수 있다.")
    void findMemberByExistedEmail() {
        // given
        Member member = Member.createUser("이름", "이메일", "비밀번호");
        jpaMemberRepository.save(member);

        // when
        Optional<Member> result = jpaMemberRepository.findByEmail(member.getEmail());

        // then
        assertThat(result).isPresent(); // Optional이 존재하는지
        assertThat(result.get().getEmail()).isEqualTo(member.getEmail());
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
