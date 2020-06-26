import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

const FEED_URL = '/load-feed';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  constructor(private http: HttpClient) { }

  /**
   * Fetch feed url from back-end
   */
  getFeedUrl(): Observable<string> {
    return this.http.get<string>(FEED_URL);
  }
}
