package roomescape;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.assertThat;


/***
 * 양방향 FK 수정 위치 테스트
 *
 * 1단계 이후 삭제할 예정
 */
@DataJpaTest
public class SlotReservationSyncTest {
    private final static Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private final static LocalDate TODAY = LocalDate.of(2026, 5, 10);

    @Autowired private SlotRepository slotRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationTimeRepository timeRepository;
    @Autowired private ThemeRepository themeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager em;

    private ReservationTime givenTime(int hour) {
        return timeRepository.save(ReservationTime.create(LocalTime.of(hour, 0)));
    }

    private Theme givenTheme(String name) {
        return themeRepository.save(Theme.create(new ThemeName(name), "테스트 테마 입니다.", new ThumbnailUrl("https://test.com")));
    }

    private Slot givenSlot(ReservationDate date, ReservationTime time, Theme theme) {
        return slotRepository.save(Slot.create(date, time, theme, LocalDateTime.now(FIXED_CLOCK)));
    }

    @Test
    void 거울만_수정하면_FK가_반영되지_않는다() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");

        Slot slot = slotRepository.save(Slot.create(new ReservationDate(TODAY), time, theme, LocalDateTime.now(FIXED_CLOCK)));
        Member member = memberRepository.save(Member.create("김철수"));
        Reservation reservation = Reservation.create(member, slot);

        slot.getReservations().add(reservation);

        em.flush();   // DB에 강제로 반영 시도
        em.clear();   // 영속성 컨텍스트 비우고 DB에서 다시 읽게

        List<Reservation> found = reservationRepository.findAll();
        assertThat(found).isEmpty();
    }

    @Test
    void 주인을_수정해야_FK가_반영된다() {
        ReservationTime time = givenTime(14);
        Theme theme = givenTheme("테스트 테마");

        Slot slot = slotRepository.save(Slot.create(new ReservationDate(TODAY), time, theme, LocalDateTime.now(FIXED_CLOCK)));
        Member member = memberRepository.save(Member.create("김철수"));
        Reservation reservation = Reservation.create(member, slot);

        reservationRepository.save(reservation);

        em.flush();
        em.clear();

        List<Reservation> found = reservationRepository.findAll();
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getSlotId()).isEqualTo(slot.getId());
    }
}
