package roomescape.member.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.model.Member;
import roomescape.member.model.Role;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(JdbcMemberDao.class)
class JdbcMemberDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JdbcMemberDao jdbcMemberDao;

    @Test
    void DataSource_접근_테스트() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getTables(null, null, "MEMBER", null)
                    .next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 멤버를_추가할_수_있다() {
        // Given
        Member member = Member.generateNormalMember("프리", "phreee@woowa.com", "password");

        // When
        Member newMember = jdbcMemberDao.add(member);

        // Then
        assertAll(() -> {
            assertThat(newMember).isNotNull();
            assertThat(newMember.getName()).isEqualTo("프리");
            assertThat(newMember.getEmail()).isEqualTo("phreee@woowa.com");
            assertThat(newMember.getPassword()).isEqualTo("password");
            assertThat(newMember.getRole()).isEqualTo(Role.NORMAL);
        });
    }

    @Test
    void 전체_멤버를_조회할_수_있다() {
        // Given
        // When
        // Then
        assertThat(jdbcMemberDao.findAll()).hasSize(5);
    }

    @Test
    void 멤버id로_단일_멤버를_조회할_수_있다() {
        // Given
        // When
        Member member = jdbcMemberDao.findById(1L);

        // Then
        assertAll(() -> {
            assertThat(member).isNotNull();
            assertThat(member.getId()).isEqualTo(1L);
        });
    }

    @Test
    void 이메일로_단일_멤버를_조회할_수_있다() {
        // Given
        // When
        Member member = jdbcMemberDao.findByEmail("normal1@normal.com");

        // Then
        assertAll(() -> {
            assertThat(member).isNotNull();
            assertThat(member.getEmail()).isEqualTo("normal1@normal.com");
        });
    }

    @Test
    void 이메일과_패스워드로_단일_멤버를_조회할_수_있다() {
        // Given
        // When
        Member member = jdbcMemberDao.findByEmailAndPassword("normal1@normal.com", "password");

        // Then
        assertAll(() -> {
            assertThat(member).isNotNull();
            assertThat(member.getEmail()).isEqualTo("normal1@normal.com");
            assertThat(member.getPassword()).isEqualTo("password");
        });
    }

    @Test
    void 해당_이메일을_가진_유저가_존재하는지_확인할_수_있다() {
        // Given
        // When
        // Then
        assertAll(() -> {
            assertThat(jdbcMemberDao.existByEmail("normal1@normal.com")).isTrue();
            assertThat(jdbcMemberDao.existByEmail("notexist@email.com")).isFalse();
        });
    }

    @Test
    void 해당_이름을_가진_유저가_존재하는지_확인할_수_있다() {
        // Given
        // When
        // Then
        assertAll(() -> {
            assertThat(jdbcMemberDao.existByName("일반1")).isTrue();
            assertThat(jdbcMemberDao.existByName("notExistName")).isFalse();
        });
    }

}