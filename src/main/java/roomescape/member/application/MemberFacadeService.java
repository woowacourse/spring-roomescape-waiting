package roomescape.member.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.application.service.MemberCommandService;
import roomescape.member.application.service.MemberQueryService;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.exception.EmailException;
import roomescape.member.presentation.dto.MemberRequest;
import roomescape.member.presentation.dto.MemberResponse;

@Service
public class MemberFacadeService {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    public MemberFacadeService(final MemberQueryService memberQueryService, final MemberCommandService memberCommandService) {
        this.memberQueryService = memberQueryService;
        this.memberCommandService = memberCommandService;
    }

    public MemberResponse save(final MemberRequest request) {
        boolean emailExist = memberQueryService.isExistsByEmail(new Email(request.email()));
        validateEmailExists(emailExist);

        Member member = memberCommandService.save(request);
        return new MemberResponse(member.getId(), member.getName());
    }

    private void validateEmailExists(boolean emailExist) {
        if (emailExist) {
            throw new EmailException("중복되는 이메일입니다.");
        }
    }

    public List<MemberResponse> findAll() {
        return memberQueryService.findAll().stream()
            .map(member -> new MemberResponse(member.getId(), member.getName()))
            .toList();
    }
}
