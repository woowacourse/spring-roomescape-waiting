package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class CreateReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public CreateReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Long addReservation(ReservationRequest request) {
        Reservation reservation = convertReservation(request);
        validateReservationNotDuplicate(reservation);

        return reservationRepository.save(reservation).getId();
    }

    private Reservation convertReservation(ReservationRequest request) {
        ReservationTime reservationTime = timeRepository.getById(request.timeId());
        Theme theme = themeRepository.getById(request.themeId());
        Member member = memberRepository.getById(request.memberId());

        return request.toEntity(reservationTime, theme, member);
    }

    private void validateReservationNotDuplicate(Reservation reservation) {
        boolean alreadyBooked = reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getThemeId()
        );

        if (alreadyBooked) {
            throw new IllegalArgumentException(
                    "[ERROR] 해당 시간에 동일한 테마가 예약되어있어 예약이 불가능합니다.",
                    new Throwable("생성 예약 정보 : " + reservation)
            );
        }
    }
}
