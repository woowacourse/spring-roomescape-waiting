package roomescape.unit.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
import roomescape.reservation.infrastructure.WaitingRepositoryImpl;

@DataJpaTest
class WaitingRepositoryImplTest {

    @Autowired
    private WaitingRepositoryImpl waitingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 회원id로_대기를_대기번호와_함께_조회한다() {
        // given
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        Member member2 = Member.builder()
                .name("name2")
                .email("email2@domain.com")
                .password("password2")
                .role(Role.MEMBER)
                .build();
        Member member3 = Member.builder()
                .name("name3")
                .email("email3@domain.com")
                .password("password3")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Reservation reservation = Reservation.builder()
                .member(member1)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Waiting waiting1 = Waiting.builder()
                .member(member2)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Waiting waiting2 = Waiting.builder()
                .member(member3)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(theme);
        entityManager.persist(time);
        entityManager.persist(reservation);
        entityManager.persist(waiting1);
        entityManager.persist(waiting2);
        // when
        List<WaitingWithRankResponse> waitings = waitingRepository.findByMemberIdWithRank(member3.getId());
        // then
        assertThat(waitings).hasSize(1);
        assertThat(waitings.get(0).getMemberName()).isEqualTo(member3.getName());
        assertThat(waitings.get(0).getRank()).isEqualTo(2L);
    }
}