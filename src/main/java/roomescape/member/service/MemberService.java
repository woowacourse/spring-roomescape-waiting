package roomescape.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.login.dto.LoginRequest;
import roomescape.member.domain.*;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.SignUpRequest;
import roomescape.member.repository.MemberJpaRepository;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class MemberService {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberJpaRepository memberJpaRepository, PasswordEncoder passwordEncoder) {
        this.memberJpaRepository = memberJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Member getLoginMemberById(Long memberId) {
        return memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원 id입니다. memberId = " + memberId));
    }

    @Transactional(readOnly = true)
    public List<MemberIdNameResponse> findMembers() {
        return memberJpaRepository.findAll()
                .stream()
                .map(MemberIdNameResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public String createMemberToken(LoginRequest loginRequest) throws AuthenticationException {
        Member member = memberJpaRepository.findByEmail(new Email(loginRequest.email()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (passwordEncoder.matches(loginRequest.password(), member.getPassword().password())) {
            return parseToToken(member);
        }
        throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    private String parseToToken(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(validityInMilliseconds);

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("name", member.getName().name())
                .claim("email", member.getEmail().email())
                .claim("role", member.getRole().name())
                .claim("password", member.getPassword().password())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public boolean isAdminToken(String token) throws AuthenticationException {
        Member member = parseTokenToLoginMember(token);

        return member.isAdmin();
    }

    private Member parseTokenToLoginMember(String token) throws AuthenticationException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new Member(
                    Long.valueOf(claims.getSubject()),
                    (String) claims.get("name"),
                    (String) claims.get("email"),
                    (String) claims.get("role"),
                    (String) claims.get("password")
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
    }

    public Member getLoginMemberByToken(String token) throws AuthenticationException {
        return parseTokenToLoginMember(token);
    }

    @Transactional(readOnly = true)
    public Member getById(Long memberId) {
        return memberJpaRepository.findById(memberId).orElseThrow(() ->
                new NotFoundException("id에 맞는 멤버가 없습니다. memberId = " + memberId));
    }

    @Transactional
    public String signUp(SignUpRequest signUpRequest) {
        if (memberJpaRepository.existsByEmail(new Email(signUpRequest.email()))) {
            throw new DuplicationException("이미 존재하는 email 주소입니다.");
        }
        String encodedPassword = passwordEncoder.encode(signUpRequest.password());
        Member member = new Member(
                new Name(signUpRequest.name()),
                new Email(signUpRequest.email()),
                Role.USER,
                new Password(encodedPassword)
        );
        Member savedMember = memberJpaRepository.save(member);
        return parseToToken(savedMember);
    }
}
