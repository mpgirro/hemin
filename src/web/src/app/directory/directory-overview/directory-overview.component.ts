import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Location } from '@angular/common';

import { Podcast } from '../../podcast/shared/podcast.model';
import {PodcastService} from '../../podcast/shared/podcast.service';
import { of } from 'rxjs/observable/of';
import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'app-directory',
  templateUrl: './directory-overview.component.html',
  styleUrls: ['./directory-overview.component.css']
})
export class DirectoryOverviewComponent implements OnInit {

  @Input() podcasts: Array<Podcast>;

  DEFAULT_PAGE = 1;
  DEFAULT_SIZE = 36;

  currPage: number;
  currSize: number;

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private location: Location) { }

  ngOnInit() {
    this.route.paramMap
      .switchMap((params: ParamMap) => {
        const p = params.get('p');
        const s = params.get('s');
        this.currPage = (p) ? Number(p) : this.DEFAULT_PAGE;
        this.currSize = (s) ? Number(s) : this.DEFAULT_SIZE;
        return this.podcastService.getAll(this.currPage, this.currSize);
      }).subscribe(podcasts => {

      // reverse sort by date
      podcasts.results.sort((a: Podcast, b: Podcast) => {
        if (a.title < b.title) {
          return -1;
        } else if (a.title > b.title) {
          return 1;
        } else {
          return 0;
        }
      });

      this.podcasts = podcasts.results;
    });
  }

}
