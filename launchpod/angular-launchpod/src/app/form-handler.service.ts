import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {map} from 'rxjs/operators';

const FEED_URL = '/load-result';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  constructor(private http: HttpClient) { }

  /**
   * Fetch feed url from back-end
   */
  getFeedUrl(): Observable<string>{
    return this.http.get(FEED_URL,{ responseType: 'text' });
  }
}
