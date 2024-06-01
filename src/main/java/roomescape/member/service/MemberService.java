package roomescape.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.SignUpRequest;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MembersResponse;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member addMember(final SignUpRequest request) {
        Optional<Member> memberHasEmail = memberRepository.findByEmail(new Email(request.email()));
        if (memberHasEmail.isPresent()) {
            throw new DataDuplicateException(ErrorType.EMAIL_DUPLICATED, ErrorType.EMAIL_DUPLICATED.getDescription());
        }
        return memberRepository.save(request.toMemberEntity());
    }

    public MembersResponse findAllMembers() {
        List<MemberResponse> response = memberRepository.findAll().stream()
                .map(MemberResponse::fromEntity)
                .toList();

        return new MembersResponse(response);
    }

    public Member findMemberByEmailAndPassword(final LoginRequest request) {
        Email email = new Email(request.email());
        Password password = new Password(request.password());

        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        ErrorType.MEMBER_NOT_FOUND.getDescription()));
    }
}
