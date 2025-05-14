package roomescape.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.dto.request.ReservationSearchFilter;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@JdbcTest
@Import(ReservationJdbcDao.class)
public class ReservationJdbcDaoTest {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER_WITHOUT_JOIN = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    null,
                    null,
                    null
            );

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long savedId;
    private LocalDate date;
    private Reservation savedReservation;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        Theme tempTheme = new Theme("새로운 테마", "새로운 테마입니다", "Image");
        Long themeId = saveNewTheme(tempTheme);
        this.theme = new Theme(themeId, tempTheme.getName(), tempTheme.getDescription(), tempTheme.getThumbnail());

        ReservationTime tempReservationTime = new ReservationTime(LocalTime.of(12, 30));
        Long reservationTimeId = saveNewReservationTime(tempReservationTime);
        this.reservationTime = new ReservationTime(reservationTimeId, tempReservationTime.getStartAt());

        this.date = LocalDate.now().plusDays(1);

        Member tempMember = new Member("히로", "email@gmail.com", "password", Role.ADMIN);
        Long memberId = saveNewMember(tempMember);
        this.member = new Member(memberId, tempMember.getName(), tempMember.getEmail(), tempMember.getPassword(),
                tempMember.getRole());

        this.savedReservation = new Reservation(date, reservationTime, theme, member, LocalDate.now());
        this.savedId = saveNewReservation(savedReservation);

    }

    @Test
    @DisplayName("예약을 저장한다")
    void test2() {
        // given
        Reservation reservation = new Reservation(date, reservationTime, theme, member, LocalDate.now());

        // when
        Long savedId = reservationDao.saveReservation(reservation);

        // then
        List<Reservation> foundReservations = jdbcTemplate.query(
                "SELECT * FROM reservation where id = ?",
                RESERVATION_ROW_MAPPER_WITHOUT_JOIN,
                savedId
        );

        assertAll(
                () -> assertThat(foundReservations).hasSize(1),
                () -> assertThat(foundReservations.getFirst().getDate()).isEqualTo(date)
        );

    }

    @Test
    @DisplayName("날짜와 시각을 이용해 테마를 찾는다")
    void test3() {
        // when
        Optional<Reservation> foundReservation = reservationDao.findByDateAndTime(
                new Reservation(this.date, this.reservationTime, this.theme, member, LocalDate.now())
        );

        // then
        assertAll(
                () -> assertThat(foundReservation.isPresent()).isTrue(),
                () -> assertThat(foundReservation.get().getDate()).isEqualTo(this.date)
        );
    }

    @Test
    @DisplayName("id 를 이용해 예약을 삭제한다")
    void test4() {
        // when
        reservationDao.deleteById(savedId);

        // then
        List<Reservation> foundReservations = jdbcTemplate.query(
                "SELECT * FROM reservation where id = ?",
                RESERVATION_ROW_MAPPER_WITHOUT_JOIN,
                savedId
        );

        assertThat(foundReservations).hasSize(0);
    }

    @Test
    @DisplayName("예약을 조회할 때 테마에 해당하는 결과만 조회한다")
    void test5() {
        // given
        saveNewReservation(new Reservation(
                LocalDate.now().plusDays(1),
                this.reservationTime,
                this.theme,
                this.member,
                LocalDate.now()
        ));

        // when
        List<Reservation> result = reservationDao.findAll(
                new ReservationSearchFilter(theme.getId(), null, null, null));

        // then
        List<Long> themeIds = result.stream()
                .map(Reservation::getTheme)
                .map(Theme::getId)
                .toList();

        assertThat(themeIds).containsOnly(theme.getId());
    }

    @Test
    @DisplayName("예약을 조회할 때 멤버에 해당하는 결과만 조회한다")
    void test6() {
        // given
        saveNewReservation(new Reservation(
                LocalDate.now().plusDays(1),
                this.reservationTime,
                theme,
                this.member,
                LocalDate.now()
        ));

        // when
        List<Reservation> result = reservationDao.findAll(
                new ReservationSearchFilter(null, member.getId(), null, null));

        // then
        List<Long> memberIds = result.stream()
                .map(Reservation::getMember)
                .map(Member::getId)
                .toList();

        assertThat(memberIds).containsOnly(member.getId());
    }

    @Test
    @DisplayName("예약을 조회할 때 기간에 해당하는 결과만 조회한다")
    void test7() {
        // given
        LocalDate searchDate = LocalDate.now().plusDays(2);

        saveNewReservation(new Reservation(
                searchDate.plusDays(1),
                this.reservationTime,
                theme,
                this.member,
                LocalDate.now()
        ));

        saveNewReservation(new Reservation(
                searchDate.minusDays(1),
                this.reservationTime,
                theme,
                this.member,
                LocalDate.now()
        ));

        // when
        List<Reservation> result = reservationDao.findAll(
                new ReservationSearchFilter(null, null, searchDate, searchDate.plusDays(1)));

        // then
        List<LocalDate> dates = result.stream()
                .map(Reservation::getDate)
                .toList();

        assertThat(dates)
                .allMatch(localDate -> !localDate.isBefore(searchDate));
    }

    private Long saveNewReservation(Reservation reservation) {
        String sql = "INSERT INTO reservation (date, time_id, theme_id, member_id) values (?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(reservation.getDate()));
            ps.setLong(2, reservation.getTime().getId());
            ps.setLong(3, reservation.getTheme().getId());
            ps.setLong(4, reservation.getMember().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    private Long saveNewTheme(Theme theme) {
        String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private Long saveNewReservationTime(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time (start_at) values(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, java.sql.Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private Long saveNewMember(Member member) {
        String sql = "INSERT INTO member"
                + " (name, email,password, role) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPassword());
            ps.setString(4, member.getRole().getValue());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
