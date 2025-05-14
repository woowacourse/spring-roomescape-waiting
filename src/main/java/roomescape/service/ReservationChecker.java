package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.exception.InvalidRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Component
public class ReservationChecker {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationChecker(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation createReservationWithoutId(UserReservationRequest dto, Member member) {
        ReservationRequest request = new ReservationRequest(dto.date(), dto.timeId(), dto.themeId(), member.getId());
        return createReservationWithoutId(request);
    }

    public Reservation createReservationWithoutId(ReservationRequest dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        validateRequestDateTime(LocalDateTime.of(dto.date(), reservationTime.getStartAt()));

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 회원을 찾을 수 없습니다."));

        return dto.createWithoutId(reservationTime, theme, member);
    }

    private void validateRequestDateTime(LocalDateTime requestDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (requestDateTime.isBefore(currentDateTime) || requestDateTime.equals(currentDateTime)) {
            throw new InvalidRequestException("[ERROR] 현 시점 이후의 날짜와 시간을 선택해주세요.");
        }
    }
}
