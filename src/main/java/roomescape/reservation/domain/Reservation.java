package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import roomescape.member.domain.Member;

@Getter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_reservation_per_time",
                columnNames = {"roomEscapeInformation_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RoomEscapeInformation roomEscapeInformation;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Builder
    private Reservation(
            final Long id,
            @NonNull final RoomEscapeInformation roomEscapeInformation,
            @NonNull final Member member
    ) {
        this.id = id;
        this.roomEscapeInformation = roomEscapeInformation;
        this.member = member;
    }
}
