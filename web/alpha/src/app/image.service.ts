import {catchError, tap} from 'rxjs/operators';
import {Observable} from 'rxjs/Observable';
import {of} from 'rxjs/observable/of';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Image} from './image.model';

@Injectable()
export class ImageService {

  private baseUrl = '/api/v1/image';  // URL to web API

  constructor(private http: HttpClient) { }

  get(id: string): Observable<Image> {
    const request = this.baseUrl + '/' + id;
    console.log('GET ' + request);
    return this.http
      .get<Image>(request)
      .pipe(
        tap(_ => console.log(`found image : "${id}"`)),
        catchError(this.handleError<Image>('getImage', new Image()))
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
