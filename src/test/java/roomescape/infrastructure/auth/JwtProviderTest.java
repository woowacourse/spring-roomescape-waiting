package roomescape.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Date;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.member.MemberPayload;
import roomescape.repository.MemberRepository;
import roomescape.util.DateUtil;

@Sql("/member-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    MemberRepository memberRepository;

    @Value("${security.jwt.token.secret-key}")
    String secretKey;

    @Test
    void 사용자의_정보가_포함된_토큰을_생성() {
        //given
        Member member = memberRepository.findById(1L).orElseThrow();
        MemberPayload memberPayLoad = createMemberPayLoad(member);

        //when
        String token = jwtProvider.createToken(memberPayLoad);

        //then
        Claims claims = jwtProvider.getClaims(token);
        Long id = Long.parseLong(jwtProvider.getSubject(token));
        String name = claims.get("name", String.class);
        String email = claims.get("email", String.class);
        Role role = Role.valueOf(claims.get("role", String.class));

        assertAll(
                () -> assertThat(member.getId()).isEqualTo(id),
                () -> assertThat(member.getName()).isEqualTo(name),
                () -> assertThat(member.getEmail()).isEqualTo(email),
                () -> assertThat(member.getRole()).isEqualTo(role)
        );
    }

    @Test
    void 유효한_토큰일_경우_true() {
        //given
        Member member = memberRepository.findById(1L).orElseThrow();
        MemberPayload memberPayLoad = createMemberPayLoad(member);
        String validToken = jwtProvider.createToken(memberPayLoad);

        //when
        boolean result = jwtProvider.isValidateToken(validToken);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 유효한_토큰이_아닐_경우_false() {
        //given
        Member member = memberRepository.findById(1L).orElseThrow();
        MemberPayload memberPayLoad = createMemberPayLoad(member);
        String expiredToken = createExpiredToken(memberPayLoad);

        //when
        boolean result = jwtProvider.isValidateToken(expiredToken);

        //then
        assertThat(result).isFalse();
    }

    private MemberPayload createMemberPayLoad(Member member) {
        return new MemberPayload(
                member.getId().toString(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }

    public String createExpiredToken(MemberPayload memberPayload) {
        Date currentTime = DateUtil.getCurrentTime();
        Date expirationTime = new Date(currentTime.getTime() - 10000);

        return Jwts.builder()
                .setSubject(memberPayload.id())
                .setExpiration(expirationTime)
                .claim("name", memberPayload.name())
                .claim("email", memberPayload.email())
                .claim("role", memberPayload.role().name())
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }
}
