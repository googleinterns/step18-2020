import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import {map} from 'rxjs/operators';

const FEED_URL = '/rss-feed';
const UPLOAD_URL = 'create-by-upload';
const LOGIN_URL = '/login-status';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  private feedValueSubject = new BehaviorSubject<string>("Loading URL...");

  feedValue = this.feedValueSubject.asObservable();

  private loginLinkSubject = new BehaviorSubject<string>("Loading...");
  loginLink = this.loginLinkSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Update the feedValue with the url from post request.
   */
  sendFeedValue(data) {
    this.feedValueSubject.next(data);
  }

  /**
   * Update the loginLink with the url from post request.
   */
  sendLoginLink(link) {
    this.loginLinkSubject.next(link);
  }

  /**
   * Post form inputs to back-end and retrieve url for rss feed.
   */
  postFormData(formData): Observable<string> {
    console.log("Form data: " + formData);
    return this.http.post(FEED_URL, formData, { responseType: 'text' });
  }

  /**
   * Post form inputs to back-end and retrieve url for rss feed for MP3 uploads.
   */
  postUploadData(formData): Observable<string> {
    console.log("Form data: " + formData);
    return this.http.post(UPLOAD_URL, formData, { responseType: 'text' });
  }

  /**
   * Get action needed to generate link to RSS feed.
   */
  getLinkToCopy(): Observable<any> {
    return this.http.get(FEED_URL, { responseType: 'text' });
  }

  /*
   * Fetch Login Status from LoginServlet.
   */
  getLoginData(): Observable<any> {
    return this.http.get(LOGIN_URL);
  }
}
