package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthDto;
import roomescape.service.dto.MemberInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/truncate.sql")
@SpringBootTest
class AuthServiceTest {

    private static final AuthDto userDto = new AuthDto("treeboss@gmail.com", "treeboss123!");

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.saveAll(List.of(
                new Member("에버", "treeboss@gmail.com", "treeboss123!", Role.USER),
                new Member("우테코", "wtc@gmail.com", "wtc123!!", Role.ADMIN)));
    }

    @DisplayName("사용자 정보를 통해 JWT 토큰을 생성한다.")
    @Test
    void should_create_token() {
        AuthDto authDto = new AuthDto(userDto.getEmail().getEmail(), userDto.getPassword().getPassword());

        String accessToken = authService.createToken(authDto);

        MemberInfo memberInfo = authService.checkToken(accessToken);
        assertThat(accessToken).isNotBlank();
        assertThat(memberInfo.getId()).isEqualTo(1L);
    }

    @DisplayName("토큰을 통해 사용자 정보를 조회한다.")
    @Test
    void should_check_login_state() {
        String token = authService.createToken(userDto);

        MemberInfo loginMember = authService.checkToken(token);

        assertThat(loginMember.getId()).isEqualTo(1L);
    }
}