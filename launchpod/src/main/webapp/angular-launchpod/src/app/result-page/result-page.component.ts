import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {
  // TODO: actually get url from backend
  feedValue = 'rss-feed-unavailable.launchpod.com';

  constructor(public snackBar: MatSnackBar) {}

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }

  ngOnInit(): void {}
}
