package roomescape.member.repository.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DataJpaTest
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void 멤버_생성_확인() {
        Member member = createMember();
        jpaMemberRepository.save(member);
        assertThatCode(() -> jpaMemberRepository.findById(member.getId())).doesNotThrowAnyException();
    }

    @Test
    void 이메일로_멤버_찾기() {
        Member member = createMember();
        jpaMemberRepository.save(member);

        Member savedMember = jpaMemberRepository.findByEmail(member.getEmail()).orElseThrow();

        assertEquals(member.getName(), savedMember.getName());
        assertEquals(member.getEmail(), savedMember.getEmail());
    }

    @Test
    void 이름으로_멤버_찾기() {
        Member member = createMember();
        jpaMemberRepository.save(member);
        Member savedMember = jpaMemberRepository.findByName(member.getName()).orElseThrow();

        assertEquals(member.getName(), savedMember.getName());
        assertEquals(member.getEmail(), savedMember.getEmail());
    }

    private Member createMember() {
        return new Member("test", "test@test.com", MemberRole.USER, "testpassword");
    }
}
