package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.util.JpaRepositoryTest;

@JpaRepositoryTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("동일한 이메일인 회원 조회 성공")
    void findByEmail() {
        String email = "asdf1@asdf.com";
        Member member = memberRepository.save(MemberFixture.getOne(email));
        memberRepository.save(MemberFixture.getOne("dk" +email));

        assertThat(memberRepository.findByEmail(new Email(email))).isEqualTo(Optional.of(member));
    }

    @Test
    @DisplayName("동일한 이메일인 회원 조회 실패: 회원 없음")
    void getByEmail_WhenNotExist() {
        String email = "asdf1@asdf.com";
        assertThatThrownBy(() -> memberRepository.getByEmail(new Email(email)))
                .hasMessage("이메일 asdf1@asdf.com에 해당하는 회원이 존재하지 않습니다.")
                .isInstanceOf(NoSuchElementException.class);
    }
}
