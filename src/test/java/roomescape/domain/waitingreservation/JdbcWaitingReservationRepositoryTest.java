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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

@JdbcTest
class JdbcWaitingReservationRepositoryTest {

    private static final long DATE_ID = 101L;
    private static final long TIME_ID = 201L;
    private static final long THEME_ID = 301L;
    private static final LocalDate PLAY_DAY = LocalDate.of(2026, 5, 10);
    private static final LocalTime START_AT = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WaitingReservationRepository waitingReservationRepository;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        waitingReservationRepository = new JdbcWaitingReservationRepository(jdbcTemplate);

        jdbcTemplate.update("insert into reservation_date(id, play_day) values (?, ?)", DATE_ID, PLAY_DAY.toString());
        jdbcTemplate.update("insert into reservation_time(id, start_at) values (?, ?)", TIME_ID, START_AT.toString());
        jdbcTemplate.update(
                "insert into theme(id, name, content, url) values (?, ?, ?, ?)",
                THEME_ID, "공포", "테마 내용", "/themes/scary"
        );

        date = ReservationDate.of(DATE_ID, PLAY_DAY);
        time = ReservationTime.of(TIME_ID, START_AT);
        theme = Theme.of(THEME_ID, "공포", "테마 내용", "/themes/scary");
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
        assertThat(oldest.getDate().getId()).isEqualTo(DATE_ID);
        assertThat(oldest.getDate().getPlayDay()).isEqualTo(PLAY_DAY);
        assertThat(oldest.getTime().getId()).isEqualTo(TIME_ID);
        assertThat(oldest.getTime().getStartAt()).isEqualTo(START_AT);
        assertThat(oldest.getTheme().getId()).isEqualTo(THEME_ID);
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
        Slot otherSlot = insertSlot(
                102L, LocalDate.of(2026, 5, 11),
                202L, LocalTime.of(11, 0),
                302L, "스릴러"
        );
        waitingReservationRepository.save(waiting("다른슬롯", otherSlot, LocalDateTime.of(2026, 5, 6, 10, 0)));
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 8, 10, 0)));

        WaitingReservation oldest = waitingReservationRepository
                .findOldestBySlot(DATE_ID, TIME_ID, THEME_ID)
                .orElseThrow();

        assertThat(oldest.getName()).isEqualTo("이산");
    }

    @Test
    void 사용자_이름으로_예약_대기_목록을_조회하면_각_슬롯의_순번을_반환한다() {
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 8, 10, 0)));

        Slot secondSlot = insertSlot(
                102L, LocalDate.of(2026, 5, 11),
                202L, LocalTime.of(11, 0),
                302L, "스릴러"
        );
        waitingReservationRepository.save(waiting("이산", secondSlot, LocalDateTime.of(2026, 5, 7, 11, 0)));
        waitingReservationRepository.save(waiting("브리", secondSlot, LocalDateTime.of(2026, 5, 8, 11, 0)));

        Slot thirdSlot = insertSlot(
                103L, LocalDate.of(2026, 5, 12),
                203L, LocalTime.of(13, 0),
                303L, "미스터리"
        );
        waitingReservationRepository.save(waiting("나무", thirdSlot, LocalDateTime.of(2026, 5, 7, 12, 0)));
        waitingReservationRepository.save(waiting("고래", thirdSlot, LocalDateTime.of(2026, 5, 8, 12, 0)));
        waitingReservationRepository.save(waiting("이산", thirdSlot, LocalDateTime.of(2026, 5, 9, 12, 0)));

        List<WaitingReservationWithRank> waitings = waitingReservationRepository.findAllByNameWithRank("이산");

        assertThat(waitings).hasSize(3);
        assertThat(waitings).extracting(result -> result.waitingReservation().getName())
                .containsOnly("이산");
        assertThat(waitings).extracting(result -> result.waitingReservation().getDate().getId())
                .containsExactly(DATE_ID, 102L, 103L);
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

    @Test
    void 예약_대기를_취소하면_같은_슬롯의_남은_예약_대기_순번이_재계산된다() {
        WaitingReservation whale = waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 8, 10, 0)));
        waitingReservationRepository.save(waiting("보예", LocalDateTime.of(2026, 5, 9, 10, 0)));

        waitingReservationRepository.deleteById(whale.getId());

        assertThat(waitingReservationRepository.findAllByNameWithRank("이산"))
                .singleElement()
                .extracting(WaitingReservationWithRank::rank)
                .isEqualTo(1L);

        assertThat(waitingReservationRepository.findAllByNameWithRank("보예"))
                .singleElement()
                .extracting(WaitingReservationWithRank::rank)
                .isEqualTo(2L);
    }

    @Test
    void 이름으로_예약_시작_시각이_지나지_않은_예약_대기와_순번을_조회한다() {
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 7, 10, 0)));

        Slot futureSlot = insertSlot(
                102L, LocalDate.of(2026, 5, 10),
                202L, LocalTime.of(10, 1),
                302L, "미래"
        );
        waitingReservationRepository.save(waiting("고래", futureSlot, LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("이산", futureSlot, LocalDateTime.of(2026, 5, 8, 10, 0)));

        List<WaitingReservationWithRank> waitings = waitingReservationRepository.findUpcomingByNameWithRank(
                "이산",
                LocalDate.of(2026, 5, 10),
                LocalTime.of(10, 0)
        );

        assertThat(waitings).singleElement()
                .extracting(result -> result.waitingReservation().getTime().getStartAt())
                .isEqualTo(LocalTime.of(10, 1));
        assertThat(waitings).singleElement()
                .extracting(WaitingReservationWithRank::rank)
                .isEqualTo(2L);
    }

    private WaitingReservation waiting(String name, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, date, time, theme, createdAt);
    }

    private WaitingReservation waiting(String name, Slot slot, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    private Slot insertSlot(long dateId, LocalDate playDay, long timeId, LocalTime startAt, long themeId,
                            String themeName) {
        jdbcTemplate.update("insert into reservation_date(id, play_day) values (?, ?)", dateId, playDay.toString());
        jdbcTemplate.update("insert into reservation_time(id, start_at) values (?, ?)", timeId, startAt.toString());
        jdbcTemplate.update(
                "insert into theme(id, name, content, url) values (?, ?, ?, ?)",
                themeId, themeName, "테마 내용", "/themes/" + themeId
        );
        return new Slot(
                ReservationDate.of(dateId, playDay),
                ReservationTime.of(timeId, startAt),
                Theme.of(themeId, themeName, "테마 내용", "/themes/" + themeId)
        );
    }

    private record Slot(
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
    }

}
