package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidIdException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDateTime;

@Service
public class ReservationRegisterService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationRegisterService(final ReservationTimeRepository reservationTimeRepository,
                                      final ReservationRepository reservationRepository,
                                      final ThemeRepository themeRepository, final MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse register(final ReservationRequest reservationRequest) {
        validateDuplicatedReservation(reservationRequest);
        final ReservationTime reservationTime = getReservationTime(reservationRequest);
        final Theme theme = getTheme(reservationRequest);
        final Member member = getMember(reservationRequest);
        validatePastDate(reservationRequest, reservationTime);
        final Reservation reservation = createReservation(reservationRequest, member, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private Reservation createReservation(final ReservationRequest reservationRequest, final Member member, final ReservationTime reservationTime, final Theme theme) {
        if (reservationRepository.existsByDateAndTime_IdAndTheme_Id(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            return Reservation.waiting(member, reservationRequest.date(), reservationTime, theme);
        }
        return Reservation.reserved(member, reservationRequest.date(), reservationTime, theme);
    }

    private ReservationTime getReservationTime(final ReservationRequest reservationRequest) {
        return reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new InvalidIdException("timeId", reservationRequest.timeId()));
    }

    private Theme getTheme(final ReservationRequest reservationRequest) {
        return themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new InvalidIdException("themeId", reservationRequest.themeId()));
    }

    private Member getMember(final ReservationRequest reservationRequest) {
        return memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new InvalidIdException("memberId", reservationRequest.memberId()));
    }

    private void validateDuplicatedReservation(final ReservationRequest reservationRequest) {
        if (reservationRepository.existsByDateAndTime_IdAndTheme_IdAndMember_Id(reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                reservationRequest.memberId())) {
            throw new ReservationFailException("이미 예약이 등록되어 있습니다.");
        }
    }

    private void validatePastDate(final ReservationRequest reservationRequest, final ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationRequest.date(), reservationTime.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ReservationFailException("지나간 날짜와 시간으로 예약할 수 없습니다.");
        }
    }
}
