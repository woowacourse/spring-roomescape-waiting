package roomescape.member.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.ConflictException;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.repository.ThemeRepository;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Member, Password> members = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByEmail(MemberEmail email) {
        return members.keySet().stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public Member save(Account account) {
        if (existsByEmail(account.getMember().getEmail())) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }

        Member saved = Member.withId(
                MemberId.from(index.getAndIncrement()),
                account.getMember().getName(),
                account.getMember().getEmail(),
                account.getMember().getRole());

        members.put(saved, account.getPassword());

        return saved;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        return members.keySet().stream()
                .filter(member -> Objects.equals(member.getId(), id))
                .findFirst();
    }

    @Override
    public Optional<Account> findAccountByEmail(MemberEmail email) {
        return members.entrySet().stream()
                .filter(entry -> entry.getKey().getEmail().equals(email))
                .findFirst()
                .map(entry -> Account.of(entry.getKey(), entry.getValue()));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members.keySet());
    }
}
