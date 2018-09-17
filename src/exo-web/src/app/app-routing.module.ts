import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchComponent } from './search/search.component';
import { PodcastDetailComponent } from './podcast/podcast-detail/podcast-detail.component';
import { DirectoryOverviewComponent } from './directory/directory-overview/directory-overview.component';
import { EpisodeDetailComponent } from './episode/episode-detail/episode-detail.component';
import {DirectoryListComponent} from './directory/directory-list/directory-list.component';
import {LoginComponent} from './login/login.component';

const routes: Routes = [
  { path: 'search', component: SearchComponent },
  /*{ path: 'search?query=:q', component: SearchComponent },*/
  { path: '', redirectTo: '/search', pathMatch: 'full' },
  { path: 'p/:id', component: PodcastDetailComponent },
  { path: 'e/:id', component: EpisodeDetailComponent },
  { path: 'directory', component: DirectoryOverviewComponent },
  { path: 'directory/list', component: DirectoryListComponent },
  { path: 'login', component: LoginComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
