package roomescape.business.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import roomescape.business.model.vo.Id;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
@Table(name = "waiting")
public class Waiting {
    @EmbeddedId
    private final Id id;
    @ManyToOne
    private User user;

    protected Waiting() {
        id = Id.issue();
    }
}
