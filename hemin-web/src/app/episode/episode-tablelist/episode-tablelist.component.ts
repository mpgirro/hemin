import {Component, Input, OnChanges} from '@angular/core';
import { Episode } from '../shared/episode.model';

@Component({
  selector: 'app-episode-tablelist',
  templateUrl: './episode-tablelist.component.html',
  styleUrls: ['./episode-tablelist.component.css']
})
export class EpisodeTablelistComponent implements OnChanges {

  @Input() episodes: Array<Episode>;
  isCollapsed: boolean[];

  constructor() { }

  ngOnChanges() {
    this.isCollapsed = new Array(this.episodes.length); // will be default init with false values
  }

}
