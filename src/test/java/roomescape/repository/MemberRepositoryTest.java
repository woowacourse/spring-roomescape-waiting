package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일을 기준으로 유저를 조회한다.")
    @ParameterizedTest
    @CsvSource({"testDB@email.com,어드민", "test2DB@email.com,사용자"})
    void findByEmail(String email, String name) {
        //when
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        //then
        assertThat(member.getName()).isEqualTo(name);
    }

    @DisplayName("이메일과 비밀번호를 기준으로 유저를 조회한다.")
    @ParameterizedTest
    @CsvSource({"testDB@email.com,1234,어드민", "test2DB@email.com,1234,사용자"})
    void findByEmailAndPassword(String email, String password, String name) {
        //when
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        //then
        assertThat(member.getName()).isEqualTo(name);
    }
}
