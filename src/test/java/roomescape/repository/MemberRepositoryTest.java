package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.dto.SignupRequest;

@DataJpaTest
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("Db에 등록된 모든 회원 목록을 조회한다.")
    @Test
    void given_when_findAll_then_returnReservations() {
        //given, when, then
        assertThat(memberRepository.findAll()).hasSize(2);
    }

    @DisplayName("Db에 회원 정보를 저장한다.")
    @Test
    void given_reservation_when_create_then_returnNoting() {
        //given
        SignupRequest signupRequest = new SignupRequest("ash@test.com", "123456", "ash");
        Member expected = signupRequest.toEntity(new Password("hashvalue", "salt"));
        //when
        Member savedMember = memberRepository.save(expected);
        //then
        assertThat(savedMember).isEqualTo(expected);
    }
}
