package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약을_생성한다() {
        Member member = saveMember("브라운");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation reservation = Reservation.createWithoutId(member,
                new ReservationSlot(LocalDate.of(2026, 5, 5), savedTime, savedTheme));

        Reservation saved = reservationRepository.save(reservation);

        assertThat(saved)
                .extracting(Reservation::getId, Reservation::getName, Reservation::getDate, Reservation::getTime,
                        Reservation::getTheme)
                .containsExactly(saved.getId(), reservation.getName(), reservation.getDate(), reservation.getTime(),
                        reservation.getTheme());
    }

    @Test
    void 예약_목록을_조회한다() {
        ReservationTime savedTime1 = saveTime(10, 0);
        ReservationTime savedTime2 = saveTime(11, 0);
        ReservationTime savedTime3 = saveTime(12, 0);
        ReservationTime savedTime4 = saveTime(13, 0);
        ReservationTime savedTime5 = saveTime(14, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationRepository.save(Reservation.createWithoutId(saveMember("브라운"), new ReservationSlot(date, savedTime1, savedTheme)));
        reservationRepository.save(Reservation.createWithoutId(saveMember("로지"), new ReservationSlot(date, savedTime2, savedTheme)));
        reservationRepository.save(Reservation.createWithoutId(saveMember("러키"), new ReservationSlot(date, savedTime3, savedTheme)));
        reservationRepository.save(Reservation.createWithoutId(saveMember("러로"), new ReservationSlot(date, savedTime4, savedTheme)));
        reservationRepository.save(Reservation.createWithoutId(saveMember("밤밤"), new ReservationSlot(date, savedTime5, savedTheme)));

        List<Reservation> reservations = reservationRepository.findAll();

        assertAll(
                () -> assertThat(reservations).hasSize(5),
                () -> assertThat(reservations.getFirst().getName()).isEqualTo("브라운")
        );
    }

    @Test
    void 특정_시간에_예약이_존재하면_true를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.save(Reservation.createWithoutId(member,
                new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme)));

        boolean result = reservationRepository.existsByTimeId(time.getId());

        assertThat(result).isTrue();
    }

    @Test
    void 특정_시간에_예약이_존재하지_않으면_false를_반환한다() {
        boolean result = reservationRepository.existsByTimeId(999L);

        assertThat(result).isFalse();
    }

    @Test
    void 특정_테마에_예약이_존재하면_true를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        reservationRepository.save(Reservation.createWithoutId(member,
                new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme)));

        boolean result = reservationRepository.existsByThemeId(theme.getId());

        assertThat(result).isTrue();
    }

    @Test
    void 특정_테마에_예약이_존재하지_않으면_false를_반환한다() {
        boolean result = reservationRepository.existsByThemeId(999L);

        assertThat(result).isFalse();
    }

    @Test
    void 테마_아이디와_선택_날짜에_해당하는_예약_목록을_조회한다() {
        ReservationTime savedTime = saveTime(10, 0);
        Theme theme1 = saveTheme("방탈출1", "설명1", "https://asdfsdf.sdfs");
        Theme theme2 = saveTheme("방탈출2", "설명2", "https://asdfsdf.sdfs");
        LocalDate date = LocalDate.of(2026, 5, 5);

        reservationRepository.save(Reservation.createWithoutId(saveMember("러키"), new ReservationSlot(date, savedTime, theme1)));
        reservationRepository.save(Reservation.createWithoutId(saveMember("로지"), new ReservationSlot(date, savedTime, theme2)));

        List<Reservation> result = reservationRepository.findBySlot_Theme_IdAndSlot_Date(theme1.getId(), date);

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("러키")
        );
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_존재하면_true를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        reservationRepository.save(Reservation.createWithoutId(member, new ReservationSlot(date, time, theme)));

        boolean result = reservationRepository.existsBySlot(new ReservationSlot(date, time, theme));

        assertThat(result).isTrue();
    }

    @Test
    void 날짜_시간_테마가_모두_같은_예약이_없으면_false를_반환한다() {
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);

        boolean result = reservationRepository.existsBySlot(new ReservationSlot(date, time, theme));

        assertThat(result).isFalse();
    }

    @Test
    void 날짜_시간_테마_회원이_모두_같은_예약이_존재하면_true를_반환한다() {
        Member member = saveMember("브라운");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        reservationRepository.save(Reservation.createWithoutId(member, new ReservationSlot(date, time, theme)));

        boolean result = reservationRepository.existsByMemberAndSlot(member, new ReservationSlot(date, time, theme));

        assertThat(result).isTrue();
    }

    @Test
    void 날짜_시간_테마_회원이_모두_같은_예약이_없으면_false를_반환한다() {
        Member member = saveMember("브라운");
        Member other = saveMember("로지");
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);

        boolean result = reservationRepository.existsByMemberAndSlot(other, new ReservationSlot(date, time, theme));

        assertThat(result).isFalse();
    }

    @Test
    void 예약을_수정한다() {
        Member member = saveMember("브라운");
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        Reservation saved = reservationRepository.save(
                Reservation.createWithoutId(member, new ReservationSlot(LocalDate.of(2026, 5, 5), time1, theme)));

        saved.changeSlot(new ReservationSlot(LocalDate.of(2026, 5, 6), time2, theme));
        Reservation updated = reservationRepository.save(saved);

        assertAll(
                () -> assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 5, 6)),
                () -> assertThat(updated.getTime().getId()).isEqualTo(time2.getId())
        );
    }

    @Test
    void 예약을_삭제한다() {
        Member member = saveMember("예약자");
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        Reservation saved = reservationRepository.save(
                Reservation.createWithoutId(member, new ReservationSlot(LocalDate.of(2026, 5, 5), savedTime, savedTheme)));

        reservationRepository.deleteById(saved.getId());

        assertThat(reservationRepository.findAll()).isEmpty();
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
