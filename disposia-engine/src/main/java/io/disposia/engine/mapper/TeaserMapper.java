package io.disposia.engine.mapper;

import io.disposia.engine.olddomain.*;
import io.disposia.engine.olddomain.ImmutableOldEpisode;
import io.disposia.engine.olddomain.OldEpisode;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper(uses = {UrlMapper.class, DateMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    default OldPodcast asTeaser(OldPodcast podcast) {
        return Optional
            .ofNullable(podcast)
            .map(p -> ImmutableOldPodcast.builder()
                //.setExo(p.getExo())
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

    default OldEpisode asTeaser(OldEpisode episode) {
        return Optional
            .ofNullable(episode)
            .map(e -> ImmutableOldEpisode.builder()
                //.setExo(e.getExo())
                .setTitle(e.getTitle())
                .setPubDate(e.getPubDate())
                .setDescription(e.getDescription())
                .setImage(e.getImage())
                .setItunesDuration(e.getItunesDuration())
                .create())
            .orElse(null);
    }

}
