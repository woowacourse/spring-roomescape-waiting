package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialMemberFixture.ADMIN;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialMemberFixture.MEMBER_3;
import static roomescape.InitialMemberFixture.NOT_SAVED_MEMBER;
import static roomescape.InitialMemberFixture.NO_RESERVATION_MEMBER;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.NotFoundException;
import roomescape.member.domain.Member;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 Member를 찾는다.")
    void findAll() {
        Iterable<Member> found = memberRepository.findAll();

        assertThat(found).containsExactly(MEMBER_1, MEMBER_2, MEMBER_3, NO_RESERVATION_MEMBER, ADMIN);
    }

    @Test
    @DisplayName("id에 맞는 Member를 찾는다.")
    void findById() {
        Member found = memberRepository.findById(MEMBER_1.getId()).get();

        assertThat(found).isEqualTo(MEMBER_1);
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void emptyIfNotExistId() {
        Optional<Member> member = memberRepository.findById(-1L);

        assertThat(member.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("id에 맞는 Member를 찾는다.")
    void getById() {
        Member found = memberRepository.getById(MEMBER_1.getId());

        assertThat(found).isEqualTo(MEMBER_1);
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 예외가 발생한다.")
    void throwExceptionIfNotExistId() {
        assertThatThrownBy(() -> memberRepository.getById(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("email에 맞는 Member를 찾는다.")
    void findByEmail() {
        Member found = memberRepository.findByEmail(MEMBER_1.getEmail()).get();

        assertThat(found).isEqualTo(MEMBER_1);
    }

    @Test
    @DisplayName("존재하지 않는 email이 들어오면 빈 Optional 객체를 반환한다.")
    void emptyIfNotExistEmail() {
        Optional<Member> member = memberRepository.findByEmail(NOT_SAVED_MEMBER.getEmail());

        assertThat(member.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("id에 맞는 Member를 제거한다.")
    void delete() {
        memberRepository.deleteById(NO_RESERVATION_MEMBER.getId());

        assertThat(memberRepository.findById(NO_RESERVATION_MEMBER.getId()).isEmpty()).isTrue();
    }
}
