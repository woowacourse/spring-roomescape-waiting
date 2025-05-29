package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.waiting.domain.Waiting;

@ActiveProfiles("test")
@DataJpaTest
@Sql(value = {"/schema.sql"})
public class WaitingJpaRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    WaitingRepository waitingRepository;

    @DisplayName("조건에 맞는 대기의 개수를 조회할 수 있다")
    @Test
    void countByDateAndTimeIdAndThemeId() {
        // given
        LocalDate now = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Theme theme = new Theme(null, "공포테마", "진짜 무서운거임", "덜덜");
        Member member1 = new Member(null, "유저1", "유저1이메일", "비밀번호1", MemberRole.USER);
        Member member2 = new Member(null, "유저2", "유저2이메일", "비밀번호2", MemberRole.USER);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(new Waiting(null, now, reservationTime, theme, member1, 1));
        entityManager.persist(new Waiting(null, now, reservationTime, theme, member2, 2));

        // when
        long result = waitingRepository.countByDateAndTimeIdAndThemeId(now, reservationTime.getId(), theme.getId());

        // then
        assertThat(result).isEqualTo(2);
    }

    @DisplayName("조건에 맞는 대기 존재 여부를 조회할 수 있다")
    @Test
    void existsByDateAndThemeIdAndTimeIdAndMemberId() {
        // given
        LocalDate now = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Theme theme = new Theme(null, "공포테마", "진짜 무서운거임", "덜덜");
        Member member = new Member(null, "유저1", "유저1이메일", "비밀번호1", MemberRole.USER);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(member);
        entityManager.persist(new Waiting(null, now, reservationTime, theme, member, 1));

        // when
        boolean result = waitingRepository.existsByDateAndThemeIdAndTimeIdAndMemberId(
                now,
                reservationTime.getId(),
                theme.getId(),
                member.getId()
        );

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("조건에 해당하는 첫번째 대기를 조회할 수 있다")
    @Test
    void popFirstWaiting() {
        // given
        LocalDate now = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Theme theme = new Theme(null, "공포테마", "진짜 무서운거임", "덜덜");
        Member firstMember = new Member(null, "유저1", "유저1이메일", "비밀번호1", MemberRole.USER);
        Member secondMember = new Member(null, "유저1", "유저1이메일", "비밀번호1", MemberRole.USER);
        entityManager.persist(reservationTime);
        entityManager.persist(theme);
        entityManager.persist(firstMember);
        entityManager.persist(secondMember);
        Waiting firstWaiting = new Waiting(null, now, reservationTime, theme, firstMember, 1);
        Waiting secondWaiting = new Waiting(null, now, reservationTime, theme, secondMember, 2);
        entityManager.persist(firstWaiting);
        entityManager.persist(secondWaiting);

        // when
        Optional<Waiting> result = waitingRepository.popFirstWaiting(theme, now, reservationTime);

        // then
        Waiting waiting = result.get();
        assertAll(
                () -> assertThat(waiting.getId()).isEqualTo(firstWaiting.getId()),
                () -> assertThat(waiting.getDate()).isEqualTo(firstWaiting.getDate()),
                () -> assertThat(waiting.getTheme()).isEqualTo(firstWaiting.getTheme()),
                () -> assertThat(waiting.getMember()).isEqualTo(firstWaiting.getMember()),
                () -> assertThat(waiting.getTime()).isEqualTo(firstWaiting.getTime()),
                () -> assertThat(waiting.getPriority()).isEqualTo(firstWaiting.getPriority())
        );
    }
}
