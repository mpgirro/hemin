import { NgModule } from '@angular/core';

import { EpisodeService } from './shared/episode.service';

/*
import { EpisodeDetailComponent } from './episode-detail/episode-detail.component';
import { EpisodeTeaserComponent } from './episode-teaser/episode-teaser.component';
import { EpisodeRichlistComponent } from './episode-richlist/episode-richlist.component';
import { EpisodeTablelistComponent } from './episode-tablelist/episode-tablelist.component';
*/

@NgModule({
  declarations: [
    /*
    EpisodeDetailComponent,
    EpisodeTeaserComponent,
    EpisodeRichlistComponent,
    EpisodeTablelistComponent
    */
  ],
  providers: [EpisodeService]
})
export class EpisodeModule { }
