package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.infrastructure.JpaMemberRepository;
import roomescape.infrastructure.JpaReservationTimeRepository;
import roomescape.infrastructure.JpaThemeRepository;
import roomescape.infrastructure.JpaWaitingRepository;
import roomescape.infrastructure.WaitingRepositoryAdaptor;

@DataJpaTest
@Sql(
        value = {
                "/sql/testMember.sql",
                "/sql/testReservationTime.sql",
                "/sql/testTheme.sql",
        }
)
class WaitingRepositoryAdaptorTest {

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    private WaitingRepositoryAdaptor waitingRepositoryAdaptor;

    private Member member1;
    private Member member2;
    private Theme theme1;
    private ReservationTime reservationTime1;

    private Waiting waiting1;
    private Waiting waiting2;

    @BeforeEach
    void setUp() {
        waitingRepositoryAdaptor = new WaitingRepositoryAdaptor(jpaWaitingRepository);

        List<Member> members = jpaMemberRepository.findAll();
        List<ReservationTime> reservationTimes = jpaReservationTimeRepository.findAll();
        List<Theme> themes = jpaThemeRepository.findAll();

        member1 = members.get(0);
        member2 = members.get(1);
        theme1 = themes.getFirst();
        reservationTime1 = reservationTimes.getFirst();

        waiting1 = Waiting.createWithoutId(member1, LocalDate.now().plusDays(1), reservationTime1, theme1);
        waiting2 = Waiting.createWithoutId(member2, LocalDate.now().plusDays(1), reservationTime1, theme1);

        waitingRepositoryAdaptor.save(waiting1);
        waitingRepositoryAdaptor.save(waiting2);
    }

    @Test
    void 대기_저장_테스트() {
        //when & then
        Waiting savedWaiting = waitingRepositoryAdaptor.save(waiting1);

        assertThat(savedWaiting).isNotNull();
        assertThat(savedWaiting.getId()).isEqualTo(waiting1.getId());
        assertThat(savedWaiting.getMember()).isEqualTo(waiting1.getMember());
    }

    @Test
    void 모든_대기_조회_테스트() {
        // when & then
        List<Waiting> allWaitings = waitingRepositoryAdaptor.findAll();

        assertThat(allWaitings).hasSize(2);
    }

    @Test
    void Id로_대기_조회_테스트() {
        //given
        waitingRepositoryAdaptor.save(waiting1);

        // when & then
        Optional<Waiting> foundWaiting = waitingRepositoryAdaptor.findById(waiting1.getId());

        assertThat(foundWaiting).isNotEmpty();
        assertThat(foundWaiting.get().getId()).isEqualTo(waiting1.getId());
    }

    @Test
    void 테마Id로_대기_조회_테스트() {
        // when & then
        List<Waiting> waitingsByTheme = waitingRepositoryAdaptor.findByThemeId(theme1.getId());

        assertThat(waitingsByTheme).hasSize(2);
    }

    @Test
    void 멤버Id로_대기_조회_테스트() {
        //when & then
        List<Waiting> waitingsByMember = waitingRepositoryAdaptor.findByMemberId(member1.getId());

        assertThat(waitingsByMember).hasSize(1);

    }

    @Test
    void 예약시간Id로_대기_조회_테스트() {
        List<Waiting> waitingsByTime = waitingRepositoryAdaptor.findByReservationTimeId(reservationTime1.getId());

        assertThat(waitingsByTime).hasSize(2);
    }

    @Test
    void 날짜_예약시간_테마_멤버로_대기_조회() {
        Optional<Waiting> foundWaiting = waitingRepositoryAdaptor.findByDateAndReservationTimeAndThemeAndMember(
                waiting1.getDate(), waiting1.getReservationTime(), waiting1.getTheme(), waiting1.getMember());

        assertThat(foundWaiting).isNotEmpty();
        assertThat(foundWaiting.get().getId()).isEqualTo(waiting1.getId());
    }

    @Test
    void 멤버Id로_정렬된_대기_조회_테스트() {
        List<WaitingWithRank> waitingsWithRank = waitingRepositoryAdaptor.findByMemberIdSortedByCreateAt(
                member1.getId());

        assertThat(waitingsWithRank).isNotEmpty();
    }

    @Test
    void 날짜_예약시간_테마_정렬된_대기_조회_테스트() {
        List<WaitingWithRank> waitingsWithRank = waitingRepositoryAdaptor.findByDateAndReservationTimeAndThemeSortedByCreateAt(
                waiting1.getDate(), reservationTime1.getId(), theme1.getId());

        assertThat(waitingsWithRank).isNotEmpty();
    }

    @Test
    void Id로_대기_삭제_테스트() {
        waitingRepositoryAdaptor.deleteById(waiting1.getId());
        Optional<Waiting> deletedWaiting = waitingRepositoryAdaptor.findById(waiting1.getId());

        assertThat(deletedWaiting).isEmpty();
    }
}