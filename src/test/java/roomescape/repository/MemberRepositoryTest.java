package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.exception.MemberNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 회원 목록을 조회한다.")
    void findAll() {
        final List<Member> expected = List.of(
                new Member(1L, null, null, null, null),
                new Member(2L, null, null, null, null),
                new Member(3L, null, null, null, null)
        );

        assertThat(memberRepository.findAll()).isEqualTo(expected);
    }

    @Test
    @DisplayName("멤버를 저장한다.")
    void save() {
        final Member saved = memberRepository.save(new Member(null, "명오", "h9@mail.com", "1234", Role.ADMIN));

        final Member expected = new Member(4L, null, null, null, null);
        assertThat(saved).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않는 id를 조회할 경우 예외가 발생한다.")
    void fetchByIdNotPresent() {
        final long id = 100L;

        assertThatThrownBy(() -> memberRepository.fetchById(id)).isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 id를 조회할 경우 예외가 발생한다.")
    void fetchByEmailNotPresent() {
        final String email = "notfound@email.com";

        assertThatThrownBy(() -> memberRepository.fetchByEmail(email)).isInstanceOf(MemberNotFoundException.class);
    }
}
