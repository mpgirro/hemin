import { Injectable } from '@angular/core';

import { ResultWrapper } from './resultwrapper.model';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of'; // TODO brauch ich eh nimma, oder?
import { HttpClient} from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';

@Injectable()
export class SearchService {

  private baseUrl = '/api/v1/search?';  // URL to web API

  constructor(private http: HttpClient) { }

  search(query: string, page: number, size: number): Observable<ResultWrapper> {
    if (!query || !query.trim()) {
      // if not search term, return empty result array.
      return of(new ResultWrapper());
    }

    const q = 'q=' + query;
    const p = (page) ? `&p=${page}` : '';
    const s = (size) ? `&s=${size}` : '';

    const request = this.baseUrl + q + p + s;

    console.log('GET ' + request);
    return this.http.get<ResultWrapper>(request).pipe(
      tap(_ => console.log(`found results matching GET "${request}"`)),
      catchError(this.handleError<ResultWrapper>(`GET ${request}`, new ResultWrapper()))
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
