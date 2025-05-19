package roomescape.auth.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.exception.DataNotFoundException;
import roomescape.fake.FakeMemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepositoryInterface;

public class AuthServiceTest {

    private final MemberRepositoryInterface memberRepository = new FakeMemberRepository();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=",
            3600000L
    );
    private final AuthService authService = new AuthService(memberRepository, jwtTokenProvider);

    @Test
    void 토큰_기준으로_이름_찾기_성공() {
        //given
        final String email = "east@email.com";
        memberRepository.save(
                new Member("이스트", email, "1234", Role.ADMIN)
        );
        final String token = jwtTokenProvider.createToken(email);

        //when
        final String name = authService.findNameByToken(token);

        //then
        Assertions.assertThat(name).isEqualTo("이스트");
    }

    @Test
    void 토큰_기준으로_이름_찾기_실패() {
        //given
        final String token = jwtTokenProvider.createToken("fake");

        //when & then
        Assertions.assertThatThrownBy(
                () -> authService.findNameByToken(token)
        ).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 토큰_만들기_성공() {
        //given
        final String email = "east@email.com";
        final String password = "1234";
        memberRepository.save(
                new Member("이스트", email, password, Role.ADMIN)
        );

        //when
        final String token = authService.createToken(email, password);

        //then
        Assertions.assertThat(token).isNotNull();
    }

    @Test
    void 토큰_만들기_실패() {
        //given
        final String invalidEmail = "fake";
        final String invalidPassword = "fake";

        //when & then
        Assertions.assertThatThrownBy(
                () -> authService.createToken(invalidEmail, invalidPassword)
        ).isInstanceOf(DataNotFoundException.class);
    }
}
