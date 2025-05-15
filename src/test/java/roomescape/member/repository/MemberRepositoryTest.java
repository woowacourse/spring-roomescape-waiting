package roomescape.member.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버_등록() {
        // given
        final Member member = new Member("testUser", "east@email.com", "password", Role.USER);

        // when
        memberRepository.save(member);

        // then
        assertThat(memberRepository.findAll()).hasSize(1);
    }

    @Test
    void 멤버_전부_찾기() {
        // when & then
        assertThat(memberRepository.findAll()).hasSize(0);
    }

    @Test
    void 아이디_기준으로_멤버_찾기() {
        // given
        final Member member = new Member("testUser", "east@email.com", "password", Role.USER);
        final Member savedMember = memberRepository.save(member);

        // when
        final Member foundMember = memberRepository.findById(savedMember.getId()).orElse(null);

        // then
        assertThat(savedMember.getId()).isEqualTo(foundMember.getId());
    }

    @Test
    void 이메일_기준으로_멤버_찾기() {
        // given
        final Member member = new Member("testUser", "east@email.com", "password", Role.USER);
        final Member savedMember = memberRepository.save(member);

        //when
        final Member foundMember = memberRepository.findByEmail(savedMember.getEmail()).orElse(null);

        // then
        assertThat(savedMember.getId()).isEqualTo(foundMember.getId());
    }

    @Test
    void 이메일_기준으로_멤버_이름_찾기() {
        // given
        final Member member = new Member("testUser", "east@email.com", "password", Role.USER);
        final Member savedMember = memberRepository.save(member);

        // when
        final String foundName = memberRepository.findNameByEmail(savedMember.getEmail()).orElse(null);

        // then
        assertThat(savedMember.getName()).isEqualTo(foundName);
    }

    @Test
    void 이메일_비밀번호_존재하는지_확인() {
        // given
        final Member member = new Member("testUser", "east@email.com", "password", Role.USER);
        final Member savedMember = memberRepository.save(member);

        // when
        final boolean exists = memberRepository.existsByEmailAndPassword(savedMember.getEmail(),
                savedMember.getPassword());

        // then
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "east@email.com, password23",
            "hello@email.com, 1234",
            "email@email.com, password"
    })
    void 이메일_비밀번호_존재_확인_실패(final String email, final String password) {
        // when
        final boolean exists = memberRepository.existsByEmailAndPassword(email, password);

        // then
        assertThat(exists).isFalse();
    }
}