package roomescape.member.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.model.Member;
import roomescape.member.model.MemberEmail;
import roomescape.member.model.MemberRole;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)   // TODO : 더 좋은 데이터 초기화 방식 고민
class InMemoryMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("전체 회원 정보를 조회한다.")
    @Test
    void findAllTest() {
        // When
        final List<Member> members = memberRepository.findAll();

        // Then
        assertThat(members).hasSize(5);
    }

    @DisplayName("주어진 id에 일치한 member를 찾아 반환한다.")
    @Test
    void findByIdTest() {
        // Given
        final Long memberId = 1L;

        // When
        final Optional<Member> member = memberRepository.findById(memberId);

        // Then
        assertThat(member).isPresent();
    }

    @DisplayName("member를 저장한다.")
    @Test
    void saveTest() {
        // Given
        final long memberId = 6L;
        final MemberRole role = MemberRole.USER;
        final String password = "kellyPw1234";
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final Member member = new Member(memberId, role, password, name, email);

        // When
        final Member savedMember = memberRepository.save(member);

        // Then
        Assertions.assertAll(
                () -> assertThat(savedMember.getId()).isEqualTo(memberId),
                () -> assertThat(savedMember.getRole()).isEqualTo(role),
                () -> assertThat(savedMember.getPassword().getValue()).isEqualTo(password),
                () -> assertThat(savedMember.getEmail().getValue()).isEqualTo(email),
                () -> assertThat(savedMember.getName().getValue()).isEqualTo(name)
        );
    }

    @DisplayName("주어진 이메일에 일치한 member를 찾아 반환한다.")
    @Test
    void findByEmailTest() {
        // Given
        final MemberEmail memberEmail = new MemberEmail("user@mail.com");

        // When
        final Optional<Member> member = memberRepository.findByEmail(memberEmail);

        // Then
        assertThat(member).isPresent();
    }

    @DisplayName("이미 존재하는 이메일은이 여부를 반환한다.")
    @Test
    void existsByEmailTest() {
        // Given
        final MemberEmail memberEmail = new MemberEmail("user@mail.com");

        // When
        final boolean isExist = memberRepository.existsByEmail(memberEmail);

        // Then
        assertThat(isExist).isTrue();
    }
}
