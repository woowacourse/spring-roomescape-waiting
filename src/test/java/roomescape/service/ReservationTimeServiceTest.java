package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DatabaseCleaner;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundException;
import roomescape.service.dto.param.CreateReservationTimeParam;
import roomescape.service.dto.result.AvailableReservationTimeResult;
import roomescape.service.dto.result.ReservationTimeResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    void 예약_시간을_생성할_수_있다() {
        //given & when
        ReservationTimeResult reservationTimeResult = reservationTimeService.create(new CreateReservationTimeParam(LocalTime.of(12, 1)));
        Optional<ReservationTime> result = reservationTimeRepository.findById(reservationTimeResult.id());

        //then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getStartAt()).isEqualTo(reservationTimeResult.startAt())
        );
    }

    @Test
    void id에_해당하는_예약_시간을_찾을_수_있다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());

        //when
        ReservationTimeResult reservationTimeResult = reservationTimeService.getById(reservationTime.getId());

        //then
        assertThat(reservationTimeResult).isEqualTo(new ReservationTimeResult(reservationTimeResult.id(), reservationTimeResult.startAt()));
    }

    @Test
    void id에_해당하는_예약_시간이_없는경우_예외가_발생한다() {
        //given & when & then
        assertThatThrownBy(() -> reservationTimeService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        ReservationTime reservationTime1 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(12, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(13, 0)));

        //when
        List<ReservationTimeResult> reservationTimeResults = reservationTimeService.getAll();

        //then
        assertAll(
                () -> assertThat(reservationTimeResults).hasSize(2),
                () -> assertThat(reservationTimeResults).isEqualTo(List.of(
                        new ReservationTimeResult(reservationTime1.getId(), reservationTime1.getStartAt()),
                        new ReservationTimeResult(reservationTime2.getId(), reservationTime2.getStartAt())
                        ))
                );
    }

    @Test
    void id에_해당하는_예약_시간을_삭제한다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());

        //when
        Long id = reservationTime.getId();
        reservationTimeService.deleteById(id);

        //then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    void time_id를_사용하는_예약이_존재하면_예외를_던진다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = TestFixture.createNewReservation(member, DEFAULT_DATE, reservationTime, theme);
        reservationRepository.save(reservation);

        //when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessage("해당 예약 시간에 예약이 존재합니다.");
    }

    @ParameterizedTest
    @CsvSource({"12:00", "22:00"})
    void 예약_시간은_12시_부터_22시_까지_가능하다_성공(LocalTime time) {
        assertThatCode(() -> reservationTimeService.create(new CreateReservationTimeParam(time)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"11:59", "22:01"})
    void 예약_시간은_12시_부터_22시_까지_가능하다_실패(LocalTime time) {
        assertThatThrownBy(() -> reservationTimeService.create(new CreateReservationTimeParam(time)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간은 예약 가능 시간이 아닙니다.");
    }

    @Test
    void 테마와_날짜에_해당하는_예약_가능한_시간을_조회할_수_있다() {
        // given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime time1 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(12, 0)));
        ReservationTime time2 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(14, 0)));
        ReservationTime time3 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(16, 0)));
        
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        reservationRepository.save(TestFixture.createNewReservation(member, DEFAULT_DATE, time2, theme));

        // when
        List<AvailableReservationTimeResult> results = reservationTimeService.getAvailableTimesByThemeIdAndDate(
                theme.getId(), DEFAULT_DATE);

        // then
        assertAll(
                () -> assertThat(results).hasSize(3),
                () -> assertThat(results).extracting("startAt")
                        .containsExactly(LocalTime.of(12, 0), LocalTime.of(14, 0), LocalTime.of(16, 0))
        );
    }

    @Test
    void 예약이_없는_날짜의_모든_시간은_예약_가능_상태이다() {
        // given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime time1 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(12, 0)));
        ReservationTime time2 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(14, 0)));
        
        LocalDate futureDate = DEFAULT_DATE.plusDays(7);

        // when
        List<AvailableReservationTimeResult> results = reservationTimeService.getAvailableTimesByThemeIdAndDate(
                theme.getId(), futureDate);

        // then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results).extracting("booked")
                        .containsOnly(false)
        );
    }

    @Test
    void 예약이_있는_날짜_정보를_같이_반환한다() {
        // given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime time1 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(12, 0)));
        ReservationTime time2 = reservationTimeRepository.save(TestFixture.createTimeFrom(LocalTime.of(14, 0)));
        
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        reservationRepository.save(TestFixture.createNewReservation(member, DEFAULT_DATE, time1, theme));

        // when
        List<AvailableReservationTimeResult> results = reservationTimeService.getAvailableTimesByThemeIdAndDate(
                theme.getId(), DEFAULT_DATE);

        // then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results).extracting("booked")
                        .containsExactly(true, false)
        );
    }
}
