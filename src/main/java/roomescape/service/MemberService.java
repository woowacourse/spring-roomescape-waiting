package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.MemberResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public ListResponse<MemberResponse> findAll() {
        List<MemberResponse> members = memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();

        return new ListResponse<>(members);
    }
}
