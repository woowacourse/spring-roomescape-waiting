package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation addReservation(CreateReservationRequest request, LoginMemberRequest loginMemberRequest) {
        return createReservation(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId());
    }

    public Reservation addReservationByAdmin(AdminCreateReservationRequest request) {
        return createReservation(request.memberId(), request.themeId(), request.date(), request.timeId());
    }

    private Reservation createReservation(long memberId, long themeId, LocalDate date, long timeId) {
        Member member = memberRepository.findFetchById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        return member.reserve(date, reservationTime, theme);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약 입니다."));
        reservation.cancel();
    }

    public List<Reservation> findAllReservationByMember(final Long memberId) {
        Member member = memberRepository.findFetchById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 입니다."));
        return member.getReservations();
    }

    public List<Reservation> findAllByFilter(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationRepository.findAllByFilter(memberId, themeId, dateFrom, dateTo);
    }
}
