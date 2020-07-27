import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

interface Feed {
  title: string;
  rssLink: string;
  postTime: string;
  description: string;
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

}
