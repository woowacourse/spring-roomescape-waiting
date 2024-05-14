package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
}
