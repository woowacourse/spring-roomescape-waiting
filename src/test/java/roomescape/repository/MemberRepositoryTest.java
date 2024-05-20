package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 등록된_이메일로_멤버_조회() {
        //given
        Member member = new Member("ted", "ted@email.com", "123456", Role.USER);
        memberRepository.save(member);

        //when
        Member findMember = memberRepository.findByEmail(new Email(member.getEmail())).orElseThrow();

        //then
        assertThat(findMember.getEmail()).isEqualTo(member.getEmail());
    }
}
