package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.exception.EmailException;
import roomescape.member.presentation.dto.MemberRequest;
import roomescape.member.presentation.dto.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse save(final MemberRequest request) {
        boolean emailExist = memberRepository.isExistsByEmail(request.email());
        validateEmailExists(emailExist);

        Long id = memberRepository.save(Member.createWithoutId(request.name(), request.email(), request.password()));

        return new MemberResponse(id, request.name());
    }

    private static void validateEmailExists(boolean emailExist) {
        if (emailExist) {
            throw new EmailException("중복되는 이메일입니다.");
        }
    }

    public Member findByEmail(final String email) {
        return memberRepository.findByEmail(email);
    }

    public boolean isExistsByEmail(final String email) {
        return memberRepository.isExistsByEmail(email);
    }

    public Member findById(final Long id) {
        return memberRepository.findById(id);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }
}
