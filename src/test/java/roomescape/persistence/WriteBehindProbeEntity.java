package roomescape.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "write_behind_probe")
public class WriteBehindProbeEntity {

    @Id
    private Long id;

    private String name;

    protected WriteBehindProbeEntity() {
    }

    public WriteBehindProbeEntity(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }
}
