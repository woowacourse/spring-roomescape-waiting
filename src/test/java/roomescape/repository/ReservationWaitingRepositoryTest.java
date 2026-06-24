package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

@DataJpaTest
class ReservationWaitingRepositoryTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약_대기를_생성한다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(
                member, LocalDateTime.now(),
                new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme)
        );

        ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        assertThat(saved)
                .extracting(ReservationWaiting::getName, ReservationWaiting::getReservationDate,
                        ReservationWaiting::getTime, ReservationWaiting::getTheme)
                .containsExactly(reservationWaiting.getName(), reservationWaiting.getReservationDate(),
                        reservationWaiting.getTime(), reservationWaiting.getTheme());
    }

    @Test
    void 생성된_예약_대기는_id가_존재한다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId(member, LocalDateTime.now(),
                        new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme))
        );

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 예약_대기를_삭제한다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId(member, LocalDateTime.now(),
                        new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme))
        );

        reservationWaitingRepository.deleteById(saved.getId());

        assertThat(reservationWaitingRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_대기를_삭제해도_예외가_발생하지_않는다() {
        assertThatNoException().isThrownBy(() -> reservationWaitingRepository.deleteById(999L));
    }

    @Test
    void 특정_슬롯과_회원에_예약_대기가_존재하면_true를_반환한다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(member, LocalDateTime.now(), slot));

        boolean result = reservationWaitingRepository.existsByMemberAndSlot(member, slot);

        assertThat(result).isTrue();
    }

    @Test
    void 특정_슬롯과_회원에_예약_대기가_존재하지_않으면_false를_반환한다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);

        boolean result = reservationWaitingRepository.existsByMemberAndSlot(member, slot);

        assertThat(result).isFalse();
    }

    @Test
    void 회원이_다르면_같은_슬롯이어도_false를_반환한다() {
        Member max = saveMember("맥스");
        Member roji = saveMember("로지");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(max, LocalDateTime.now(), slot));

        boolean result = reservationWaitingRepository.existsByMemberAndSlot(roji, slot);

        assertThat(result).isFalse();
    }

    @Test
    void 첫번째_대기자의_순번은_1이다() {
        Member member = saveMember("맥스");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId(member, LocalDateTime.now(), slot)
        );

        int order = reservationWaitingRepository.countOrder(slot, saved.getId());

        assertThat(order).isEqualTo(1);
    }

    @Test
    void 두번째_대기자의_순번은_2이다() {
        Member max = saveMember("맥스");
        Member roji = saveMember("로지");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(max, LocalDateTime.now(), slot));
        ReservationWaiting second = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId(roji, LocalDateTime.now(), slot)
        );

        int order = reservationWaitingRepository.countOrder(slot, second.getId());

        assertThat(order).isEqualTo(2);
    }

    @Test
    void 다른_슬롯의_대기는_순번_계산에_포함되지_않는다() {
        Member max = saveMember("맥스");
        Member roji = saveMember("로지");
        ReservationTime savedTime1 = saveTime(10, 0);
        ReservationTime savedTime2 = saveTime(11, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot1 = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime1, savedTheme);
        ReservationSlot slot2 = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime2, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(max, LocalDateTime.now(), slot1));
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId(roji, LocalDateTime.now(), slot2)
        );

        int order = reservationWaitingRepository.countOrder(slot2, saved.getId());

        assertThat(order).isEqualTo(1);
    }

    @Test
    void 전체_대기_목록을_조회한다() {
        Member max = saveMember("맥스");
        Member roji = saveMember("로지");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(max, LocalDateTime.now(), slot));
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(roji, LocalDateTime.now(), slot));

        List<ReservationWaiting> result = reservationWaitingRepository.findAll();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst().getName()).isEqualTo("맥스")
        );
    }

    @Test
    void 회원_ID로_대기_목록을_조회한다() {
        Member max = saveMember("맥스");
        Member roji = saveMember("로지");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(max, LocalDateTime.now(), slot));
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId(roji, LocalDateTime.now(), slot));

        List<ReservationWaiting> result = reservationWaitingRepository.findByMember_IdOrderByCreatedAt(max.getId());

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("맥스")
        );
    }

    @Test
    void 존재하지_않는_회원_ID로_조회하면_빈_목록을_반환한다() {
        List<ReservationWaiting> result = reservationWaitingRepository.findByMember_IdOrderByCreatedAt(999L);

        assertThat(result).isEmpty();
    }

    private Member saveMember(String name) {
        return memberRepository.save(Member.createWithoutId(name));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }
}
