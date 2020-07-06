import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {map} from 'rxjs/operators';

const FEED_URL = '/rss-feed';

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

  /**
   * Post form inputs to back-end
   */
  postFormData(formData): Observable<any> {
    return this.http.post(FEED_URL, formData);
  }
}
