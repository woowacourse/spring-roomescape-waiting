package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.MemberRepository;

// ('안돌', 'andole@test.com', '123', 'MEMBER')
// ('파랑', 'parang@test.com', '123', 'MEMBER')
// ('리비', 'libienz@test.com','123', 'MEMBER')
// ('메이슨', 'mason@test.com', '123', 'MEMBER')
// ('어드민', 'admin@test.com', '123', 'ADMIN');
@DataJpaTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일이 같은 멤버들을 찾을 수 있다")
    @Test
    void should_find_same_email_member() {
        Optional<Member> findMember = memberRepository.findByEmail(new Email("parang@test.com"));

        assertAll(
                () -> assertThat(findMember).isPresent(),
                () -> assertThat(findMember.get().getName()).isEqualTo("파랑")
        );
    }

    @DisplayName("이메일이 같은 멤버가 존재하는 지 확인할 수 있다.")
    @Test
    void should_check_same_email_member_exists() {
        Email existingEmail = new Email("parang@test.com");
        Email absentEmail = new Email("noExist@test.com");

        assertAll(
                () -> assertThat(memberRepository.existsByEmail(existingEmail)).isTrue(),
                () -> assertThat(memberRepository.existsByEmail(absentEmail)).isFalse()
        );
    }

    @DisplayName("이름이 같은 멤버가 존재하는 지 확인할 수 있다")
    @Test
    void should_check_same_name_member_exists() {
        MemberName existingName = new MemberName("리비");
        MemberName absentName = new MemberName("없는이름");

        assertAll(
                () -> assertThat(memberRepository.existsByMemberName(existingName)).isTrue(),
                () -> assertThat(memberRepository.existsByMemberName(absentName)).isFalse()
        );
    }
}
