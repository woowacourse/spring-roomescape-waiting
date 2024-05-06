package roomescape.infrastructure.role;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.infrastructure.member.JdbcMemberRepository;

@JdbcTest
@Import({JdbcRoleRepository.class, JdbcMemberRepository.class})
class JdbcRoleRepositoryTest {

    @Autowired
    private JdbcRoleRepository jdbcRoleRepository;

    @Autowired
    private JdbcMemberRepository jdbcMemberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("멤버 역할을 저장한다.")
    void saveRoleTest() {
        Member member = new Member("name", "email@test.com", "12341234");
        Member savedMember = jdbcMemberRepository.save(member);
        MemberRole role = new MemberRole(savedMember, Role.ADMIN);
        jdbcRoleRepository.save(role);
        assertThat(getRowCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("멤버 id로 어드민 여부를 조회한다.")
    void isAdminByMemberIdTest() {
        Member member = new Member("name", "email@test.com", "12341234");
        long memberId = jdbcMemberRepository.save(member).getId();
        jdbcTemplate.update("insert into role (member_id, role) values (?, ?)", memberId, Role.ADMIN.getRoleName());
        boolean isAdmin = jdbcRoleRepository.isAdminByMemberId(memberId);
        assertThat(isAdmin).isTrue();
    }

    int getRowCount() {
        return jdbcTemplate.queryForObject("select count(*) from role", Integer.class);
    }
}
