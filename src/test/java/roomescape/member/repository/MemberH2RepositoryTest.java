package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_2;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_3;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_4;
import static roomescape.InitialMemberFixture.NOT_SAVED_LOGIN_MEMBER;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.LoginMember;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class MemberH2RepositoryTest {

    @Autowired
    private MemberH2Repository memberH2Repository;

    @Test
    @DisplayName("모든 Member을 찾는다.")
    void findAll() {
        List<LoginMember> found = memberH2Repository.findAll();

        assertThat(found).containsExactly(LOGIN_MEMBER_1, LOGIN_MEMBER_2, LOGIN_MEMBER_3, LOGIN_MEMBER_4);
    }

    @Test
    @DisplayName("id에 맞는 Member을 찾는다.")
    void findById() {
        LoginMember found = memberH2Repository.findById(LOGIN_MEMBER_1.getId()).get();

        assertThat(found).isEqualTo(LOGIN_MEMBER_1);
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void emptyIfNotExistId() {
        Optional<LoginMember> member = memberH2Repository.findById(-1L);

        assertThat(member.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("email에 맞는 Member를 찾는다.")
    void findByEmail() {
        LoginMember found = memberH2Repository.findByEmail(LOGIN_MEMBER_1.getEmail()).get();

        assertThat(found).isEqualTo(LOGIN_MEMBER_1);
    }

    @Test
    @DisplayName("존재하지 않는 email이 들어오면 빈 Optional 객체를 반환한다.")
    void emptyIfNotExistEmail() {
        Optional<LoginMember> member = memberH2Repository.findByEmail(NOT_SAVED_LOGIN_MEMBER.getEmail());

        assertThat(member.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("id에 맞는 Member을 제거한다.")
    void delete() {
        memberH2Repository.delete(NOT_SAVED_LOGIN_MEMBER.getId());

        assertThat(memberH2Repository.findById(NOT_SAVED_LOGIN_MEMBER.getId()).isEmpty()).isTrue();
    }
}
