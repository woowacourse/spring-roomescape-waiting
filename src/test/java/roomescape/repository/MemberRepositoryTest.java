package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.repository.impl.MemberRepositoryImpl;
import roomescape.repository.jpa.MemberJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaConfig.class)
@DataJpaTest
public class MemberRepositoryTest {

    private MemberRepository memberRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = new MemberRepositoryImpl(memberJpaRepository);

        member = memberRepository.save(
                new Member("test@example.com", "testPassword", "test", MemberRole.USER)
        );
    }

    @Test
    @DisplayName("사용자의 이메일을 통해 사용자를 찾는다")
    void findByEmailTest() {
        // given
        String email = member.getEmail();

        // when
        final Optional<Member> found = memberRepository.findByEmail(email);

        // then
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getId()).isEqualTo(member.getId())
        );
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자를 검색한다")
    void findByNonExistEmailTest() {
        // given
        String email = "non-exist@example.com";

        // when
        final Optional<Member> found = memberRepository.findByEmail(email);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일이 존재하는 지 확인한다")
    void existByEmailTest() {
        // given
        String existEmail = member.getEmail();
        String nonExistEmail = "non-exist@example.com";

        // when
        final boolean exist = memberRepository.existByEmail(existEmail);
        final boolean nonExist = memberRepository.existByEmail(nonExistEmail);

        // then
        assertThat(exist).isTrue();
        assertThat(nonExist).isFalse();
    }

    @Test
    @DisplayName("이름으로 사용자가 존재하는 지 확인한다")
    void findByNameTest() {
        // given
        String existName = member.getName();
        String nonExistName = "non-exist";

        // when
        final boolean exist = memberRepository.existByName(existName);
        final boolean nonExist = memberRepository.existByName(nonExistName);

        // then
        assertThat(exist).isTrue();
        assertThat(nonExist).isFalse();
    }
}
