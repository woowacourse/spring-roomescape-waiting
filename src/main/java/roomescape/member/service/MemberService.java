package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.dto.response.MemberCreateResponse;
import roomescape.member.dto.response.MemberReadResponse;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {
        validateDuplicateEmail(request.email());
        Member member = request.toEntity();
        Member saved = memberRepository.save(member);
        return MemberCreateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MemberReadResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberReadResponse::from)
                .toList();
    }

    @Transactional
    public void deleteMember(long id) {
        memberRepository.deleteById(id);
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ConflictException("중복된 이메일입니다.");
        }
    }
}
