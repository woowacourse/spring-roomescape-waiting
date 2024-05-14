package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberInfo;
import roomescape.domain.member.Role;
import roomescape.repository.rowmapper.MemberRowMapper;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@JdbcTest
class JdbcMemberRepositoryTest {

    @Autowired
    DataSource dataSource;
    private JdbcMemberRepository memberRepository;

    private final Member member1 = new Member(null, "t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member member2 = new Member(null, "t2@t2.com", "124", "재즈", "MEMBER");
    private final Member member3 = new Member(null, "t3@t3.com", "125", "재즈덕", "MEMBER");

    @BeforeEach
    void setUp() {
        memberRepository = new JdbcMemberRepository(dataSource, new MemberRowMapper());
    }

    @DisplayName("회원을 저장한다.")
    @Test
    void save_member() {
        Member member = memberRepository.insertMember(member1);

        assertAll(
                () -> assertThat(member.getId()).isEqualTo(1),
                () -> assertThat(member.getName()).isEqualTo("러너덕"),
                () -> assertThat(member.getEmail()).isEqualTo("t1@t1.com"),
                () -> assertThat(member.getRole()).isEqualTo(Role.valueOf("MEMBER"))
        );
    }

    @Test
    @DisplayName("회원 존재 여부를 이메일로 판단한다.")
    void is_Exist_member_by_email() {
        Member member = memberRepository.insertMember(member1);

        boolean exist = memberRepository.isMemberExistsByEmail(member.getEmail());
        boolean notExist = memberRepository.isMemberExistsByEmail(member2.getEmail());

        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(notExist).isFalse()
        );
    }

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void find_member_by_email() {
        Member member = memberRepository.insertMember(member1);

        Optional<Member> memberById = memberRepository.findMemberByEmail(member.getEmail());
        Member findMember = memberById.get();

        assertAll(
                () -> assertThat(findMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(findMember.getRole()).isEqualTo(member.getRole()),
                () -> assertThat(findMember.getEmail()).isEqualTo(member.getEmail())
        );
    }

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void find_member_by_id() {
        Member member = memberRepository.insertMember(member1);

        Optional<Member> memberById = memberRepository.findMemberById(member.getId());
        Member findMember = memberById.get();

        assertAll(
                () -> assertThat(findMember.getId()).isEqualTo(member.getId()),
                () -> assertThat(findMember.getRole()).isEqualTo(member.getRole()),
                () -> assertThat(findMember.getEmail()).isEqualTo(member.getEmail())
        );
    }

    @DisplayName("저장된 모든 회원 정보를 가져온다.")
    @Test
    void find_all_members() {
        Member savedMember1 = memberRepository.insertMember(member1);
        Member savedMember2 = memberRepository.insertMember(member2);
        Member savedMember3 = memberRepository.insertMember(member3);

        List<MemberInfo> allMemberNames = memberRepository.findAllMemberNames();

        assertAll(
                () -> assertThat(allMemberNames.get(0).getName()).isEqualTo(savedMember1.getName()),
                () -> assertThat(allMemberNames.get(1).getName()).isEqualTo(savedMember2.getName()),
                () -> assertThat(allMemberNames.get(2).getName()).isEqualTo(savedMember3.getName()),
                () -> assertThat(allMemberNames.size()).isEqualTo(3)
        );
    }
}
