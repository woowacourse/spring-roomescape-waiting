package roomescape.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.member.domain.Role.MEMBER;
import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.RepositoryTest;
import roomescape.domain.member.domain.Member;

class MemberRepositoryImplTest extends RepositoryTest {


    @Autowired
    private JdbcTemplate jdbcTemplate;
    private MemberRepository memberRepository;

    MemberRepositoryImplTest() {

    }

    @BeforeEach
    void setUp() {
        memberRepository = new MemberRepositoryImpl(jdbcTemplate);
        jdbcTemplate.update(
                "insert into member (name, email, password, role) values ('어드민', 'admin@gmail.com', '123456', 'ADMIN')");
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("delete from member");
    }

    @DisplayName("유저를 table에 넣을 수 있습니다.")
    @Test
    void should_insert_member() {
        Member testMember = memberRepository.insert(new Member(null, "testName1", "testEmail1@gmail.com", "123123",
                MEMBER));

        assertThat(testMember.getId()).isNotNull();
    }

    @DisplayName("원하는 id의 Member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_id() {
        Member expectedMember = ADMIN_MEMBER;

        Member actualMember = memberRepository.findById(1L).get();

        assertThat(actualMember).isEqualTo(expectedMember);
    }

    @DisplayName("email과 password로 member를 찾을 수 있습니다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member expectedMember = ADMIN_MEMBER;

        Member actualMember = memberRepository.findByEmailAndPassword("admin@gmail.com", "123456").get();

        assertThat(actualMember).isEqualTo(expectedMember);
    }
}
