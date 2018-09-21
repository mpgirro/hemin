import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import {catchError, tap} from 'rxjs/operators';
import {of} from 'rxjs/observable/of';
import { Episode } from './episode.model';
import {Chapter} from './chapter.model';
import {ArrayWrapper} from '../../arraywrapper.model';

@Injectable()
export class EpisodeService {

  private baseUrl = '/api/v1/episode';  // URL to web API

  constructor(private http: HttpClient) { }

  get(exo: string): Observable<Episode> {
    const request = this.baseUrl + '/' + exo;
    console.log('GET ' + request);
    return this.http.get<Episode>(request).pipe(
      tap(_ => console.log(`episode : "${exo}"`)),
      catchError(this.handleError<Episode>('get episode', new Episode()))
    );
  }

  getChapters(exo: string): Observable<ArrayWrapper<Chapter>> {
    const request = this.baseUrl + '/' + exo + '/chapters';
    console.log('GET ' + request);
    return this.http.get<ArrayWrapper<Chapter>>(request).pipe(
      tap(_ => console.log(`found chapters for episode : "${exo}"`)),
      catchError(this.handleError<ArrayWrapper<Chapter>>('getChaptersByEpisode', new ArrayWrapper<Chapter>()))
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
