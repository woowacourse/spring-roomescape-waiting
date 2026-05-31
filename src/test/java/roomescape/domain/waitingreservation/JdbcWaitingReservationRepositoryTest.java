package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationdate.JdbcReservationDateRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.JdbcReservationTimeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.JdbcThemeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

@JdbcTest
@Sql("/truncate.sql")
@Import({JdbcReservationDateRepository.class, JdbcReservationTimeRepository.class, JdbcThemeRepository.class})
class JdbcWaitingReservationRepositoryTest {

    private static final LocalDate PLAY_DAY = LocalDate.of(2026, 5, 10);
    private static final LocalTime START_AT = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private WaitingReservationRepository waitingReservationRepository;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        waitingReservationRepository = new JdbcWaitingReservationRepository(jdbcTemplate);

        date = reservationDateRepository.save(ReservationDate.createWithoutId(PLAY_DAY));
        time = reservationTimeRepository.save(ReservationTime.createWithoutId(START_AT));
        theme = themeRepository.save(Theme.createWithoutId("공포", "테마 내용", "/themes/scary"));
    }

    @Test
    void 같은_이름_날짜_테마_시간으로_예약_대기를_생성할_수_없다() {
        WaitingReservation waitingReservation = waiting("이산", LocalDateTime.of(2026, 5, 9, 10, 0));
        waitingReservationRepository.save(waitingReservation);

        assertThatThrownBy(() -> waitingReservationRepository.save(waitingReservation))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 가장_먼저_신청한_예약_대기를_가져온다() {
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 8, 10, 0)));
        waitingReservationRepository.save(waiting("보예", LocalDateTime.of(2026, 5, 9, 10, 0)));

        WaitingReservation oldest = waitingReservationRepository.findOldestBySlot(
                date.getId(),
                time.getId(),
                theme.getId()
        ).orElseThrow();

        assertThat(oldest.getName()).isEqualTo("이산");
        assertThat(oldest.getDate().getId()).isEqualTo(date.getId());
        assertThat(oldest.getDate().getPlayDay()).isEqualTo(PLAY_DAY);
        assertThat(oldest.getTime().getId()).isEqualTo(time.getId());
        assertThat(oldest.getTime().getStartAt()).isEqualTo(START_AT);
        assertThat(oldest.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(oldest.getTheme().getName()).isEqualTo("공포");
        assertThat(oldest.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 7, 10, 0));
    }

    @Test
    void 예약_대기가_없으면_가장_먼저_신청한_예약_대기를_조회할_수_없다() {
        assertThat(waitingReservationRepository.findOldestBySlot(
                        date.getId(),
                        time.getId(),
                        theme.getId()
                )
        ).isEmpty();
    }

    @Test
    void 특정_슬롯에서_가장_먼저_신청한_예약_대기를_가져온다() {
        Slot otherSlot = insertSlot(LocalDate.of(2026, 5, 11), LocalTime.of(11, 0), "스릴러");
        waitingReservationRepository.save(waiting("다른슬롯", otherSlot, LocalDateTime.of(2026, 5, 6, 10, 0)));
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 8, 10, 0)));

        WaitingReservation oldest = waitingReservationRepository
                .findOldestBySlot(date.getId(), time.getId(), theme.getId())
                .orElseThrow();

        assertThat(oldest.getName()).isEqualTo("이산");
    }

    @Test
    void 사용자_이름으로_예약_대기_목록을_조회하면_각_슬롯의_순번을_반환한다() {
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 8, 10, 0)));

        Slot secondSlot = insertSlot(LocalDate.of(2026, 5, 11), LocalTime.of(11, 0), "스릴러");
        waitingReservationRepository.save(waiting("이산", secondSlot, LocalDateTime.of(2026, 5, 7, 11, 0)));
        waitingReservationRepository.save(waiting("브리", secondSlot, LocalDateTime.of(2026, 5, 8, 11, 0)));

        Slot thirdSlot = insertSlot(LocalDate.of(2026, 5, 12), LocalTime.of(13, 0), "미스터리");
        waitingReservationRepository.save(waiting("나무", thirdSlot, LocalDateTime.of(2026, 5, 7, 12, 0)));
        waitingReservationRepository.save(waiting("고래", thirdSlot, LocalDateTime.of(2026, 5, 8, 12, 0)));
        waitingReservationRepository.save(waiting("이산", thirdSlot, LocalDateTime.of(2026, 5, 9, 12, 0)));

        List<WaitingReservationWithRank> waitings = waitingReservationRepository.findAllByNameWithRank("이산");

        assertThat(waitings).hasSize(3);
        assertThat(waitings).extracting(result -> result.waitingReservation().getName())
                .containsOnly("이산");
        assertThat(waitings).extracting(result -> result.waitingReservation().getDate().getId())
                .containsExactly(date.getId(), secondSlot.date().getId(), thirdSlot.date().getId());
        assertThat(waitings).extracting(WaitingReservationWithRank::rank)
                .containsExactly(2L, 1L, 3L);
    }

    @Test
    void 대기_취소를_하면_정상_삭제한다() {
        WaitingReservation actual = waitingReservationRepository.save(
                waiting("고래", LocalDateTime.of(2026, 5, 7, 10, 0)));

        waitingReservationRepository.deleteById(actual.getId());

        assertThat(waitingReservationRepository.findById(actual.getId())).isEmpty();
    }

    private WaitingReservation waiting(String name, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, date, time, theme, createdAt);
    }

    private WaitingReservation waiting(String name, Slot slot, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    private Slot insertSlot(LocalDate playDay, LocalTime startAt, String themeName) {
        ReservationDate savedDate = reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
        Theme savedTheme = themeRepository.save(Theme.createWithoutId(themeName, "테마 내용", "/themes/" + themeName));
        return new Slot(savedDate, savedTime, savedTheme);
    }

    private record Slot(
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
    }
}
