package roomescape.member.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberChoco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.util.RepositoryTest;

@DisplayName("사용자 레포지토리 테스트")
class MemberRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다.")
    @Test
    void save() {
        //given & when
        Member member = memberRepository.save(getMemberChoco());

        //then
        assertAll(() -> assertThat(member.getId()).isNotNull(),
                () -> assertThat(member.getName()).isEqualTo(getMemberChoco().getName()),
                () -> assertThat(member.getEmail()).isEqualTo(getMemberChoco().getEmail()),
                () -> assertThat(member.getPassword()).isEqualTo(getMemberChoco().getPassword()),
                () -> assertThat(member.getRole()).isEqualTo(getMemberChoco().getRole()));
    }
}
