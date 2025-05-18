package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.Role;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;


    @DisplayName("이메일에 해당하는 member가 있다면 반환한다.")
    @Test
    void findByEmail() {
        //given
        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        String email = "email@example.com";

        //when
        Optional<Member> actual = memberRepository.findByEmail(email);

        //then
        assertThat(actual.get()).isEqualTo(member);
    }

    @DisplayName("이메일에 해당하는 member가 없다면 빈 값을 반환한다.")
    @Test
    void emptyFindByEmail() {
        //given
        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        String email = "not@example.com";

        //when
        Optional<Member> actual = memberRepository.findByEmail(email);

        //then
        assertThat(actual).isEmpty();
    }

}
