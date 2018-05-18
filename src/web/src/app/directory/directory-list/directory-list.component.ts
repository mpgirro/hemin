import {Component, Input, OnInit} from '@angular/core';
import {Podcast} from '../../podcast/shared/podcast.model';
import {ActivatedRoute} from '@angular/router';
import {PodcastService} from '../../podcast/shared/podcast.service';
import {Location} from '@angular/common';

@Component({
  selector: 'app-directory-list',
  templateUrl: './directory-list.component.html',
  styleUrls: ['./directory-list.component.css']
})
export class DirectoryListComponent implements OnInit {

  @Input() podcasts: Array<Podcast>;

  DEFAULT_PAGE = 1;
  DEFAULT_SIZE = 500;

  currPage: number;
  currSize: number;

  constructor(private route: ActivatedRoute,
              private podcastService: PodcastService,
              private location: Location) { }

  ngOnInit() {
    const p = this.route.snapshot.queryParamMap.get('p');
    const s = this.route.snapshot.queryParamMap.get('s');
    this.currPage = (p) ? Number(p) : this.DEFAULT_PAGE;
    this.currSize = (s) ? Number(s) : this.DEFAULT_SIZE;
    this.getAllPodcasts();
  }

  getAllPodcasts(): void {
    this.podcastService.getAll(this.currPage, this.currSize)
      .subscribe(podcasts => {

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
