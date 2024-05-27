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

    public Member find(final MemberLoginRequest request) {
        final Member foundMember = memberRepository.findByEmailOrThrow(request.email());
        final String encryptedPassword = encryptor.encryptPassword(request.password());
        if (encryptedPassword.equals(foundMember.getPassword())) {
            return foundMember;
        }
        throw new InvalidRequestException("Invalid email or password");
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findByIdOrThrow(id);
    }
}
