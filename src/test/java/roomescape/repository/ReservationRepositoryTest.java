package roomescape.repository;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.theme.ThumbnailUrl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


@DataJpaTest
class ReservationRepositoryTest {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private final static LocalDate TODAY = LocalDate.of(2026, 5, 10);

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime givenTime(int hour) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.of(hour, 0)));
    }

    private Theme givenTheme(String name) {
        return themeRepository.save(Theme.create(new ThemeName(name), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com")));
    }

    private Slot givenSlot(ReservationDate date, ReservationTime time, Theme theme) {
        return slotRepository.save(Slot.create(date, time, theme, LocalDateTime.now(FIXED_CLOCK)));
    }

    private Member givenMember(String name) {
        return memberRepository.save(Member.create(name));
    }

    @Test
    @DisplayName("ID 부여하며 저장")
    void save() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation persisted = Reservation.create(member, slot).withStatus(Status.APPROVED);

        Reservation saved = reservationRepository.save(persisted);

        assertSoftly(softly -> {
            softly.assertThat(saved.getId()).isNotNull();
            softly.assertThat(saved.getMember().getName()).isEqualTo("유저");
        });
    }

    @Test
    @DisplayName("같은 회원 저장 시 유니크 처리")
    void save_throwsException_whenSameSlotAndMember() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation persisted = Reservation.create(member, slot).withStatus(Status.APPROVED);

        reservationRepository.save(persisted);

        assertThatThrownBy(() -> {
            Reservation conflict = Reservation.create(member, slot).withStatus(Status.WAITING);
            reservationRepository.saveAndFlush(conflict);
        }).isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("전체 조회")
    void findAll() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member1 = givenMember("유저1");
        Member member2 = givenMember("유저2");
        Reservation given1 = Reservation.create(member1, slot).withStatus(Status.APPROVED);
        Reservation given2 = Reservation.create(member2, slot).withStatus(Status.WAITING);
        reservationRepository.save(given1);
        reservationRepository.save(given2);

        List<Reservation> all = reservationRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("저장 후 ID 조회")
    void findById() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.APPROVED);

        Reservation saved = reservationRepository.save(given);
        Optional<Reservation> found = reservationRepository.findById(saved.getId());

        assertSoftly(softly -> {
            softly.assertThat(found).isPresent();
            softly.assertThat(found.get().getId()).isEqualTo(saved.getId());
            softly.assertThat(found.get().getMember().getId()).isEqualTo(saved.getMember().getId());
            softly.assertThat(found.get().getStatus()).isEqualTo(saved.getStatus());
        });
    }

    @Test
    @DisplayName("삭제 후 조회 시 빈 Optional")
    void deleteById() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.APPROVED);
        Reservation saved = reservationRepository.save(given);

        reservationRepository.deleteById(saved.getId());

        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("slot_id의 모든 예약 조회")
    void findBySlot_Id() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member1 = givenMember("유저1");
        Member member2 = givenMember("유저2");
        Reservation given1 = Reservation.create(member1, slot).withStatus(Status.APPROVED);
        Reservation given2 = Reservation.create(member2, slot).withStatus(Status.WAITING);
        reservationRepository.save(given1);
        reservationRepository.save(given2);

        List<Reservation> bySlotId = reservationRepository.findBySlot_Id(slot.getId());

        assertThat(bySlotId).hasSize(2);
    }

    @Test
    @DisplayName("같은 slot_id와 member의 예약 존재 확인")
    void existsBySlotIdAndMember() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.APPROVED);
        Reservation saved = reservationRepository.save(given);

        List<Reservation> reservations = reservationRepository.findBySlot_Id(slot.getId());
        boolean exists = reservations.stream().anyMatch(r -> r.isSameMember(saved));

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("한 슬롯의 APPROVE 존재 확인")
    void existsApprovedBySlotId() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.APPROVED);
        reservationRepository.save(given);

        List<Reservation> reservations = reservationRepository.findBySlot_Id(slot.getId());
        boolean hasApproved = reservations.stream().anyMatch(Reservation::isApproved);

        assertThat(hasApproved).isTrue();
    }

    @Test
    @DisplayName("id에 따른 status 더티 체킹 후 조회")
    void changeStatus() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.WAITING);
        Reservation saved = reservationRepository.save(given);

        reservationRepository.getById(saved.getId()).changeStatus(Status.APPROVED);

        Optional<Reservation> found = reservationRepository.findById(saved.getId());

        assertSoftly(softly -> {
            softly.assertThat(found).isPresent();
            softly.assertThat(found.get().getStatus()).isEqualTo(Status.APPROVED);
        });
    }

    @Test
    @DisplayName("회원으로 예약 조회")
    void findByMember() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member1 = givenMember("유저1");
        Member member2 = givenMember("유저2");
        reservationRepository.save(Reservation.create(member1, slot).withStatus(Status.APPROVED));
        reservationRepository.save(Reservation.create(member2, slot).withStatus(Status.WAITING));

        List<Reservation> byMember = reservationRepository.findAllByMember(member1);

        assertThat(byMember).hasSize(1);
        assertThat(byMember.get(0).getMember().getName()).isEqualTo("유저1");
    }

    @Test
    @DisplayName("존재하는 ID의 existsById는 true")
    void existsById_true() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");
        Slot slot = givenSlot(new ReservationDate(TODAY), time, theme);
        Member member = givenMember("유저");
        Reservation given = Reservation.create(member, slot).withStatus(Status.APPROVED);
        Reservation saved = reservationRepository.save(given);

        assertThat(reservationRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 ID의 existsById는 false")
    void existsById_false() {
        assertThat(reservationRepository.existsById(Long.MAX_VALUE)).isFalse();
    }
}
