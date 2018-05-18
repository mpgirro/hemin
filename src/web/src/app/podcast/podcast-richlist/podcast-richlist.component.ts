import {Component, Input, OnInit} from '@angular/core';
import {Podcast} from '../shared/podcast.model';

@Component({
  selector: 'app-podcast-richlist',
  templateUrl: './podcast-richlist.component.html',
  styleUrls: ['./podcast-richlist.component.css']
})
export class PodcastRichlistComponent implements OnInit {

  @Input() podcasts: Array<Podcast>;

  constructor() { }

  ngOnInit() {
  }

}
