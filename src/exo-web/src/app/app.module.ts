import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

import { SearchComponent } from './search/search.component';
import { DirectoryOverviewComponent } from './directory/directory-overview/directory-overview.component';

import { PodcastDetailComponent } from './podcast/podcast-detail/podcast-detail.component';
import { PodcastRichlistComponent } from './podcast/podcast-richlist/podcast-richlist.component';
import { PodcastMatrixComponent } from './podcast/podcast-matrix/podcast-matrix.component';

import { EpisodeDetailComponent } from './episode/episode-detail/episode-detail.component';
import { EpisodeTeaserComponent } from './episode/episode-teaser/episode-teaser.component';
import { EpisodeRichlistComponent } from './episode/episode-richlist/episode-richlist.component';
import { EpisodeTablelistComponent } from './episode/episode-tablelist/episode-tablelist.component';

import { NavbarComponent } from './core/navbar/navbar.component';

import { DomainService } from './domain.service';
import { SearchService } from './search.service';

import { CoreModule } from './core/core.module';
import { PodcastModule } from './podcast/podcast.module';
import { EpisodeModule } from './episode/episode.module';

import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';

import { Angular2FontawesomeModule } from 'angular2-fontawesome/angular2-fontawesome';
import { DirectoryListComponent } from './directory/directory-list/directory-list.component';
import { LoginComponent } from './login/login.component';

@NgModule({
  declarations: [
    AppComponent,
    SearchComponent,
    PodcastDetailComponent,
    DirectoryOverviewComponent,
    EpisodeDetailComponent,
    EpisodeTeaserComponent,
    EpisodeRichlistComponent,
    EpisodeTablelistComponent,
    NavbarComponent,
    PodcastRichlistComponent,
    PodcastMatrixComponent,
    DirectoryListComponent,
    LoginComponent
  ],
  imports: [
    CoreModule,
    PodcastModule,
    EpisodeModule,
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    Angular2FontawesomeModule
  ],
  providers: [SearchService, DomainService],
  bootstrap: [AppComponent]
})
export class AppModule { }
