package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.response.BookResponse;
import roomescape.fixture.MemberFixtures;
import roomescape.fixture.ReservationFixtures;
import roomescape.fixture.ThemeFixtures;
import roomescape.fixture.TimeSlotFixtures;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@Sql(value = "classpath:test-db-clean.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class BookServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private BookService bookService;

    @DisplayName("이미 예약이 되어있다면 true를 반환한다.")
    @Test
    void findAvailableBookList() {
        //given
        LocalDate curDate = LocalDate.now();
        Member member = memberRepository.save(MemberFixtures.createAdminMember("daon", "test@email.com"));
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now()));
        timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now().plusHours(1)));
        timeSlotRepository.save(TimeSlotFixtures.createReservationTime(LocalTime.now().plusHours(2)));
        Theme theme = themeRepository.save(ThemeFixtures.createDefaultTheme());
        reservationRepository.save(ReservationFixtures.createBookingReservation(member, curDate, timeSlot, theme));

        //when
        List<BookResponse> result = bookService.findAvailableBookList(curDate, theme.getId());
        BookResponse firstResult = result.get(0);
        BookResponse secondResult = result.get(1);

        //then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(firstResult.alreadyBooked()).isTrue(),
                () -> assertThat(secondResult.alreadyBooked()).isFalse()
        );
    }
}
