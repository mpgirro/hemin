import {Component, Input, OnInit} from '@angular/core';
import { Episode } from '../shared/episode.model';
import {ImageService} from '../../image.service';
import {Image} from '../../image.model';
import {DomSanitizer} from '@angular/platform-browser';

@Component({
  selector: 'app-episode-teaser',
  templateUrl: './episode-teaser.component.html',
  styleUrls: ['./episode-teaser.component.css']
})
export class EpisodeTeaserComponent implements OnInit {

  @Input() episode: Episode;
  image: Image;

  constructor(private imageService: ImageService,
              private DomSanitizationService: DomSanitizer) { }

  ngOnInit() {
    this.imageService
      .get(this.episode.image)
      .subscribe(image => {
        this.image = image;
      });
  }

}
