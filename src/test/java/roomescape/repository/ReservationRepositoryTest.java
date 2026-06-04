package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;


@JdbcTest
@Import(value = {
        ReservationRepository.class,
        ReservationTimeRepository.class,
        ThemeRepository.class,
        SlotRepository.class
})
class ReservationRepositoryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 10);
    private static final LocalDate FUTURE = LocalDate.of(2099, 1, 1);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SlotRepository slotRepository;

    private ReservationTime saveTimeAndGet(int hour) {
        return timeRepository.save(ReservationTime.of(LocalTime.of(hour, 0)));
    }

    private Theme saveThemeAndGet(String name) {
        return themeRepository.save(
                Theme.create(new ThemeName(name), name + " 설명", new ThumbnailUrl("https://test-theme.com")));
    }

    private Theme saveThemeAndGet() {
        return saveThemeAndGet("테마");
    }

    private Slot getSlotOrCreate(LocalDate date, ReservationTime time, Theme theme) {
        return slotRepository.findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> slotRepository.save(Slot.create(new ReservationDate(date), time, theme)));
    }

    private Reservation reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservation(name, date, time, theme, Status.APPROVED);
    }

    private Reservation reservation(String name, LocalDate date, ReservationTime time, Theme theme, Status status) {
        Slot slot = getSlotOrCreate(date, time, theme);
        return Reservation.load(0L, new ReservationName(name), slot, status, LocalDateTime.now());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        void 예약을_저장하면_ID가_부여된_예약이_반환된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation saved = reservationRepository.save(reservation("제제", FUTURE, time, theme));

            assertSoftly(soft -> {
                soft.assertThat(saved.getId()).isPositive();
                soft.assertThat(saved.getName().getValue()).isEqualTo("제제");
                soft.assertThat(saved.getDate().getValue()).isEqualTo(FUTURE);
            });
        }

        @Test
        void 여러_예약을_저장하면_각기_다른_ID가_부여된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation first = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            Reservation second = reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        void 예약이_없으면_빈_목록을_반환한다() {
            assertThat(reservationRepository.findAll()).isEmpty();
        }

        @Test
        void 저장된_예약을_모두_반환한다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            assertThat(reservationRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAll (name filter)")
    class FindAllByName {

        @Test
        void 이름으로_조회하면_해당_이름의_예약만_반환된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("달수", FUTURE.plusDays(1), time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(2), time, theme));

            assertThat(reservationRepository.findAll().stream()
                    .filter(r -> r.getName().getValue().equals("달수"))
                    .toList()).hasSize(2);
        }

        @Test
        void 존재하지_않는_이름으로_조회하면_빈_목록을_반환한다() {
            assertThat(reservationRepository.findAll().stream()
                    .filter(r -> r.getName().getValue().equals("없는이름"))
                    .toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        void ID로_조회하면_해당_예약이_반환된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.findById(saved.getId())).isPresent();
        }

        @Test
        void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
            assertThat(reservationRepository.findById(Long.MAX_VALUE)).isEmpty();
        }

        @Test
        void 조회한_예약의_필드가_저장된_값과_일치한다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            Reservation found = reservationRepository.findById(saved.getId()).orElseThrow();

            assertSoftly(soft -> {
                soft.assertThat(found.getName().getValue()).isEqualTo("달수");
                soft.assertThat(found.getDate().getValue()).isEqualTo(FUTURE);
                soft.assertThat(found.getTime().getId()).isEqualTo(time.getId());
                soft.assertThat(found.getTheme().getId()).isEqualTo(theme.getId());
            });
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void 예약을_수정하면_변경된_내용이_반영된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time1 = saveTimeAndGet(10);
            ReservationTime time2 = saveTimeAndGet(14);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time1, theme));
            Reservation target = reservation("민구", FUTURE.plusDays(1), time2, theme);

            Reservation updated = reservationRepository.update(saved.getId(), target);

            assertSoftly(soft -> {
                soft.assertThat(updated.getId()).isEqualTo(saved.getId());
                soft.assertThat(updated.getName().getValue()).isEqualTo("민구");
                soft.assertThat(updated.getDate().getValue()).isEqualTo(FUTURE.plusDays(1));
                soft.assertThat(updated.getTime().getId()).isEqualTo(time2.getId());
            });
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        void 예약을_삭제하면_조회할_수_없다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.deleteById(saved.getId());

            assertThat(reservationRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        void 예약을_삭제하면_전체_목록에서도_제외된다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            Reservation r1 = reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            reservationRepository.deleteById(r1.getId());

            assertThat(reservationRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllBySlot")
    class FindAllBySlot {

        @Test
        void 같은_슬롯의_예약을_모두_반환한다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE, time, theme));

            Slot slot = getSlotOrCreate(FUTURE, time, theme);
            assertThat(reservationRepository.findAllBySlot(slot)).hasSize(2);
        }

        @Test
        void 다른_날짜의_예약은_포함되지_않는다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));
            reservationRepository.save(reservation("민구", FUTURE.plusDays(1), time, theme));

            Slot slot = getSlotOrCreate(FUTURE, time, theme);
            assertThat(reservationRepository.findAllBySlot(slot)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("existsByTimeId / existsByThemeId")
    class ExistsByFk {

        @Test
        void 해당_시간으로_예약이_있으면_true를_반환한다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.existsByTimeId(time.getId())).isTrue();
        }

        @Test
        void 해당_시간으로_예약이_없으면_false를_반환한다() {
            ReservationTime time = saveTimeAndGet(10);

            assertThat(reservationRepository.existsByTimeId(time.getId())).isFalse();
        }

        @Test
        void 해당_테마로_예약이_있으면_true를_반환한다() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(10);

            reservationRepository.save(reservation("달수", FUTURE, time, theme));

            assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
        }

        @Test
        void 해당_테마로_예약이_없으면_false를_반환한다() {
            Theme theme = saveThemeAndGet("테마1");

            assertThat(reservationRepository.existsByThemeId(theme.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("existsBySlotAndName")
    class Exists {

        @Test
        void 같은_슬롯과_이름이면_true() {
            Theme theme = saveThemeAndGet("테마1");
            ReservationTime time = saveTimeAndGet(14);

            String name = "달수";
            reservationRepository.save(reservation(name, TODAY, time, theme));

            Slot slot = getSlotOrCreate(TODAY, time, theme);
            assertThat(reservationRepository.existsBySlotAndName(slot, name)).isTrue();
        }

        @Test
        void 슬롯이나_이름이_다르면_false() {
            Theme theme1 = saveThemeAndGet("테마1");
            Theme theme2 = saveThemeAndGet("테마2");
            ReservationTime time1 = saveTimeAndGet(14);

            String name = "달수";
            reservationRepository.save(reservation(name, TODAY, time1, theme1));

            Slot slot1 = getSlotOrCreate(TODAY, time1, theme1);

            assertSoftly(soft -> {
                soft.assertThat(reservationRepository.existsBySlotAndName(slot1, "other")).isFalse();
                Slot slot2 = getSlotOrCreate(TODAY, time1, theme2);
                soft.assertThat(reservationRepository.existsBySlotAndName(slot2, name)).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("existsApproved (slot 기준)")
    class ExistsApproved {

        @Test
        void APPROVED_예약이_있으면_true() {
            Theme theme = saveThemeAndGet();
            ReservationTime time = saveTimeAndGet(14);

            reservationRepository.save(reservation("달수", TODAY, time, theme, Status.APPROVED));

            Slot slot = getSlotOrCreate(TODAY, time, theme);
            assertThat(reservationRepository.findAllBySlot(slot).stream()
                    .anyMatch(r -> r.getStatus() == Status.APPROVED)).isTrue();
        }

        @Test
        void WAITING_예약만_있으면_false() {
            Theme theme = saveThemeAndGet();
            ReservationTime time = saveTimeAndGet(14);

            reservationRepository.save(reservation("달수", TODAY, time, theme, Status.WAITING));

            Slot slot = getSlotOrCreate(TODAY, time, theme);
            assertThat(reservationRepository.findAllBySlot(slot).stream()
                    .anyMatch(r -> r.getStatus() == Status.APPROVED)).isFalse();
        }
    }

    @Nested
    @DisplayName("findFirstWaitingBySlot")
    class FindFirstWaiting {

        @Test
        void WAITING_예약이_있으면_가장_먼저_생성된_예약을_반환한다() {
            Theme theme = saveThemeAndGet();
            ReservationTime time = saveTimeAndGet(14);

            reservationRepository.save(reservation("달수", TODAY, time, theme, Status.APPROVED));
            Reservation first = reservationRepository.save(reservation("민구", TODAY, time, theme, Status.WAITING));
            reservationRepository.save(reservation("철수", TODAY, time, theme, Status.WAITING));

            Slot slot = getSlotOrCreate(TODAY, time, theme);
            assertThat(reservationRepository.findFirstWaitingBySlot(slot))
                    .isPresent()
                    .get()
                    .extracting(Reservation::getId)
                    .isEqualTo(first.getId());
        }

        @Test
        void WAITING_예약이_없으면_빈_Optional을_반환한다() {
            Theme theme = saveThemeAndGet();
            ReservationTime time = saveTimeAndGet(14);

            reservationRepository.save(reservation("달수", TODAY, time, theme, Status.APPROVED));

            Slot slot = getSlotOrCreate(TODAY, time, theme);
            assertThat(reservationRepository.findFirstWaitingBySlot(slot)).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        void WAITING_예약을_APPROVED로_변경할_수_있다() {
            Theme theme = saveThemeAndGet();
            ReservationTime time = saveTimeAndGet(14);
            Reservation saved = reservationRepository.save(reservation("달수", FUTURE, time, theme, Status.WAITING));
            reservationRepository.updateStatus(saved.getId(), Status.APPROVED);

            assertThat(reservationRepository.findById(saved.getId()))
                    .isPresent()
                    .get()
                    .extracting(Reservation::getStatus)
                    .isEqualTo(Status.APPROVED);
        }
    }
}
