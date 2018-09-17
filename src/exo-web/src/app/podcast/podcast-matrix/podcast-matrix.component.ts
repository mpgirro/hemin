import {Component, Input, OnInit} from '@angular/core';
import {Podcast} from '../shared/podcast.model';

@Component({
  selector: 'app-podcast-matrix',
  templateUrl: './podcast-matrix.component.html',
  styleUrls: ['./podcast-matrix.component.css']
})
export class PodcastMatrixComponent implements OnInit {

  @Input() podcasts: Array<Podcast>;

  constructor() { }

  ngOnInit() {
  }

}
