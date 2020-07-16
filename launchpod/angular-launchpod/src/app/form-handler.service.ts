import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import {map} from 'rxjs/operators';

const FEED_URL = '/rss-feed';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  private feedValueSubject = new Subject<string>();
  feedValue = this.feedValueSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Update the feedValue with the url from post request.
   */
  sendFeedValue(data) {
        console.log("Service Data: " + data);
        this.feedValueSubject.next(data);
    }

  /**
   * Post form inputs to back-end and retrieve url for rss feed.
   */
  postFormData(formData): Observable<string> {
    return this.http.post(FEED_URL, formData, { responseType: 'text' });
  }
}
