package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
class ReservationWaitingRepositoryTest {

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 예약_대기를_생성한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(
                "맥스", LocalDateTime.now(),
                new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme)
        );

        // when
        ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        // then
        assertThat(saved)
                .extracting(ReservationWaiting::getName, ReservationWaiting::getReservationDate,
                        ReservationWaiting::getTime, ReservationWaiting::getTheme)
                .containsExactly(reservationWaiting.getName(), reservationWaiting.getReservationDate(),
                        reservationWaiting.getTime(), reservationWaiting.getTheme());
    }

    @Test
    void 생성된_예약_대기는_id가_존재한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        // when
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(),
                        new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme))
        );

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 예약_대기를_삭제한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(),
                        new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme))
        );

        // when
        reservationWaitingRepository.deleteById(saved.getId());

        // then
        assertThat(reservationWaitingRepository.findAll()).isEmpty();
    }

    @Test
    void 존재하지_않는_대기를_삭제해도_예외가_발생하지_않는다() {
        assertThatNoException().isThrownBy(() -> reservationWaitingRepository.deleteById(999L));
    }

    @Test
    void 특정_날짜_테마_시간_사용자_이름에_예약_대기가_존재하면_true를_반환한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot));

        // when
        boolean result = reservationWaitingRepository.existsByNameAndSlot("맥스", slot);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_날짜_테마_시간_사용자_이름에_예약_대기가_존재하지_않으면_false를_반환한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);

        // when
        boolean result = reservationWaitingRepository.existsByNameAndSlot("맥스", slot);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 이름이_다르면_같은_슬롯이어도_false를_반환한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot));

        // when
        boolean result = reservationWaitingRepository.existsByNameAndSlot("로지", slot);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 첫번째_대기자의_순번은_1이다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot)
        );

        // when
        int order = reservationWaitingRepository.countOrder(slot, saved.getId());

        // then
        assertThat(order).isEqualTo(1);
    }

    @Test
    void 두번째_대기자의_순번은_2이다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot));
        ReservationWaiting second = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), slot)
        );

        // when
        int order = reservationWaitingRepository.countOrder(slot, second.getId());

        // then
        assertThat(order).isEqualTo(2);
    }

    @Test
    void 다른_슬롯의_대기는_순번_계산에_포함되지_않는다() {
        // given
        ReservationTime savedTime1 = saveTime(10, 0);
        ReservationTime savedTime2 = saveTime(11, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot1 = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime1, savedTheme);
        ReservationSlot slot2 = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime2, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot1));
        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), slot2)
        );

        // when
        int order = reservationWaitingRepository.countOrder(slot2, saved.getId());

        // then
        assertThat(order).isEqualTo(1);
    }

    @Test
    void 전체_대기_목록을_조회한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot));
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), slot));

        // when
        List<ReservationWaiting> result = reservationWaitingRepository.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst().getName()).isEqualTo("맥스")
        );
    }

    @Test
    void 이름으로_대기_목록을_조회한다() {
        // given
        ReservationTime savedTime = saveTime(10, 0);
        Theme savedTheme = saveTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 10), savedTime, savedTheme);
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("맥스", LocalDateTime.now(), slot));
        reservationWaitingRepository.save(ReservationWaiting.createWithoutId("로지", LocalDateTime.now(), slot));

        // when
        List<ReservationWaiting> result = reservationWaitingRepository.findByNameOrderByCreatedAt("맥스");

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getFirst().getName()).isEqualTo("맥스")
        );
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈_목록을_반환한다() {
        // when
        List<ReservationWaiting> result = reservationWaitingRepository.findByNameOrderByCreatedAt("없는이름");

        // then
        assertThat(result).isEmpty();
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }
}
