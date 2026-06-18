package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "theme")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnailImageUrl;

    protected Theme() {
    }

    public Theme(
        Long id,
        String name,
        String description,
        String thumbnailImageUrl
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public Theme(
        String name,
        String description,
        String thumbnailImageUrl
    ) {
        this(null, name, description, thumbnailImageUrl);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
