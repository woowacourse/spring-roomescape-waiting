package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.entity.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("인기테마 조회 테스트")
    @Test
    void findTopReservedThemesByDateRangeAndLimit() {
        setTopReservedThemes();
        LocalDate startDate = LocalDate.parse("2024-04-25");
        LocalDate endDate = LocalDate.parse("2024-05-30");

        List<Theme> themes = themeRepository.findTopReservedThemesByDateRangeAndLimit(startDate, endDate, 2);

        assertThat(themes).hasSize(2)
                .extracting("id", "name")
                .containsExactly(
                        tuple(12L, "hi12"),
                        tuple(11L, "hi11")
                );
    }

    private void setTopReservedThemes() {
        Member member = new Member("asd", "asd@email.com", "asd", Role.ADMIN);
        memberRepository.save(member);

        for (int hour = 10; hour <= 21; hour++) {
            ReservationTime reservationTime = new ReservationTime(LocalTime.of(hour, 0));
            reservationTimeRepository.save(reservationTime);
        }

        themeRepository.save(new Theme("hi1", "happy1", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(1L, "hi1", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));

        LocalDate date = LocalDate.of(2024, 4, 25);
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(1L, "hi1", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(8L, LocalTime.of(17,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(8L, LocalTime.of(17,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(9L, LocalTime.of(18,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L, LocalTime.of(16,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(8L, LocalTime.of(17,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(9L, LocalTime.of(18,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L, LocalTime.of(11,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L, LocalTime.of(12,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L, LocalTime.of(13,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L, LocalTime.of(14,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L, LocalTime.of(15,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L, LocalTime.of(16,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(8L, LocalTime.of(17,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(9L, LocalTime.of(18,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(11L,LocalTime.of(20,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(1L,LocalTime.of(10,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(2L,LocalTime.of(11,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(3L,LocalTime.of(12,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(4L,LocalTime.of(13,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(5L,LocalTime.of(14,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(6L,LocalTime.of(15,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(7L,LocalTime.of(16,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(8L,LocalTime.of(17,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(9L,LocalTime.of(18,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(11L,LocalTime.of(20,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
        reservationRepository.save(new Reservation(member, new Schedule(date, new ReservationTime(12L,LocalTime.of(21,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"))));
    }
}
