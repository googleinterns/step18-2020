import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService, public snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.formHandlerService.getLinkToCopy()
      .subscribe((response) => {
        this.feedValue = response;
      });
  }

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
