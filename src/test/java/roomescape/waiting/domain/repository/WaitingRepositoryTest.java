package roomescape.waiting.domain.repository;

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
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.infrastructure.WaitingRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import(WaitingRepositoryAdapter.class)
class WaitingRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("예약 대기를 저장한다")
    @Test
    void save() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        // 대기 객체 생성
        Waiting waiting = new Waiting(member, spec);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getMember()).isEqualTo(member);
        assertThat(savedWaiting.getSpec()).isEqualTo(spec);
    }

    @DisplayName("ID로 예약 대기를 삭제한다")
    @Test
    void deleteById() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        // 대기 객체 생성
        Waiting waiting = new Waiting(member, spec);

        // 대기 저장 및 저장된 객체 참조
        Waiting savedWaiting = waitingRepository.save(waiting);
        // 저장된 대기의 ID 추출
        Long waitingId = savedWaiting.getId();

        // when
        waitingRepository.deleteById(waitingId);

        // then
        Optional<Waiting> foundWaiting = waitingRepository.findById(waitingId);
        assertThat(foundWaiting).isEmpty();
    }

    @DisplayName("모든 예약 대기를 조회한다")
    @Test
    void findAll() {
        // given
        // 첫 번째 회원 생성 및 저장
        Member member1 = MemberFixture.createMember("에드", "test1@test.com", "1234");
        entityManager.persist(member1);

        // 두 번째 회원 생성 및 저장
        Member member2 = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        entityManager.persist(member2);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        // 첫 번째 예약 스펙 생성 (오늘 날짜)
        ReservationSpec spec1 = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        // 첫 번째 대기 객체 생성 및 저장
        Waiting waiting1 = new Waiting(member1, spec1);
        waitingRepository.save(waiting1);

        // 두 번째 예약 스펙 생성 (내일 날짜)
        ReservationSpec spec2 = ReservationSpecFixture.createSpec(LocalDate.now().plusDays(1), reservationTime, theme);
        // 두 번째 대기 객체 생성 및 저장
        Waiting waiting2 = new Waiting(member2, spec2);
        waitingRepository.save(waiting2);

        // when
        List<Waiting> waitings = waitingRepository.findAll();

        // then
        assertThat(waitings).hasSize(2);
    }

    @DisplayName("ID로 예약 대기를 조회한다")
    @Test
    void findById() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), reservationTime, theme);
        // 대기 객체 생성
        Waiting waiting = new Waiting(member, spec);
        // 대기 저장 및 저장된 객체 참조
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        Optional<Waiting> foundWaiting = waitingRepository.findById(savedWaiting.getId());

        // then
        assertThat(foundWaiting).isPresent();
        assertThat(foundWaiting.get().getId()).isEqualTo(savedWaiting.getId());
        assertThat(foundWaiting.get().getMember()).isEqualTo(member);
        assertThat(foundWaiting.get().getSpec()).isEqualTo(spec);
    }
}
