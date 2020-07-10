import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService, public snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.formHandlerService.feedValue.subscribe((feedValue) => {
      console.log("Result: " + feedValue)
      this.feedValue = feedValue;
    });
  }

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
