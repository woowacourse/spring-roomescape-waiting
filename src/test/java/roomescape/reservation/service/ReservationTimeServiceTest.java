package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.global.exception.model.AssociatedDataExistsException;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ReservationTimeService.class)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
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

    @Test
    @DisplayName("중복된 예약 시간을 등록하는 경우 예외가 발생한다.")
    void duplicateTimeFail() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 30)));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.addTime(new ReservationTimeRequest(LocalTime.of(12, 30))))
                .isInstanceOf(DataDuplicateException.class);
    }

    @Test
    @DisplayName("삭제하려는 시간에 예약이 존재하면 예외를 발생한다.")
    void usingTimeDeleteFail() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        // when
        reservationRepository.save(new ReservationDetail(LocalDate.now().plusDays(1L), reservationTime, theme));

        // then
        assertThatThrownBy(() -> reservationTimeService.removeTimeById(reservationTime.getId()))
                .isInstanceOf(AssociatedDataExistsException.class);
    }
}
