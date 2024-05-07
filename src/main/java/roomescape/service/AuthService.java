package roomescape.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthDto;
import roomescape.service.dto.MemberInfo;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    private static final String SECRET_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String createToken(AuthDto authDto) {
        Member member = memberRepository.findMemberByEmailAndPassword(authDto.getEmail(), authDto.getPassword())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당하는 계정이 없습니다."));
        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("name", member.getName())
                .claim("email", member.getEmail())
                .claim("role", member.getRole())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    public MemberInfo checkToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        long memberId = Long.parseLong(claims.getSubject());
        String memberName = claims.get("name").toString();
        String memberEmail = claims.get("email").toString();
        Role memberRole = Role.asRole(claims.get("role").toString());
        return new MemberInfo(memberId, memberName, memberEmail, memberRole);
    }
}
