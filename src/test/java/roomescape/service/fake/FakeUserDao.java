package roomescape.service.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.model.Member;
import roomescape.repository.UserDao;

public class FakeUserDao implements UserDao {

    private final List<Member> members = new ArrayList<>();
    private final AtomicLong atomicLong = new AtomicLong(1L);

    @Override
    public Optional<Member> findUserByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(user -> Objects.equals(user.getEmail(), email)
                        && Objects.equals(user.getPassword(), password))
                .findAny();
    }

    @Override
    public Optional<String> findUserNameByUserId(Long userId) {
        return members.stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .map(Member::getName)
                .findAny();
    }

    @Override
    public Optional<Member> findUserById(Long userId) {
        return members.stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findAny();
    }

    @Override
    public List<Member> findAllUsers() {
        return members;
    }

    public void addUser(Member member) {
        members.add(member);
    }

    public void clear() {
        atomicLong.set(1L);
        members.clear();
    }
}
