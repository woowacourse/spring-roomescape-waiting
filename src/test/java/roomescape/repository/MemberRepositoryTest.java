package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import roomescape.domain.Member;
import roomescape.fixture.MemberFixtures;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일과 비밀번호로 회원 정보를 조회한다.")
    @Test
    void findByEmailAndPassword() {
        String email = "daon@email.com";
        String password = "1234";
        Member daon = MemberFixtures.createAdminMemberDaon(email, password);
        memberRepository.save(daon);

        Optional<Member> result = memberRepository.findByEmailAndPassword(email, password);
        assertAll(
                () -> assertThat(result).isPresent()
        );
    }

    @DisplayName("이메일과 비밀번호와 동일한 회원 정보가 존재하지 않으면 비어있다.")
    @Test
    void findByEmailAndPasswordWhenNotExist() {
        String email = "daon@email.com";
        String password = "1234";

        Optional<Member> result = memberRepository.findByEmailAndPassword(email, password);
        assertAll(
                () -> assertThat(result).isEmpty()
        );
    }

    @DisplayName("이메일과 동일한 회원 정보를 조회한다.")
    @Test
    void findByEmail() {
        String email = "daon@email.com";
        Member daon1 = MemberFixtures.createAdminMemberDaon(email);
        memberRepository.save(daon1);

        Optional<Member> result = memberRepository.findByEmail(email);
        assertAll(
                () -> assertThat(result).isPresent()
        );
    }

    @DisplayName("이메일과 동일한 회원 정보가 존재하지 않으면 비어있다.")
    @Test
    void findByEmailWhenNotExist() {
        String email = "daon@email.com";

        Optional<Member> result = memberRepository.findByEmail(email);
        assertAll(
                () -> assertThat(result).isEmpty()
        );
    }

    @DisplayName("id로 회원 정보를 조회한다.")
    @Test
    void getMemberById() {
        String email = "daon@email.com";
        Member daon = MemberFixtures.createAdminMemberDaon(email);
        Member savedMember = memberRepository.save(daon);

        Member member = memberRepository.getMemberById(savedMember.getId());
        assertThat(member.getEmail()).isEqualTo(email);
    }

    @DisplayName("id가 존재하지 않는다면 예외가 발생한다.")
    @Test
    void getMemberByIdWhenNotExist() {
        assertThatThrownBy(() -> memberRepository.getMemberById(-1L))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원 입니다");
    }
}
