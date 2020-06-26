import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {
  // TODO: actually get url from backend
  feedValue: string;

  constructor(private formHandlerService: FormHandlerService, public snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.formHandlerService.getFeedUrl()
      .subscribe((feedData) => {
        console.log('hello');
        console.log(feedData);
        this.feedValue = feedData;
    });
  }

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
