package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.ReservationFixture.member;
import static roomescape.fixture.ReservationFixture.reservation;
import static roomescape.fixture.ReservationFixture.slot;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

@DataJpaTest
class ReservationRepositoryTest {

    private static final String THEME_NAME = "공포";
    private static final String THEME_DESCRIPTION = "무서운 테마";
    private static final String THEME_THUMBNAIL_IMAGE_URL = "https://example.com/horror.jpg";
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 8, 5);
    private static final LocalDate OTHER_RESERVATION_DATE = LocalDate.of(2026, 8, 6);
    private static final LocalTime RESERVATION_START_AT = LocalTime.of(10, 0);
    private static final LocalTime OTHER_RESERVATION_START_AT = LocalTime.of(11, 0);
    private static final LocalDateTime WAITING_CREATED_AT = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository repository;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL
        );
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);
    }

    @Test
    void save는_생성된_id를_부여한_예약을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        Reservation reservation = reservation("브라운", RESERVATION_DATE, time, theme);

        Reservation saved = repository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    void 같은_날짜_시간_테마로_저장하면_DataIntegrityViolationException을_던진다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        LocalDate date = RESERVATION_DATE;
        repository.saveAndFlush(reservation("브라운", date, time, theme));

        assertThatThrownBy(() -> repository.saveAndFlush(reservation("티뉴", date, time, theme)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 같은_날짜_시간_테마로_수정하면_DataIntegrityViolationException을_던진다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('11:00')");
        Long otherTimeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = '11:00'",
                Long.class
        );

        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        ReservationTime otherTime = new ReservationTime(otherTimeId, OTHER_RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        LocalDate date = RESERVATION_DATE;
        repository.save(reservation("브라운", date, time, theme));
        Reservation saved = repository.save(reservation("티뉴", date, otherTime, theme));

        Reservation updated = reservation(saved.getId(), saved.getName(), date, time, theme);

        assertThatThrownBy(() -> repository.saveAndFlush(updated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findReservedTimeIdsByDateAndTheme는_같은_날짜와_테마의_예약_시간_id만_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        LocalDate targetDate = RESERVATION_DATE;
        repository.save(reservation("브라운", targetDate, time, theme));
        repository.save(reservation("티뉴", OTHER_RESERVATION_DATE, time, theme));

        List<Long> result = repository.findBySlot_DateAndSlot_Theme(targetDate, theme)
                .stream()
                .map(reservation -> reservation.getTime().getId())
                .collect(Collectors.toList());

        assertThat(result).containsExactly(timeId);
    }

    @Test
    void findBySlot은_해당_슬롯의_예약을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        LocalDate date = RESERVATION_DATE;
        Slot targetSlot = slot(date, time, theme);
        Reservation saved = repository.save(reservation("브라운", date, time, theme));

        Optional<Reservation> result = repository.findBySlot(targetSlot);

        assertThat(result).contains(saved);
    }

    @Test
    void findWithLockById는_해당_id의_예약을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        Reservation saved = repository.save(reservation("브라운", RESERVATION_DATE, time, theme));

        Optional<Reservation> result = repository.findWithLockById(saved.getId());

        assertThat(result).contains(saved);
    }

    @Test
    void deleteById_이후_findById는_빈_Optional을_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        Reservation saved = repository.save(reservation("브라운", RESERVATION_DATE, time, theme));

        repository.deleteById(saved.getId());

        Optional<Reservation> found = repository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteById는_예약만_삭제하고_같은_슬롯의_대기는_남긴다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        Reservation saved = repository.save(reservation("브라운", RESERVATION_DATE, time, theme));
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)",
                "민욱", RESERVATION_DATE, timeId, themeId, Timestamp.valueOf(WAITING_CREATED_AT)
        );

        repository.deleteById(saved.getId());

        Integer waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);
        assertThat(waitingCount).isOne();
    }

    @Test
    void findByName은_이름이_일치하는_예약만_반환한다() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(themeId, THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL_IMAGE_URL);
        repository.save(reservation("민욱", RESERVATION_DATE, time, theme));
        repository.save(reservation("티뉴", OTHER_RESERVATION_DATE, time, theme));

        assertThat(repository.findByReserver(member("민욱")))
                .hasSize(1)
                .first()
                .extracting(Reservation::getName)
                .isEqualTo("민욱");
    }
}
