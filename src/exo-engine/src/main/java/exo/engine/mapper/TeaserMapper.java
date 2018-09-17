package exo.engine.mapper;

import exo.engine.domain.dto.Episode;
import exo.engine.domain.dto.ImmutableEpisode;
import exo.engine.domain.dto.ImmutablePodcast;
import exo.engine.domain.dto.Podcast;
import exo.engine.domain.entity.EpisodeEntity;
import exo.engine.domain.entity.PodcastEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Mapper(uses = {UrlMapper.class, DateMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default Podcast asTeaser(Podcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> ImmutablePodcast.builder()
                .setExo(p.getExo())
                .setTitle(p.getTitle())
                .setImage(p.getImage())
                .setLanguage(p.getLanguage())
                .setGenerator(p.getGenerator())
                .setCopyright(p.getCopyright())
                .setEpisodeCount(p.getEpisodeCount())
                .setItunesAuthor(p.getItunesAuthor())
                .setItunesExplicit(p.getItunesExplicit())
                .setItunesBlock(p.getItunesBlock())
                .setRegistrationComplete(p.getRegistrationComplete())
                .setRegistrationTimestamp(p.getRegistrationTimestamp())
                .create())
            .orElse(null);
    }

    default Podcast asTeaser(PodcastEntity podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> ImmutablePodcast.builder()
                .setExo(p.getExo())
                .setTitle(p.getTitle())
                .setImage(p.getImage())
                .setLanguage(p.getLanguage())
                .setGenerator(p.getGenerator())
                .setCopyright(p.getCopyright())
                .setEpisodeCount(p.getEpisodeCount())
                .setItunesAuthor(p.getItunesAuthor())
                .setItunesExplicit(p.getItunesExplicit())
                .setItunesBlock(p.getItunesBlock())
                .setRegistrationComplete(p.getRegistrationComplete())
                .setRegistrationTimestamp(DateMapper.INSTANCE.asLocalDateTime(p.getRegistrationTimestamp()))
                .create())
            .orElse(null);
    }

    default Episode asTeaser(Episode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> ImmutableEpisode.builder()
                .setExo(e.getExo())
                .setTitle(e.getTitle())
                .setPubDate(e.getPubDate())
                .setDescription(e.getDescription())
                .setImage(e.getImage())
                .setItunesDuration(e.getItunesDuration())
                .create())
            .orElse(null);
    }

    default Episode asTeaser(EpisodeEntity episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> ImmutableEpisode.builder()
                .setExo(e.getExo())
                .setTitle(e.getTitle())
                .setPubDate(DateMapper.INSTANCE.asLocalDateTime(e.getPubDate()))
                .setDescription(e.getDescription())
                .setImage(e.getImage())
                .setItunesDuration(e.getItunesDuration())
                .create())
            .orElse(null);
    }

}
