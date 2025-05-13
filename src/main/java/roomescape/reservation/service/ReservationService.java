package roomescape.reservation.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
public class ReservationService {

    private final DateTime dateTime;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            final DateTime dateTime,
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository, MemberRepository memberRepository
    ) {
        this.dateTime = dateTime;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId());
        Theme theme = themeRepository.findById(request.themeId());

        Optional<Member> findMember = memberRepository.findById(memberId);
        if (findMember.isEmpty()) {
            throw new IllegalArgumentException("존재 하지 않는 유저입니다.");
        }

        Reservation reservation = Reservation.createWithoutId(dateTime.now(), findMember.get(), request.date(), time,
                theme);

        if (reservationRepository.hasSameReservation(reservation)) {
            throw new IllegalArgumentException("이미 예약이 존재합니다.");
        }

        Long id = reservationRepository.save(reservation);

        return ReservationResponse.from(reservation.assignId(id));
    }

    public List<ReservationResponse> getReservations(ReservationConditionRequest request) {
        if (request.isEmpty()) {
            return reservationRepository.findAll().stream()
                    .map(ReservationResponse::from)
                    .toList();
        }
        return reservationRepository.findByMemberIdAndThemeIdAndDate(request.memberId(), request.themeId(),
                        request.dateFrom(), request.dateTo())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservationById(final Long id) {
        boolean isDeleted = reservationRepository.deleteById(id);
        validateExistIdToDelete(isDeleted);
    }

    private void validateExistIdToDelete(boolean isDeleted) {
        if (!isDeleted) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 예약입니다.");
        }
    }
}
