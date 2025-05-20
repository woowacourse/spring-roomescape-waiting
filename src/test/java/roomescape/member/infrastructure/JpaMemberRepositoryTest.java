package roomescape.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DataJpaTest
public class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository repository;

    @Test
    @DisplayName("정상적으로 저장되어 id를 반환하는지 확인한다.")
    void save_test() {
        // given
        Member member = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "password");
        // when
        Member save = repository.save(member);
        // then
        assertThat(save.getId()).isNotNull();
    }

    @Test
    @DisplayName("아이디로 회원 조회 테스트")
    void findById_test() {
        // given
        Member member = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "password");
        Member save = repository.save(member);
        // when
        Optional<Member> findMember = repository.findById(save.getId());
        // then
        assertAll(
                () -> assertThat(findMember.isPresent()).isTrue(),
                () -> assertThat(findMember.get().getId()).isEqualTo(save.getId()),
                () -> assertThat(findMember.get().getName()).isEqualTo(member.getName()),
                () -> assertThat(findMember.get().getEmail()).isEqualTo(member.getEmail()),
                () -> assertThat(findMember.get().getPassword()).isEqualTo(member.getPassword())
        );
    }

    @ParameterizedTest
    @CsvSource({"member@naver.com,true", "falseMember@naver.com,false"})
    @DisplayName("이메일 존재 확인 테스트")
    void existsByEmail_test(String email, boolean expected) {
        // given
        Member member = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "password");
        repository.save(member);
        // when
        boolean existed = repository.existsByEmail(email);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"member@naver.com,password,true", "member@naver.com,x,false", "x,password,false", "x,x,false"})
    @DisplayName("이메일, 비밀번호 존재 확인 테스트")
    void existsByEmailAndPassword(String email, String password, boolean expected) {
        // given
        Member member = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "password");
        repository.save(member);
        // when
        Optional<Member> findMember = repository.findByEmailAndPassword(email, password);
        // then
        assertThat(findMember.isPresent()).isEqualTo(expected);
    }

    @Test
    @DisplayName("모든 회원 조회 테스트")
    void findAll_test() {
        // given
        Member member1 = TestFixture.createMemberWithoutId("멤버1", "member@naver.com", "password");
        repository.save(member1);
        Member member2 = TestFixture.createMemberWithoutId("멤버2", "member@naver.com", "password");
        repository.save(member2);
        // when
        List<Member> members = repository.findAll();
        // then
        assertThat(members).hasSize(2);
    }
}
