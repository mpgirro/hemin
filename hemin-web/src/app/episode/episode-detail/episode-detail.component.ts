import {Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Episode } from '../shared/episode.model';
import { EpisodeService } from '../shared/episode.service';
import { DomainService } from '../../domain.service';
import {Chapter} from '../shared/chapter.model';
import {ImageService} from '../../image.service';
import {Image} from '../../image.model';

@Component({
  selector: 'app-episode-detail',
  templateUrl: './episode-detail.component.html',
  styleUrls: ['./episode-detail.component.css']
})
export class EpisodeDetailComponent implements OnInit {

  @Input() episode: Episode;
  image: Image;
  chapters: Chapter[];

  HIGHLIGHT_COLOR = '#007bff';

  constructor(private route: ActivatedRoute,
              private episodeService: EpisodeService,
              private imageService: ImageService,
              private domainService: DomainService,
              private location: Location) { }

  ngOnInit() {
    this.getEpisode();
  }

  // TODO unused
  initPlyrPlayer(): void {
    const plyrJS = 'plyr.setup("#plyr-audio");';
    const el = document.createElement('script');
    el.appendChild(document.createTextNode(plyrJS));
    document.body.appendChild(el);
  }

  initPodlovePlayer(): void {
    console.log(this.episode);
    const podlovePlayerJS = `podlovePlayer('#podlove-player',{
      "duration" : "${this.episode.itunes.duration}",
      "audio" : [{
        "url" : "${this.episode.enclosure.url}",
        "size" : ${this.episode.enclosure.length},
        "mimeType" :"${this.episode.enclosure.typ}"
      }],
      "chapters" : ${JSON.stringify(this.chapters)},
      "theme" : {
        "main" : "#ffffff",
        "highlight" : "${this.HIGHLIGHT_COLOR}"
      },
      "tabs": {
        "chapters" : true
      },
      "visibleComponents": [
        "tabChapters",
        "tabAudio",
        "progressbar",
        "controlSteppers",
        "controlChapters"
      ]});`;

    const el = document.createElement('script');
    el.appendChild(document.createTextNode(podlovePlayerJS));
    document.body.appendChild(el);
  }

  getEpisode(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.episodeService
      .get(id)
      .subscribe(episode => {
        this.episode = episode;
        this.imageService
          .get(episode.image)
          .subscribe(image => {
            this.image = image;
            console.log(image);
          });
        this.episodeService
          .getChapters(id)
          .subscribe(chapters => {
            this.chapters = chapters.results;
            this.chapters.sort((a: Chapter, b: Chapter) => {
              if (a.start < b.start) {
                return -1;
              } else if (a.start > b.start) {
                return 1;
              } else {
                return 0;
              }
            });
          this.initPodlovePlayer();
        });
      });
  }

  goBack(): void {
    this.location.back();
  }

}
