package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;

@Sql("/member-test-data.sql")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 등록된_이메일로_멤버_조회() {
        //given
        Email email = new Email("test@email.com");

        //when
        Member member = memberRepository.findByEmail(email).orElseThrow();

        //then
        assertThat(member.getEmail()).isEqualTo(email.getEmail());
    }
}
