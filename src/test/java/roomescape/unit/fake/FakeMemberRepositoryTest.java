package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class FakeMemberRepositoryTest {

    private final FakeMemberRepository memberDao = new FakeMemberRepository();

    @Test
    void 회원을_저장한다() {
        // given
        Member member = new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER);
        // when
        memberDao.save(member);
        // then
        List<Member> members = memberDao.getMembers();
        assertThat(members.getFirst().getName()).isEqualTo("name1");
        assertThat(members.getFirst().getEmail()).isEqualTo("email1@domain.com");
        assertThat(members.getFirst().getPassword()).isEqualTo("password1");
    }

    @Test
    void 이메일로_회원을_찾는다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberDao.getMembers().add(member);
        // when
        Optional<Member> optionalMember = memberDao.findByEmail("email1@domain.com");
        // then
        assertThat(optionalMember.isPresent()).isTrue();
        assertThat(optionalMember.get().getEmail()).isEqualTo("email1@domain.com");
        assertThat(optionalMember.get().getPassword()).isEqualTo("password1");
    }

    @Test
    void id로_회원을_찾는다() {
        // given
        Member member = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberDao.getMembers().add(member);
        // when
        Optional<Member> optionalMember = memberDao.findById(1L);
        // then
        assertThat(optionalMember.isPresent()).isTrue();
        assertThat(optionalMember.get().getEmail()).isEqualTo("email1@domain.com");
        assertThat(optionalMember.get().getPassword()).isEqualTo("password1");
    }
}