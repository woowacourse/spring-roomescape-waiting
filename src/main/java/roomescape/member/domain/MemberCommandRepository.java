package roomescape.member.domain;

public interface MemberCommandRepository {
    Long save(Member member);

    void deleteById(Long id);
}
