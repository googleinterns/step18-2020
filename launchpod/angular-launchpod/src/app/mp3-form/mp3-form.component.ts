import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-mp3-form',
  templateUrl: './mp3-form.component.html',
  styleUrls: ['./mp3-form.component.css']
})
export class Mp3FormComponent implements OnInit {

  constructor(private formHandlerService: FormHandlerService) { }

  ngOnInit(): void {
    this.formHandlerService.feedValue.subscribe((feedValue) => {
      console.log("Result: " + feedValue)
      // this.feedValue = feedValue;
      document.getElementById('mp3-upload').innerHTML = feedValue;
    });
  }

}
