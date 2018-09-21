package exo.engine.domain.entity;

import exo.engine.domain.FeedStatus;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "feed",
    indexes = {@Index(name = "idx_feed_exo",  columnList="exo", unique = true)})
//@Cacheable(false)
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FeedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "exo")
    private String exo;

    @Column(name = "url")
    private String url;

    @Column(name = "last_checked")
    private Timestamp lastChecked;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_status")
    private FeedStatus lastStatus;

    @Column(name = "registration_timestamp")
    private Timestamp registrationTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="podcast_id")
    private PodcastEntity podcast;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExo() {
        return exo;
    }

    public void setExo(String exo) {
        this.exo = exo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Timestamp lastChecked) {
        this.lastChecked = lastChecked;
    }

    public FeedStatus getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(FeedStatus lastStatus) {
        this.lastStatus = lastStatus;
    }

    public Timestamp getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Timestamp registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public PodcastEntity getPodcast() {
        return podcast;
    }

    public void setPodcast(PodcastEntity podcast) {
        this.podcast = podcast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeedEntity feed = (FeedEntity) o;
        if(feed.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, feed.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FeedEntity{" +
            "id=" + id +
            ", exo='" + exo + '\'' +
            ", url='" + url + '\'' +
            ", lastChecked=" + lastChecked +
            ", lastStatus=" + lastStatus +
            ", registrationTimestamp=" + registrationTimestamp +
            '}';
    }
}
