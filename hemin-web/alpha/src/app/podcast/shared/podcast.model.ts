import {PodcastItunes} from './podcast-itunes.model';

export class Podcast {
  docType: string;
  id: string;
  title: string;
  link: string;
  description: string;
  pubDate: string;
  lastBuildDate: string;
  image: string;
  language: string;
  generator: string;
  copyright: string;
  itunes: PodcastItunes;
}
