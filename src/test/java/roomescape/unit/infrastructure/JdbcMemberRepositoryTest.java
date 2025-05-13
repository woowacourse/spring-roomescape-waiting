package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.infrastructure.JdbcMemberRepository;

@JdbcTest
class JdbcMemberRepositoryTest {

    private final JdbcMemberRepository memberRepository;

    @Autowired
    public JdbcMemberRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.memberRepository = new JdbcMemberRepository(jdbcTemplate);
    }

    @Test
    void 회원_저장() {
        // given
        Member member = new Member(null, "name1", "eamil1@domain.com", "password1", Role.MEMBER);
        // when
        memberRepository.save(member);
        // then
        List<Member> allMembers = memberRepository.findAll();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allMembers).hasSize(1);
        soft.assertThat(allMembers.getFirst().getName()).isEqualTo("name1");
        soft.assertAll();
    }

    @Test
    void 회원_전체_조회() {
        // given
        Member member1 = new Member(null, "name1", "eamil1@domain.com", "password1", Role.MEMBER);
        Member member2 = new Member(null, "name2", "eamil2@domain.com", "password2", Role.MEMBER);
        memberRepository.save(member1);
        memberRepository.save(member2);
        // when
        List<Member> allMembers = memberRepository.findAll();
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allMembers).hasSize(2);
        soft.assertThat(allMembers.getFirst().getName()).isEqualTo("name1");
        soft.assertThat(allMembers.get(1).getName()).isEqualTo("name2");
        soft.assertAll();
    }

    @Test
    void 회원_id로_조회() {
        // given
        Member savedMember1 = memberRepository.save(
                new Member(null, "name1", "eamil1@domain.com", "password1", Role.MEMBER)
        );
        Member savedMember2 = memberRepository.save(
                new Member(null, "name2", "eamil2@domain.com", "password2", Role.MEMBER)
        );
        // when
        Optional<Member> optionalMember = memberRepository.findById(savedMember1.getId());
        // then
        assertThat(optionalMember).isPresent();
        assertThat(optionalMember.get().getName()).isEqualTo("name1");
    }

    @Test
    void 회원_id로_조회_없을_경우_빈_값() {
        // when
        Optional<Member> optionalMember = memberRepository.findById(100000L);
        // then
        assertThat(optionalMember).isEmpty();
    }

    @Test
    void 회원을_이메일로_조회() {
        // given
        Member savedMember1 = memberRepository.save(
                new Member(null, "name1", "eamil1@domain.com", "password1", Role.MEMBER)
        );
        Member savedMember2 = memberRepository.save(
                new Member(null, "name2", "eamil2@domain.com", "password2", Role.MEMBER)
        );
        // when
        Optional<Member> optionalMember = memberRepository.findByEmail(savedMember1.getEmail());
        // then
        assertThat(optionalMember).isPresent();
        assertThat(optionalMember.get().getName()).isEqualTo("name1");
    }

    @Test
    void 회원을_이메일로_조회_없을_경우_빈_값() {
        // when
        Optional<Member> optionalMember = memberRepository.findByEmail("nobodyEmail@domain.com");
        // then
        assertThat(optionalMember).isEmpty();
    }
}