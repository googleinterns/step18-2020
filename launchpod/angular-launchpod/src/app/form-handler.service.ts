import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

const FEED_URL = '/rss-feed';
const UPLOAD_URL = 'create-by-upload';
const LINK_URL = 'create-by-link';
const LOGIN_URL = '/login-status';
const TRANSLATION_URL = "/translate-feed";

@Injectable({
  providedIn: 'root'
})
export class FormHandlerService {

  private readonly feedValueSubject = new BehaviorSubject<string>("Loading URL...");
  feedValue = this.feedValueSubject.asObservable();

  private readonly loginLinkSubject = new BehaviorSubject<string>("Loading...");
  loginLink = this.loginLinkSubject.asObservable();

  private readonly myFeedsSubject = new BehaviorSubject<Array<any>>([]);
  myFeeds = this.myFeedsSubject.asObservable();

  private readonly hasNewFeedSubject = new BehaviorSubject<boolean>(false);
  hasNewFeed = this.hasNewFeedSubject.asObservable();

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
    this.myFeedsSubject.next(feeds);
  }

  /**
  * Update the newFeed boolean when a new feed is added.
  */
  updateHasNewFeed() {
    this.hasNewFeedSubject.next(true);
  }

  /**
   * Post inputs from feed creation form to back end and retrieve URL for RSS feed.
   */
  postFormData(formData): Observable<any> {
    return this.http.post(FEED_URL, formData);
  }

  /**
   * Post form inputs to back-end and retrieve url for rss feed for MP3 uploads.
   */
  postEpisodeUploadData(formData): Observable<string> {
    console.log("Form data: " + formData);
    return this.http.post(UPLOAD_URL, formData, { responseType: 'text' });
  }

  /**
   * Post input from episode by link creation form to back end and retrieve URL for RSS feed.
  */
  postEpisodeLinkData(formData): Observable<string> {
    return this.http.post(LINK_URL, formData, { responseType: 'text' });
  }

   /**
   * Post form inputs to back-end and retrieve url for rss feed.
   */
  postTranslationData(formData): Observable<any> {
    return this.http.post(TRANSLATION_URL, formData);
  }

  /**
   * Fetch Login Status from LoginServlet.
   */
  getLoginData(): Observable<any> {
    return this.http.get(LOGIN_URL);
  }

  /**
   * Get action needed to generate link to RSS feed.
   */
  getLinkToCopy(): Observable<any> {
    return this.http.get(FEED_URL, { responseType: 'text' });
  }

  /**
   * Post deletion request to LoginServlet.
   */
  deleteFeedEntity(formData): Observable<any> {
    return this.http.post(LOGIN_URL, formData);
  }
}
