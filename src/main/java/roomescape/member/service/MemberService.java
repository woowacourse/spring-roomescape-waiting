package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MembersResponse;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.NotFoundException;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MembersResponse findAllMembers() {
        final List<MemberResponse> response = memberRepository.findAll().stream()
                .map(MemberResponse::fromEntity)
                .toList();

        return new MembersResponse(response);
    }

    public Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        String.format("회원(Member) 정보가 존재하지 않습니다. [memberId: %d]", memberId)));
    }

    public Member findMemberByEmailAndPassword(final String email, final String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        String.format("회원(Member) 정보가 존재하지 않습니다. [email: %s, password: %s]", email, password)));
    }
}
