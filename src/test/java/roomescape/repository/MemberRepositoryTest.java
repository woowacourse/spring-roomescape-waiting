package roomescape.repository;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.Role;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("이메일을 통해 저장된 모든 Member 를 가져온다")
    void test1() {
        // given
        String email = "email@gmail.com";

        Member member = memberRepository.save(new Member(
                "히로",
                email,
                "password"
                , Role.ADMIN)
        );

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(email);

        // then
        assertAll(
                () -> assertThat(foundMember).isPresent(),
                () -> assertThat(foundMember.get().getEmail()).isEqualTo(email)
        );
    }
}
