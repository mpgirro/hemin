import {EpisodeEnclosure} from './episode-enclosure.model';
import {EpisodeItunes} from './episode-itunes.model';

export class Episode {
  docType: string;
  id: string;
  podcastId: string;
  podcastTitle: string;
  title: string;
  link: string;
  description: string;
  pubDate: string;
  image: string;
  contentEncoded: string;
  enclosure: EpisodeEnclosure;
  itunes: EpisodeItunes;
}
