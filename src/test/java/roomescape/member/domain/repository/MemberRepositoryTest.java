package roomescape.member.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;

@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다")
    @Test
    void save() {
        // given
        Member member = new Member("name", "email", "password");

        // when
        Collection<Member> members = memberRepository.findAll();
        memberRepository.save(member);
        Collection<Member> newMembers = memberRepository.findAll();

        // then
        assertThat(newMembers).hasSize(members.size() + 1);
    }
}
