package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MEMBER_BROWN;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("존재하는 모든 사용자를 보여준다.")
    @Test
    void findAll() {
        assertThat(memberRepository.findAll()).isEmpty();
    }

    @DisplayName("해당 id의 사용자를 삭제한다.")
    @Test
    void deleteById() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        // when
        memberRepository.deleteById(member.getId());
        // then
        assertThat(memberRepository.findAll()).isEmpty();
    }

}
