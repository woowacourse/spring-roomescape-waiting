package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Members;
import roomescape.dto.MemberResponse;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public List<MemberResponse> findAll() {
        return new Members(memberRepository.findAll()).getMembers().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
