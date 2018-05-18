package echo.core.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "episode",
    indexes = {@Index(name = "idx_episode_exo",  columnList="exo", unique = true)})
//@Cacheable(false)
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EpisodeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "exo")
    private String exo;

    @Column(name = "title")
    private String title;

    @Column(name = "link")
    private String link;

    @Column(name = "description")
    private String description;

    @Column(name = "pub_date")
    private Timestamp pubDate;

    @Column(name = "guid")
    private String guid;

    @Column(name = "guid_is_permalink")
    private Boolean guidIsPermaLink;

    @Column(name = "image")
    private String image;

    @Column(name = "itunes_duration")
    private String itunesDuration;

    @Column(name = "itunes_subtitle")
    private String itunesSubtitle;

    @Column(name = "itunes_author")
    private String itunesAuthor;

    @Column(name = "itunes_summary")
    private String itunesSummary;

    @Column(name = "itunes_season")
    private Integer itunesSeason;

    @Column(name = "itunes_episode")
    private Integer itunesEpisode;

    @Column(name = "itunes_episode_type")
    private String itunesEpisodeType;

    @Column(name = "enclosure_url")
    private String enclosureUrl;

    @Column(name = "enclosure_length")
    private Long enclosureLength;

    @Column(name = "enclosure_type")
    private String enclosureType;

    @Column(name = "content_encoded")
    private String contentEncoded;

    @Column(name = "registration_timestamp")
    private Timestamp registrationTimestamp;

    @OneToMany(fetch=FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        mappedBy="episode")
    private Set<ChapterEntity> chapters = new LinkedHashSet();

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getPubDate() {
        return pubDate;
    }

    public void setPubDate(Timestamp pubDate) {
        this.pubDate = pubDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Boolean getGuidIsPermaLink() {
        return guidIsPermaLink;
    }

    public void setGuidIsPermaLink(Boolean guidIsPermaLink) {
        this.guidIsPermaLink = guidIsPermaLink;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getItunesDuration() {
        return itunesDuration;
    }

    public void setItunesDuration(String itunesDuration) {
        this.itunesDuration = itunesDuration;
    }

    public String getItunesSubtitle() {
        return itunesSubtitle;
    }

    public void setItunesSubtitle(String itunesSubtitle) {
        this.itunesSubtitle = itunesSubtitle;
    }

    public String getItunesAuthor() {
        return itunesAuthor;
    }

    public void setItunesAuthor(String itunesAuthor) {
        this.itunesAuthor = itunesAuthor;
    }

    public String getItunesSummary() {
        return itunesSummary;
    }

    public void setItunesSummary(String itunesSummary) {
        this.itunesSummary = itunesSummary;
    }

    public Integer getItunesSeason() {
        return itunesSeason;
    }

    public void setItunesSeason(Integer itunesSeason) {
        this.itunesSeason = itunesSeason;
    }

    public Integer getItunesEpisode() {
        return itunesEpisode;
    }

    public void setItunesEpisode(Integer itunesEpisode) {
        this.itunesEpisode = itunesEpisode;
    }

    public String getItunesEpisodeType() {
        return itunesEpisodeType;
    }

    public void setItunesEpisodeType(String itunesEpisodeType) {
        this.itunesEpisodeType = itunesEpisodeType;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public Long getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Long enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    public String getContentEncoded() {
        return contentEncoded;
    }

    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
    }

    public Timestamp getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Timestamp registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public Set<ChapterEntity> getChapters() {
        return chapters;
    }

    public void setChapters(Set<ChapterEntity> chapters) {
        this.chapters = chapters;
    }

    public PodcastEntity getPodcast() {
        return podcast;
    }

    public void setPodcast(PodcastEntity podcast) {
        this.podcast = podcast;
    }

    public void addChapter(ChapterEntity chapter) {
        this.chapters.add(chapter);
        chapter.setEpisode(this);
    }

    public void removeChapter(ChapterEntity chapter) {
        this.chapters.remove(chapter);
        chapter.setEpisode(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EpisodeEntity episode = (EpisodeEntity) o;
        if(episode.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, episode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "EpisodeEntity{" +
            "id=" + id +
            ", exo='" + exo + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", guid='" + guid + '\'' +
            ", guidIsPermaLink=" + guidIsPermaLink +
            ", image='" + image + '\'' +
            ", itunesDuration='" + itunesDuration + '\'' +
            ", itunesSubtitle='" + itunesSubtitle + '\'' +
            ", itunesAuthor='" + itunesAuthor + '\'' +
            ", itunesSummary='" + itunesSummary + '\'' +
            ", itunesSeason=" + itunesSeason +
            ", itunesEpisode=" + itunesEpisode +
            ", itunesEpisodeType='" + itunesEpisodeType + '\'' +
            ", enclosureUrl='" + enclosureUrl + '\'' +
            ", enclosureLength=" + enclosureLength +
            ", enclosureType='" + enclosureType + '\'' +
            ", registrationTimestamp=" + registrationTimestamp +
            ", contentEncoded='" + contentEncoded + '\'' +
            '}';
    }
}
