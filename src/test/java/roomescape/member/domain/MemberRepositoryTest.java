package roomescape.member.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.RepositoryTest;
import roomescape.member.persistence.MemberDao;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;

class MemberRepositoryTest extends RepositoryTest {
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        this.memberRepository = new MemberDao(dataSource);
    }

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
    @DisplayName("이메일로 사용자를 조회한다.")
    void findByEmail() {
        // given
        memberRepository.save(USER_MIA());

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(MIA_EMAIL);

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
        assertThat(members).hasSize(2);
    }
}
