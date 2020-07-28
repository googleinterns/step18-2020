import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpParams } from '@angular/common/http';

interface Key {
  kind: string;
  id: string;
}

interface Feed {
  title: string;
  rssLink: string;
  postTime: string;
  description: string;
  key: string;
}

@Component({
  selector: 'app-my-feeds-page',
  templateUrl: './my-feeds-page.component.html',
  styleUrls: ['./my-feeds-page.component.css']
})
export class MyFeedsPageComponent implements OnInit {

  myFeeds: Feed[];

  constructor(private formHandlerService: FormHandlerService, public snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.formHandlerService.myFeeds.subscribe((feeds) => {
      this.myFeeds = feeds;
    });
  }

  // Sends input data to backend when user clicks create button.
  public deleteFeed(key) {
    let formData = new HttpParams();
    formData = formData.set('keyId', key);
    console.log("Key: " + key);

    this.formHandlerService.deleteFeedEntity(formData)
      .subscribe((response) => {
        console.log(response);
      });
    window.location.reload();
  }

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
