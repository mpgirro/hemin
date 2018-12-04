import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Podcast } from './podcast.model';
import { Episode } from '../../episode/shared/episode.model';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/observable/of';
import {ArrayWrapper} from '../../arraywrapper.model';
import {Feed} from './feed.model';
import {Image} from '../../image.model';

@Injectable()
export class PodcastService {

  private baseUrl = '/api/v1/podcast';  // URL to web API

  constructor(private http: HttpClient) { }

  get(id: string): Observable<Podcast> {
    const request = this.baseUrl + '/' + id;
    console.log('GET ' + request);
    return this.http
      .get<Podcast>(request)
      .pipe(
        tap(_ => console.log(`found podcast : "${id}"`)),
        catchError(this.handleError<Podcast>('getPodcast', new Podcast()))
      );
  }

  getEpisodes(podcastId: string): Observable<ArrayWrapper<Episode>> {
    const request = this.baseUrl + '/' + podcastId + '/episodes';
    console.log('GET ' + request);
    return this.http
      .get<ArrayWrapper<Episode>>(request)
      .pipe(
        tap(_ => console.log(`found episodes for podcast : "${podcastId}"`)),
        catchError(this.handleError<ArrayWrapper<Episode>>('getEpisodesByPodcast', new ArrayWrapper<Episode>()))
      );
  }

  getFeeds(podcastId: string): Observable<ArrayWrapper<Feed>> {
    const request = this.baseUrl + '/' + podcastId + '/feeds';
    console.log('GET ' + request);
    return this.http
      .get<ArrayWrapper<Feed>>(request)
      .pipe(
        tap(_ => console.log(`found feeds for podcast : "${podcastId}"`)),
        catchError(this.handleError<ArrayWrapper<Feed>>('getFeedsByPodcast', new ArrayWrapper<Feed>()))
      );
  }

  getAll(page: number, size: number): Observable<ArrayWrapper<Podcast>> {
    const p = (page) ? `&p=${page}` : '';
    const s = (size) ? `&s=${size}` : '';
    const request = this.baseUrl + '?' + p + s;
    console.log('GET ' + request);
    return this.http
      .get<ArrayWrapper<Podcast>>(request)
      .pipe(
        tap(_ => console.log(`found all podcasts`)),
        catchError(this.handleError<ArrayWrapper<Podcast>>('getAllPodcasts', new ArrayWrapper<Podcast>()))
      );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      // this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
