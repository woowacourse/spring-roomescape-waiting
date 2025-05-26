package roomescape.waiting.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.jpa.JpaMemberRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.jpa.JpaReservationTimeRepository;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.jpa.JpaScheduleRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.jpa.JpaThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.repository.jpa.JpaWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@DataJpaTest
public class JpaWaitingRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaScheduleRepository jpaScheduleRepository;

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        member = jpaMemberRepository.save(new Member(null, "test", "test@test.com", MemberRole.USER, "testpassword"));
        theme = jpaThemeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        schedule = jpaScheduleRepository.save(new Schedule(null, LocalDate.now().plusDays(1), time, theme));
    }

    @Test
    void 예약_대기_저장() {
        Waiting waiting = new Waiting(null, schedule, member);
        Waiting savedWaiting = jpaWaitingRepository.save(waiting);
        assertThat(jpaWaitingRepository.findById(waiting.getId()).orElse(null)).isEqualTo(savedWaiting);
    }

    @Test
    void 예약_대기_정보와_순번을_멤버_ID로_찾기() {
        Member member2 = jpaMemberRepository.save(new Member(null, "test2", "test2@test.com", MemberRole.USER, "testpassword"));
        Waiting waiting1 = new Waiting(null, schedule, member);
        Waiting savedWaiting1 = jpaWaitingRepository.save(waiting1);

        Waiting waiting2 = new Waiting(null, schedule, member2);
        Waiting savedWaiting2 = jpaWaitingRepository.save(waiting2);

        List<WaitingWithRank> waitingWithRanks1 = jpaWaitingRepository.findWaitingWithRankByMemberId(member.getId());
        WaitingWithRank member1Waiting = waitingWithRanks1.getFirst();

        List<WaitingWithRank> waitingWithRanks2 = jpaWaitingRepository.findWaitingWithRankByMemberId(member2.getId());
        WaitingWithRank member2Waiting = waitingWithRanks2.getFirst();

        assertAll(
                () -> assertThat(member1Waiting.getWaiting()).isEqualTo(savedWaiting1),
                () -> assertThat(member1Waiting.getRank()).isEqualTo(0L),
                () -> assertThat(member2Waiting.getWaiting()).isEqualTo(savedWaiting2),
                () -> assertThat(member2Waiting.getRank()).isEqualTo(1L)
        );
    }

    @Test
    void 동일한_스케줄의_대기열에서_해당_멤버_대기_존재여부_확인_True() {
        Waiting waiting = new Waiting(null, schedule, member);
        jpaWaitingRepository.save(waiting);
        assertThat(jpaWaitingRepository.existsByMemberAndSchedule(member, schedule)).isTrue();
    }

    @Test
    void 동일한_스케줄의_대기열에서_해당_멤버_대기_존재여부_확인_False() {
        Waiting waiting = new Waiting(null, schedule, member);
        Waiting savedWaiting = jpaWaitingRepository.save(waiting);
        jpaWaitingRepository.deleteById(savedWaiting.getId());
        assertThat(jpaWaitingRepository.existsByMemberAndSchedule(member, schedule)).isFalse();
    }

    @Test
    void 스케줄에_대기_존재여부_확인_True() {
        Waiting waiting = new Waiting(null, schedule, member);
        jpaWaitingRepository.save(waiting);
        assertThat(jpaWaitingRepository.existsBySchedule(schedule)).isTrue();
    }

    @Test
    void 스케줄에_대기_존재여부_확인_False() {
        Waiting waiting = new Waiting(null, schedule, member);
        Waiting savedWaiting = jpaWaitingRepository.save(waiting);
        jpaWaitingRepository.deleteById(savedWaiting.getId());
        assertThat(jpaWaitingRepository.existsBySchedule(schedule)).isFalse();
    }


    @Test
    void 대기열의_첫번째_대기자_찾기() {
        Member member2 = jpaMemberRepository.save(new Member(null, "test2", "test2@test.com", MemberRole.USER, "testpassword"));
        Waiting waiting1 = new Waiting(null, schedule, member);
        Waiting savedWaiting1 = jpaWaitingRepository.save(waiting1);

        Waiting waiting2 = new Waiting(null, schedule, member2);
        jpaWaitingRepository.save(waiting2);

        Waiting firstWaiting = jpaWaitingRepository.findFirstWaiting(schedule.getTheme().getId(), schedule.getDate(), schedule.getTime().getStartAt()).orElse(null);

        assertThat(firstWaiting).isEqualTo(savedWaiting1);
    }
}
