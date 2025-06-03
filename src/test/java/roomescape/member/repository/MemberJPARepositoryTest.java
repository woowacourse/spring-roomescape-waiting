package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-member-data.sql"})
public class MemberJPARepositoryTest {

    private final static MemberEmail MEMBER_EMAIL = new MemberEmail("aaa@gmail.com");
    private final static MemberName MEMBER_NAME = new MemberName("사용자1");
    private final static String MEMBER_PASSWORD = "1234";

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("모든 멤버 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<Member> members = memberRepository.findAll();
        // then
        assertThat(members).hasSize(3);
    }

    @DisplayName("이메일, 비밀번호에 맞는 멤버를 조회할 수 있다.")
    @Test
    void testFindByEmailAndPassword() {
        // given
        // when
        Member member = memberRepository.findByEmailAndPassword(MEMBER_EMAIL, MEMBER_PASSWORD)
                .orElseThrow();
        // then
        assertThat(member.getName().getValue()).isEqualTo(MEMBER_NAME.getValue());
    }

    @DisplayName("이메일에 맞는 멤버가 존재하는 지 확인할 수 있다.")
    @Test
    void testExistsByEmail() {
        // given
        // when
        // then
        assertThat(memberRepository.existsByEmail(MEMBER_EMAIL)).isTrue();
        assertThat(memberRepository.existsByEmail(new MemberEmail("test@gmail.com"))).isFalse();
    }

    @DisplayName("이름에 맞는 멤버가 존재하는 지 확인할 수 있다.")
    @Test
    void testExistsByName() {
        // given
        // when
        // then
        assertThat(memberRepository.existsByName(MEMBER_NAME)).isTrue();
        assertThat(memberRepository.existsByName(new MemberName("테스트"))).isFalse();
    }
}
