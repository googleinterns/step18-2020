import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import {map} from 'rxjs/operators';

const FEED_URL = '/rss-feed';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  private feedValueSubject = new BehaviorSubject<string>("Loading...");
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
    console.log("Form data: " + formData);
    return this.http.post(FEED_URL, formData, { responseType: 'text' });
  }

  /**
   * Get action needed to generate link to RSS feed.
   */
  getLinkToCopy(): Observable<any> {
    return this.http.get(FEED_URL, { responseType: 'text' });
  }

}
