package proj.concert.service.domain;

import proj.concert.common.types.Genre;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PERFORMERS")
public class Performer {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "IMAGE_NAME")
    private String imageName;
    @Column(name = "GENRE")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Column(name = "BLURB", length = 1024)
    private String blurb;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "PERFORMER_ID", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "CONCERT_ID", nullable = false))
    private Set<Concert> concerts = new HashSet<>();

    public Performer() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(Set<Concert> concerts) {
        this.concerts = concerts;
    }
}
