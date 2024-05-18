package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.common.RepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.USER_TOMMY;

class MemberRepositoryTest extends RepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("사용자를 저장한다.")
    void save() {
        // given
        Member member = USER_MIA();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getId()).isNotNull();
    }

    @Test
    @DisplayName("사용자의 이메일은 중복될 수 없다.")
    void saveWithDuplicatedEmail() {
        // given
        memberRepository.save(USER_MIA());

        // when & then
        assertThatThrownBy(() -> memberRepository.save(USER_MIA()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다.")
    void findByEmail() {
        // given
        memberRepository.save(USER_MIA());

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(new Email(MIA_EMAIL));

        // then
        assertThat(foundMember).isNotEmpty();
    }

    @Test
    @DisplayName("Id로 사용자를 조회한다.")
    void findById() {
        // given
        Member member = memberRepository.save(USER_MIA());

        // when
        Optional<Member> foundMember = memberRepository.findById(member.getId());

        // then
        assertThat(foundMember).isNotEmpty();
    }

    @Test
    @DisplayName("사용자 목록을 조회한다.")
    void findAll() {
        // given
        memberRepository.save(USER_MIA());
        memberRepository.save(USER_TOMMY());

        // when
        List<Member> members = memberRepository.findAll();

        // then
//        assertThat(members).hasSize(2);
        for (Member member : members) {
            System.out.println(member.getName());
        }
    }
}
