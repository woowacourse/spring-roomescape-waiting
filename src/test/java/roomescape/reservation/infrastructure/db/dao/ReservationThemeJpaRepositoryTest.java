package roomescape.reservation.infrastructure.db.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.ReservationTestFixture.createConfirmedReservation;
import static roomescape.ReservationTestFixture.createTheme;
import static roomescape.ReservationTestFixture.createUser;
import static roomescape.ReservationTestFixture.getReservationTimeFixture;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.infrastructure.db.MemberJpaRepository;
import roomescape.member.model.Member;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.support.RepositoryTestSupport;

class ReservationThemeJpaRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReservationThemeJpaRepository reservationThemeJpaRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    private ReservationTime savedTime;
    private Member savedMember;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        savedTime = reservationTimeJpaRepository.save(getReservationTimeFixture());
        savedMember = memberJpaRepository.save(createUser("테스트", "test@test.com", "password"));
        testDate = LocalDate.now().plusDays(5);
    }

    @Test
    @DisplayName("예약 횟수가 많은 순서대로 테마가 정렬되어 반환된다")
    void getOrderByThemeBookedCountWithLimit_ordered_by_booking_count() {
        // given
        ReservationTheme theme1 = reservationThemeJpaRepository.save(createTheme("인기테마", "가장 인기", "popular.jpg"));
        ReservationTheme theme2 = reservationThemeJpaRepository.save(createTheme("보통테마", "보통 인기", "normal.jpg"));
        ReservationTheme theme3 = reservationThemeJpaRepository.save(createTheme("비인기테마", "비인기", "unpopular.jpg"));

        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme1, savedMember));
        reservationJpaRepository.save(createConfirmedReservation(testDate.plusDays(1), savedTime, theme1, savedMember));
        reservationJpaRepository.save(createConfirmedReservation(testDate.plusDays(2), savedTime, theme1, savedMember));

        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme2, savedMember));
        reservationJpaRepository.save(createConfirmedReservation(testDate.plusDays(1), savedTime, theme2, savedMember));

        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme3, savedMember));

        // when
        List<ReservationTheme> result = reservationThemeJpaRepository.getOrderByThemeBookedCountWithLimit(10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.get(0).getId()).isEqualTo(theme1.getId());
            softly.assertThat(result.get(1).getId()).isEqualTo(theme2.getId());
            softly.assertThat(result.get(2).getId()).isEqualTo(theme3.getId());
        });
    }

    @Test
    @DisplayName("LIMIT 개수만큼만 결과가 반환된다")
    void getOrderByThemeBookedCountWithLimit_respects_limit() {
        // given
        ReservationTheme theme1 = reservationThemeJpaRepository.save(createTheme("테마1", "설명1", "1.jpg"));
        ReservationTheme theme2 = reservationThemeJpaRepository.save(createTheme("테마2", "설명2", "2.jpg"));
        ReservationTheme theme3 = reservationThemeJpaRepository.save(createTheme("테마3", "설명3", "3.jpg"));

        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme1, savedMember));
        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme2, savedMember));
        reservationJpaRepository.save(createConfirmedReservation(testDate, savedTime, theme3, savedMember));

        int limit = 2;
        // when
        List<ReservationTheme> result = reservationThemeJpaRepository.getOrderByThemeBookedCountWithLimit(limit);

        // then
        assertThat(result).hasSize(limit);
    }
}
