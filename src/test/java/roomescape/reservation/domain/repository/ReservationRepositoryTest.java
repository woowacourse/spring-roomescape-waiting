package roomescape.reservation.domain.repository;

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
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
@Import(ReservationRepositoryAdapter.class)
class ReservationRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약을 저장한다")
    @Test
    void save() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성
        Reservation reservation = new Reservation(member, spec);

        // when
        Reservation savedReservation = reservationRepository.save(reservation);

        // then
        assertThat(savedReservation.getId()).isNotNull();
        assertThat(savedReservation.getMember().getId()).isEqualTo(member.getId());
        assertThat(savedReservation.getSpec().getDate().getValue()).isEqualTo(date);
        assertThat(savedReservation.getSpec().getTime().getId()).isEqualTo(time.getId());
        assertThat(savedReservation.getSpec().getTheme().getId()).isEqualTo(theme.getId());
    }

    @DisplayName("ID로 예약을 조회한다")
    @Test
    void findById() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성
        Reservation reservation = new Reservation(member, spec);
        // 예약 저장 및 저장된 객체 참조
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        Optional<Reservation> foundReservation = reservationRepository.findById(savedReservation.getId());

        // then
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getId()).isEqualTo(savedReservation.getId());
        assertThat(foundReservation.get().getMember().getId()).isEqualTo(member.getId());
        assertThat(foundReservation.get().getSpec().getDate().getValue()).isEqualTo(date);
        assertThat(foundReservation.get().getSpec().getTime().getId()).isEqualTo(time.getId());
        assertThat(foundReservation.get().getSpec().getTheme().getId()).isEqualTo(theme.getId());
    }

    @DisplayName("회원 ID로 예약을 조회한다")
    @Test
    void findAllByMemberId() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장 (첫 번째 예약용)
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time1);

        // 오후 12시 예약 시간 생성 및 저장 (두 번째 예약용)
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        entityManager.persist(time2);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 첫 번째 예약 날짜 설정
        LocalDate date1 = LocalDate.now().plusDays(1);
        // 첫 번째 예약 스펙 생성 (날짜1, 시간1, 테마)
        ReservationSpec spec1 = new ReservationSpec(new ReservationDate(date1), time1, theme);
        // 회원으로 첫 번째 예약 생성 및 저장
        Reservation reservation1 = new Reservation(member, spec1);
        reservationRepository.save(reservation1);

        // 모레 날짜로 두 번째 예약 날짜 설정
        LocalDate date2 = LocalDate.now().plusDays(2);
        // 두 번째 예약 스펙 생성 (날짜2, 시간2, 테마)
        ReservationSpec spec2 = new ReservationSpec(new ReservationDate(date2), time2, theme);
        // 동일 회원으로 두 번째 예약 생성 및 저장
        Reservation reservation2 = new Reservation(member, spec2);
        reservationRepository.save(reservation2);

        // when
        List<Reservation> reservations = reservationRepository.findAllByMemberId(member.getId());

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).extracting(Reservation::getMember).extracting(Member::getId)
                .containsOnly(member.getId());
    }

    @DisplayName("모든 예약을 조회한다")
    @Test
    void findAll() {
        // given
        // 첫 번째 회원 생성 및 저장
        Member member1 = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member1);

        // 두 번째 회원 생성 및 저장
        Member member2 = MemberFixture.createMember("김진우", "jinu@example.com", "password456");
        entityManager.persist(member2);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 첫 번째 예약 날짜 설정
        LocalDate date1 = LocalDate.now().plusDays(1);
        // 첫 번째 예약 스펙 생성 (날짜1, 시간, 테마)
        ReservationSpec spec1 = new ReservationSpec(new ReservationDate(date1), time, theme);
        // 첫 번째 회원으로 첫 번째 예약 생성 및 저장
        Reservation reservation1 = new Reservation(member1, spec1);
        reservationRepository.save(reservation1);

        // 모레 날짜로 두 번째 예약 날짜 설정
        LocalDate date2 = LocalDate.now().plusDays(2);
        // 두 번째 예약 스펙 생성 (날짜2, 시간, 테마)
        ReservationSpec spec2 = new ReservationSpec(new ReservationDate(date2), time, theme);
        // 두 번째 회원으로 두 번째 예약 생성 및 저장
        Reservation reservation2 = new Reservation(member2, spec2);
        reservationRepository.save(reservation2);

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("필터링된 예약을 조회한다")
    @Test
    void findFiltered() {
        // given
        // 첫 번째 회원 생성 및 저장
        Member member1 = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member1);

        // 두 번째 회원 생성 및 저장
        Member member2 = MemberFixture.createMember("김진우", "jinu@example.com", "password456");
        entityManager.persist(member2);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 첫 번째 테마 생성 및 저장
        Theme theme1 = new Theme("테마1", "설명1", "썸네일1");
        entityManager.persist(theme1);

        // 두 번째 테마 생성 및 저장
        Theme theme2 = new Theme("테마2", "설명2", "썸네일2");
        entityManager.persist(theme2);

        // 내일 날짜로 첫 번째 예약 날짜 설정
        LocalDate date1 = LocalDate.now().plusDays(1);
        // 첫 번째 예약 스펙 생성 (날짜1, 시간, 테마1)
        ReservationSpec spec1 = new ReservationSpec(new ReservationDate(date1), time, theme1);
        // 첫 번째 회원으로 첫 번째 예약 생성 및 저장
        Reservation reservation1 = new Reservation(member1, spec1);
        reservationRepository.save(reservation1);

        // 모레 날짜로 두 번째 예약 날짜 설정
        LocalDate date2 = LocalDate.now().plusDays(2);
        // 두 번째 예약 스펙 생성 (날짜2, 시간, 테마2)
        ReservationSpec spec2 = new ReservationSpec(new ReservationDate(date2), time, theme2);
        // 두 번째 회원으로 두 번째 예약 생성 및 저장
        Reservation reservation2 = new Reservation(member2, spec2);
        reservationRepository.save(reservation2);

        // when
        List<Reservation> filteredByMember = reservationRepository.findFiltered(member1.getId(), null, null, null);
        List<Reservation> filteredByTheme = reservationRepository.findFiltered(null, theme2.getId(), null, null);
        List<Reservation> filteredByDate = reservationRepository.findFiltered(null, null, date1, date1);
        List<Reservation> filteredByAll = reservationRepository.findFiltered(member2.getId(), theme2.getId(), date2,
                date2);

        // then
        assertThat(filteredByMember).hasSize(1);
        assertThat(filteredByMember.getFirst().getMember().getId()).isEqualTo(member1.getId());

        assertThat(filteredByTheme).hasSize(1);
        assertThat(filteredByTheme.getFirst().getSpec().getTheme().getId()).isEqualTo(theme2.getId());

        assertThat(filteredByDate).hasSize(1);
        assertThat(filteredByDate.getFirst().getSpec().getDate().getValue()).isEqualTo(date1);

        assertThat(filteredByAll).hasSize(1);
        assertThat(filteredByAll.getFirst().getMember().getId()).isEqualTo(member2.getId());
        assertThat(filteredByAll.getFirst().getSpec().getTheme().getId()).isEqualTo(theme2.getId());
        assertThat(filteredByAll.getFirst().getSpec().getDate().getValue()).isEqualTo(date2);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void deleteById() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성
        Reservation reservation = new Reservation(member, spec);
        // 예약 저장 및 저장된 객체 참조
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        reservationRepository.deleteById(savedReservation.getId());

        // then
        Optional<Reservation> deletedReservation = reservationRepository.findById(savedReservation.getId());
        assertThat(deletedReservation).isEmpty();
    }

    @DisplayName("예약 스펙으로 예약 존재 여부를 확인한다")
    @Test
    void existsBySpec() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsBySpec(spec);
        ReservationSpec nonExistingSpec = new ReservationSpec(new ReservationDate(date.plusDays(1)), time, theme);
        boolean notExists = reservationRepository.existsBySpec(nonExistingSpec);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @DisplayName("날짜와 테마로 시간 ID를 조회한다")
    @Test
    void findTimeIdsByDateAndTheme() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장 (첫 번째 예약용)
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time1);

        // 오후 12시 예약 시간 생성 및 저장 (두 번째 예약용)
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        entityManager.persist(time2);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 첫 번째 예약 스펙 생성 (날짜, 시간1, 테마)
        ReservationSpec spec1 = new ReservationSpec(new ReservationDate(date), time1, theme);
        // 회원으로 첫 번째 예약 생성 및 저장
        Reservation reservation1 = new Reservation(member, spec1);
        reservationRepository.save(reservation1);

        // 두 번째 예약 스펙 생성 (동일 날짜, 시간2, 동일 테마)
        ReservationSpec spec2 = new ReservationSpec(new ReservationDate(date), time2, theme);
        // 동일 회원으로 두 번째 예약 생성 및 저장
        Reservation reservation2 = new Reservation(member, spec2);
        reservationRepository.save(reservation2);

        // when
        List<Long> timeIds = reservationRepository.findTimeIdsByDateAndTheme(date, theme.getId());

        // then
        assertThat(timeIds).hasSize(2);
        assertThat(timeIds).containsExactlyInAnyOrder(time1.getId(), time2.getId());
    }

    @DisplayName("시간 ID로 예약 존재 여부를 확인한다")
    @Test
    void existsByTimeId() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByTimeId(time.getId());
        boolean notExists = reservationRepository.existsByTimeId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @DisplayName("테마 ID로 예약 존재 여부를 확인한다")
    @Test
    void existsByThemeId() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        entityManager.persist(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        entityManager.persist(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByThemeId(theme.getId());
        boolean notExists = reservationRepository.existsByThemeId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
