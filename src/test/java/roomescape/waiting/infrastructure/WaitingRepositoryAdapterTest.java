package roomescape.waiting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationSpecFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

@ActiveProfiles("test")
@DataJpaTest
@Import(WaitingRepositoryAdapter.class)
class WaitingRepositoryAdapterTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingRepositoryAdapter waitingRepositoryAdapter;

    @DisplayName("예약 대기를 저장한다")
    @Test
    void save() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting = new Waiting(member, spec);

        // when
        Waiting savedWaiting = waitingRepositoryAdapter.save(waiting);

        // then
        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMember()).isEqualTo(member);
        assertThat(savedWaiting.getSpec()).isEqualTo(spec);
    }

    @DisplayName("ID로 예약 대기를 삭제한다")
    @Test
    void deleteById() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting = new Waiting(member, spec);

        Waiting savedWaiting = waitingRepositoryAdapter.save(waiting);
        Long waitingId = savedWaiting.getId();

        // when
        waitingRepositoryAdapter.deleteById(waitingId);

        // then
        Optional<Waiting> foundWaiting = waitingRepositoryAdapter.findById(waitingId);
        assertThat(foundWaiting).isEmpty();
    }

    @DisplayName("모든 예약 대기를 조회한다")
    @Test
    void findAll() {
        // given
        Member member1 = MemberFixture.createMember("에드", "test1@test.com", "1234");
        entityManager.persist(member1);

        Member member2 = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        entityManager.persist(member2);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec1 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting1 = new Waiting(member1, spec1);
        waitingRepositoryAdapter.save(waiting1);

        ReservationSpec spec2 = ReservationSpecFixture.createSpec(LocalDate.now().plusDays(1), reservationTime, theme);
        Waiting waiting2 = new Waiting(member2, spec2);
        waitingRepositoryAdapter.save(waiting2);

        // when
        List<Waiting> waitings = waitingRepositoryAdapter.findAll();

        // then
        assertThat(waitings).hasSize(2);
    }

    @DisplayName("멤버 ID로 예약 대기와 순서를 조회한다")
    @Test
    void findWithRankByMemberId() {
        // given
        Member member1 = MemberFixture.createMember("에드", "test1@test.com", "1234");
        entityManager.persist(member1);

        Member member2 = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        entityManager.persist(member2);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec1 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting1 = new Waiting(member1, spec1);
        waitingRepositoryAdapter.save(waiting1);

        ReservationSpec spec2 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting2 = new Waiting(member2, spec2);
        waitingRepositoryAdapter.save(waiting2);

        // when
        List<WaitingWithRank> waitingsWithRank = waitingRepositoryAdapter.findWithRankByMemberId(member2.getId());

        // then
        assertThat(waitingsWithRank).hasSize(1);
        assertThat(waitingsWithRank.getFirst().getWaiting().getMember().getId()).isEqualTo(member2.getId());
        assertThat(waitingsWithRank.getFirst().getRank()).isEqualTo(2);
    }

    @DisplayName("ID로 예약 대기와 순위를 조회한다")
    @Test
    void findWithRankById() {
        // given
        Member member1 = MemberFixture.createMember("에드", "test1@test.com", "1234");
        entityManager.persist(member1);

        Member member2 = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        entityManager.persist(member2);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec1 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting1 = new Waiting(member1, spec1);
        waitingRepositoryAdapter.save(waiting1);

        ReservationSpec spec2 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting2 = new Waiting(member2, spec2);
        Waiting savedWaiting2 = waitingRepositoryAdapter.save(waiting2);

        // when
        Optional<WaitingWithRank> waitingWithRank = waitingRepositoryAdapter.findWithRankById(savedWaiting2.getId());

        // then
        assertThat(waitingWithRank).isPresent();
        assertThat(waitingWithRank.get().getWaiting().getId()).isEqualTo(savedWaiting2.getId());
        assertThat(waitingWithRank.get().getRank()).isEqualTo(2);
    }

    @DisplayName("ID로 예약 대기를 조회한다")
    @Test
    void findById() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        Waiting waiting = new Waiting(member, spec);
        Waiting savedWaiting = waitingRepositoryAdapter.save(waiting);

        // when
        Optional<Waiting> foundWaiting = waitingRepositoryAdapter.findById(savedWaiting.getId());

        // then
        assertThat(foundWaiting).isPresent();
        assertThat(foundWaiting.get().getId()).isEqualTo(savedWaiting.getId());
        assertThat(foundWaiting.get().getMember()).isEqualTo(member);
        assertThat(foundWaiting.get().getSpec()).isEqualTo(spec);
    }
}
