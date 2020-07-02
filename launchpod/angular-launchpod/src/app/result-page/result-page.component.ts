import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService, private http: HttpClient, public snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.formHandlerService.getFeedUrl()
      .subscribe((feedData) => {
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
