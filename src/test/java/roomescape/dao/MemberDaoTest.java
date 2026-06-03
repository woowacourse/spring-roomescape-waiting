package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;

@JdbcTest
@Import(MemberDao.class)
class MemberDaoTest {

    @Autowired
    private MemberDao memberDao;

    @DisplayName("회원을 저장하고 ID로 조회하면 도메인 객체로 매핑된다.")
    @Test
    void saveAndFindById() {
        Member member = new Member(null, "roro", "러로", "password", Role.USER);

        Long id = memberDao.save(member);

        assertThat(memberDao.findById(id)).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(id);
            assertThat(found.getLoginId()).isEqualTo("roro");
            assertThat(found.getName()).isEqualTo("러로");
            assertThat(found.getPassword()).isEqualTo("password");
            assertThat(found.getRole()).isEqualTo(Role.USER);
        });
    }

    @DisplayName("로그인 ID로 회원을 조회한다.")
    @Test
    void findByLoginId() {
        memberDao.save(new Member(null, "admin", "관리자", "password", Role.ADMIN));

        assertThat(memberDao.findByLoginId("admin")).hasValueSatisfying(found -> {
            assertThat(found.getName()).isEqualTo("관리자");
            assertThat(found.getRole()).isEqualTo(Role.ADMIN);
        });
        assertThat(memberDao.findByLoginId("unknown")).isEmpty();
    }

    @DisplayName("역할로 회원 목록을 이름, 로그인 ID 순으로 조회한다.")
    @Test
    void findByRole() {
        memberDao.save(new Member(null, "admin", "관리자", "password", Role.ADMIN));
        memberDao.save(new Member(null, "roro", "러로", "password", Role.USER));
        memberDao.save(new Member(null, "brown", "현미밥", "password", Role.USER));

        assertThat(memberDao.findByRole(Role.USER))
                .extracting(Member::getName)
                .containsExactly("러로", "현미밥");
    }

    @DisplayName("로그인 ID 존재 여부를 조회한다.")
    @Test
    void existsByLoginId() {
        memberDao.save(new Member(null, "roro", "러로", "password", Role.USER));

        assertThat(memberDao.existsByLoginId("roro")).isTrue();
        assertThat(memberDao.existsByLoginId("unknown")).isFalse();
    }

    @DisplayName("로그인 ID는 유니크 제약조건을 가진다.")
    @Test
    void uniqueLoginId() {
        memberDao.save(new Member(null, "roro", "러로", "password", Role.USER));

        assertThatThrownBy(() -> memberDao.save(new Member(null, "roro", "현미밥", "password", Role.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("역할은 필수다.")
    @Test
    void requiredRole() {
        assertThatThrownBy(() -> memberDao.save(new Member(null, "roro", "러로", "password", null)))
                .isInstanceOf(RoomescapeException.class);
    }
}
