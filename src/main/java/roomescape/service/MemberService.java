package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.controller.member.dto.MemberLoginRequest;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.infrastructure.Encryptor;
import roomescape.repository.MemberRepository;
import roomescape.service.exception.DuplicateEmailException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final Encryptor encryptor;

    public MemberService(final MemberRepository memberRepository,
                         final Encryptor encryptor) {
        this.memberRepository = memberRepository;
        this.encryptor = encryptor;
    }

    public Member save(final SignupRequest request) {
        final Optional<Member> findMember = memberRepository.findByEmail(request.email());
        if (findMember.isPresent()) {
            throw new DuplicateEmailException("해당 email로 사용자가 존재합니다.");
        }
        final String encryptedPassword = encryptor.encryptPassword(request.password());
        final Member member = new Member(null, request.name(),
                request.email(), encryptedPassword, Role.USER);
        return memberRepository.save(member);
    }

    public boolean invalidPassword(final String email, final String rawPassword) {
        final Member findMember = memberRepository.findByEmailOrThrow(email);
        final String encryptedPassword = encryptor.encryptPassword(rawPassword);
        return !encryptedPassword.equals(findMember.getPassword());
    }

    public Member find(final MemberLoginRequest request) {
        if (invalidPassword(request.email(), request.password())) {
            throw new InvalidRequestException("Invalid email or password");
        }
        return memberRepository.findByEmailOrThrow(request.email());
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findByEmailOrThrow(id);
    }
}
