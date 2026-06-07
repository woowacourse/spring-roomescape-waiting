package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@JdbcTest
@Import({JdbcReservationTimeRepository.class, JdbcThemeRepository.class,
        JdbcWaitingRepository.class})
class JdbcWaitingRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 대기를_등록하면_id를_부여한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting waiting = Waiting.create("루드비코",
                ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime, theme), 1L);

        // when
        Waiting saved = waitingRepository.save(waiting);

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void 대기를_id로_조회한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting waiting = Waiting.create("루드비코",
                ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime, theme), 1L);

        // when
        Waiting saved = waitingRepository.save(waiting);
        Optional<Waiting> result = waitingRepository.findById(saved.getId());

        // then
        assertThat(result).isPresent().get().usingRecursiveComparison().isEqualTo(saved);
    }

    @Test
    void 존재하지_않는_대기id로_대기를_조회하면_빈_Optional을_반환한다() {
        // when
        Optional<Waiting> result = waitingRepository.findById(2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 대기목록을_이름으로_조회하면_대기와_각_대기의_현재_대기_순번을_반환한다() {
        // given
        String name = "루드비코";
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        waitingRepository.save(
                Waiting.create("코코",
                        ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime1, theme),
                        1L));
        Waiting waiting1 = waitingRepository.save(
                Waiting.create(name,
                        ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime1, theme),
                        4L));
        Waiting waiting2 = waitingRepository.save(
                Waiting.create(name,
                        ReservationSlot.of(LocalDate.parse("2026-05-07"), reservationTime2, theme),
                        1L));

        WaitingWithOrder waitingWithOrder1 = WaitingWithOrder.of(waiting1, 2L);
        WaitingWithOrder waitingWithOrder2 = WaitingWithOrder.of(waiting2, 1L);

        // when
        List<WaitingWithOrder> result = waitingRepository.findByName(name);

        // then
        assertThat(result).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(waitingWithOrder1, waitingWithOrder2);
    }

    @Test
    void 존재하지_않는_이름으로_대기목록을_조회하면_빈_리스트를_반환한다() {
        // given
        String name = "코코";
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        waitingRepository.save(
                Waiting.create("루드비코",
                        ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime1, theme),
                        1L));
        waitingRepository.save(
                Waiting.create("루드비코",
                        ReservationSlot.of(LocalDate.parse("2026-05-07"), reservationTime2, theme),
                        1L));
        waitingRepository.save(
                Waiting.create("네오",
                        ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime1, theme),
                        2L));

        // when
        List<WaitingWithOrder> result = waitingRepository.findByName(name);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 대기를_대기_id로_삭제한다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Waiting saved = waitingRepository.save(
                Waiting.create("루드비코",
                        ReservationSlot.of(LocalDate.parse("2026-05-06"), reservationTime, theme),
                        1L));

        // when
        waitingRepository.delete(saved.getId());
        Optional<Waiting> result = waitingRepository.findById(saved.getId());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void 존재하지_않는_대기id로_대기를_삭제해도_예외가_발생하지_않는다() {
        // given
        Long invalidId = 9999L;

        // when & then
        assertDoesNotThrow(() -> {
            waitingRepository.delete(invalidId);
        });
    }

    @Test
    void 이름_날짜_시간_테마가_모두_일치하는_대기가_이미_존재하면_true를_반환한다() {
        // given
        String name = "루드비코";
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);
        Waiting waiting = Waiting.create(name, slot, 1L);
        waitingRepository.save(waiting);

        // when
        boolean exists = waitingRepository.existsByNameAndSlot(name, slot);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 이름_날짜_시간_테마가_모두_일치하는_대기가_존재하지_않으면_false를_반환한다() {
        // given
        String name = "루드비코";
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);

        // when
        boolean exists = waitingRepository.existsByNameAndSlot(name, slot);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 이름_날짜_시간_테마_중_하나라도_조건이_일치하지_않으면_존재하지_않는_대기로_판단한다() {
        // given
        String name = "루드비코";
        LocalDate date = LocalDate.parse("2026-05-06");
        LocalDate wrongDate = date.plusDays(1);
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);
        ReservationSlot worngSlot = ReservationSlot.of(wrongDate, reservationTime, theme);
        waitingRepository.save(Waiting.create(name, slot, 1L));

        // when
        boolean exists = waitingRepository.existsByNameAndSlot(name, worngSlot);

        // then
        assertThat(exists).isFalse();

    }

    @Test
    void 한_슬롯에_존재하는_대기_중_마지막_대기순서를_반환한다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        waitingRepository.save(
                Waiting.create("루드비코", ReservationSlot.of(date, reservationTime, theme), 1L));
        waitingRepository.save(
                Waiting.create("코코", ReservationSlot.of(date, reservationTime, theme), 4L));
        waitingRepository.save(
                Waiting.create("네오", ReservationSlot.of(date, reservationTime, theme), 2L));

        // when
        Optional<Long> maxNum = waitingRepository.findMaxWaitingNumberBy(date, reservationTime,
                theme);

        // then
        assertThat(maxNum).contains(4L);
    }

    @Test
    void 다른_슬롯에_존재하는_대기의_마지막_대기순서는_반환하지_않는다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        ReservationTime otherReservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        waitingRepository.save(
                Waiting.create("루드비코", ReservationSlot.of(date, reservationTime, theme), 1L));
        waitingRepository.save(
                Waiting.create("코코", ReservationSlot.of(date, reservationTime, theme), 4L));
        waitingRepository.save(
                Waiting.create("네오", ReservationSlot.of(date, reservationTime, theme), 2L));

        waitingRepository.save(
                Waiting.create("루드비코", ReservationSlot.of(date, otherReservationTime, theme), 2L));
        waitingRepository.save(
                Waiting.create("코코", ReservationSlot.of(date, otherReservationTime, theme), 10L));
        waitingRepository.save(
                Waiting.create("네오", ReservationSlot.of(date, otherReservationTime, theme), 5L));

        // when
        Optional<Long> maxNum = waitingRepository.findMaxWaitingNumberBy(date, reservationTime,
                theme);

        // then
        assertThat(maxNum).contains(4L);
    }

    @Test
    void 한_슬롯에_존재하는_대기가_없으면_빈_Optional을_반환한다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        // when
        Optional<Long> maxNum = waitingRepository.findMaxWaitingNumberBy(date, reservationTime,
                theme);

        // then
        assertThat(maxNum).isEmpty();
    }

    @Test
    void 한_슬롯에_동일한_사용자의_대기를_등록하려고_하면_DuplicateKeyException이_발생한다() {
        // given
        String name = "코코";
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));

        // when
        waitingRepository.save(
                Waiting.create(name, ReservationSlot.of(date, reservationTime, theme), 1L));

        // then
        assertThatThrownBy(() -> waitingRepository.save(
                Waiting.create(name, ReservationSlot.of(date, reservationTime, theme), 4L))
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 한_슬롯에_중복되는_대기번호로_대기를_등록하려고_하면_DuplicateKeyException이_발생한다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        Long waitingNumber = 3L;

        // when
        waitingRepository.save(
                Waiting.create("루드비코", ReservationSlot.of(date, reservationTime, theme),
                        waitingNumber));

        // then
        assertThatThrownBy(() -> waitingRepository.save(
                Waiting.create("코코", ReservationSlot.of(date, reservationTime, theme),
                        waitingNumber))
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 한_슬롯에_존재하는_대기_중_승격_가능한_대기를_반환한다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);

        Waiting waiting1 = waitingRepository.save(
                Waiting.create("루드비코", slot, 1L));
        Waiting waiting2 = waitingRepository.save(
                Waiting.create("코코", slot, 4L));
        Waiting waiting3 = waitingRepository.save(
                Waiting.create("네오", slot, 2L));

        // when
        Optional<Waiting> result = waitingRepository.findPromotableWaitingBySlotWithLock(slot);

        // then
        assertThat(result).isPresent().get().isEqualTo(waiting1);
    }

    @Test
    void 모든_대기를_조회하면_대기와_각_대기의_현재_대기_순번을_반환한다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-06");
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "example.com"));
        ReservationSlot slot = ReservationSlot.of(date, reservationTime, theme);

        Waiting waiting1 = waitingRepository.save(
                Waiting.create("루드비코", slot, 1L));
        Waiting waiting2 = waitingRepository.save(
                Waiting.create("코코", slot, 2L));
        Waiting waiting3 = waitingRepository.save(
                Waiting.create("네오", slot, 5L));
        Waiting waiting4 = waitingRepository.save(
                Waiting.create("브라운", slot, 7L));

        WaitingWithOrder waitingWithOrder1 = WaitingWithOrder.of(waiting1, 1L);
        WaitingWithOrder waitingWithOrder2 = WaitingWithOrder.of(waiting2, 2L);
        WaitingWithOrder waitingWithOrder3 = WaitingWithOrder.of(waiting3, 3L);
        WaitingWithOrder waitingWithOrder4 = WaitingWithOrder.of(waiting4, 4L);

        // when
        List<WaitingWithOrder> waitings = waitingRepository.findAll();

        // then
        assertThat(waitings).hasSize(4)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(waitingWithOrder1, waitingWithOrder2, waitingWithOrder3,
                        waitingWithOrder4);
    }
}
