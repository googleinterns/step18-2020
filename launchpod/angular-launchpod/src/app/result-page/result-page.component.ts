import { FormHandlerService } from '../form-handler.service';
import { CreateFormComponent } from '../create-form/create-form.component';
import { Component, Input, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.css']
})
export class ResultPageComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService, private createFormComponent: CreateFormComponent, public snackBar: MatSnackBar) { }

  ngOnInit(): void {

    this.formHandlerService.getLinkToCopy()
      .subscribe((response) => {
        this.feedValue = response;
        // console.log("Create feedValue: " + this.feedValue);
        // this.formHandlerService.sendFeedValue(response);
      });
  }

  openSnackBar() {
    this.snackBar.open('Copied URL to clipboard', '', {
      duration: 2000,
    });
  }
}
