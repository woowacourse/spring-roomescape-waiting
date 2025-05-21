package roomescape.application.reservation.command;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateReservationCommand;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.ReservationException;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.infrastructure.error.exception.ThemeException;

@Service
@Transactional
public class CreateReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public CreateReservationService(ReservationRepository reservationRepository,
                                    ReservationTimeRepository reservationTimeRepository,
                                    ThemeRepository themeRepository,
                                    MemberRepository memberRepository,
                                    Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Long reserve(CreateReservationCommand command) {
        Member member = getMember(command.memberId());
        ReservationTime time = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        validateDuplicateReservation(command.date(), time, theme);
        Reservation reservation = new Reservation(member, command.date(), time, theme);
        reservation.validateReservable(LocalDateTime.now(clock));
        Reservation savedReservation = reservationRepository.save(reservation);
        return savedReservation.getId();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
    }

    private ReservationTime getTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException("존재하지 않는 예약 시간입니다."));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeException("존재하지 않는 테마입니다."));
    }

    private void validateDuplicateReservation(LocalDate date, ReservationTime time, Theme theme) {
        boolean duplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId());
        if (duplicated) {
            throw new ReservationException("날짜와 시간이 중복된 예약이 존재합니다.");
        }
    }
}
