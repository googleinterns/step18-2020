import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpParams } from '@angular/common/http';
import { Router } from '@angular/router'

interface Feed {
  title: string;
  rssLink: string;
  postTime: string;
  description: string;
  key: string;
  language: string;
}

@Component({
  selector: 'app-my-feeds-page',
  templateUrl: './my-feeds-page.component.html',
  styleUrls: ['./my-feeds-page.component.css']
})
export class MyFeedsPageComponent implements OnInit {

  hasNewFeed: boolean;
  myFeeds: Feed[];

  constructor(private formHandlerService: FormHandlerService, public snackBar: MatSnackBar, private router: Router) {}

  ngOnInit(): void {
    this.formHandlerService.myFeeds.subscribe((feeds) => {
      this.myFeeds = feeds;
      this.formHandlerService.hasNewFeed.subscribe((result) => {
        this.hasNewFeed = result;
      });
    });
  }

  // Send the key for the feed the user wants to delete to the backend.
  public deleteFeed(key) {
    let formData = new HttpParams();
    formData = formData.set('keyId', key);

    this.formHandlerService.deleteFeedEntity(formData)
      .subscribe((feeds) => {
        this.formHandlerService.sendMyFeeds(feeds);
        this.hasNewFeed = false;
        this.myFeeds = feeds;
      });
    
  }

  // Send the key for the feed the user wants to edit to the backend.
  public addEpisode(key) {
    this.formHandlerService.sendCurrentFeedKey(key);

    this.router.navigate(['/episode-choice']);
  }

  openSnackBar() {
    // open() takes in a message, an action, and a configuration.
    // There is no action for this snackbar as it disappears on its own.
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
