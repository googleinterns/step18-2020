import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

const FEED_URL = '/rss-feed';
const LOGIN_URL = '/login-status';

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  private feedValueSubject = new BehaviorSubject<string>("Loading URL...");
  feedValue = this.feedValueSubject.asObservable();

  private loginLinkSubject = new BehaviorSubject<string>("Loading...");
  loginLink = this.loginLinkSubject.asObservable();

  private myFeedsSubject = new BehaviorSubject<Array<any>>([]);
  myFeeds = this.myFeedsSubject.asObservable();

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
   * Update the list of "my feeds" with the feeds from post request.
   */
  sendMyFeeds(feeds) {
    console.log("My feeds service: " + feeds);
    this.myFeedsSubject.next(feeds);
  }

  /**
   * Post form inputs to back-end and retrieve url for rss feed.
   */
  postFormData(formData): Observable<string> {
    return this.http.post(FEED_URL, formData, { responseType: 'text' });
  }

  /**
   * Fetch Login Status from LoginServlet.
   */
  getLoginData(): Observable<any> {
    return this.http.get(LOGIN_URL);
  }
}
