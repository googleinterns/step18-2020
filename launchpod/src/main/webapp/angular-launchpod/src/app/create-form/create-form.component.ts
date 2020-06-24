import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-create-form',
  templateUrl: './create-form.component.html',
  styleUrls: ['./create-form.component.css']
})
export class CreateFormComponent implements OnInit {
  constructor() {}

  // fetches input data when user clicks create button
  public getData() {
    // TODO: implement method to actually fetch data
    document.getElementById('message').textContent =
        'Your request cannot be fulfilled at this time.';
  }

  ngOnInit(): void {}
}
