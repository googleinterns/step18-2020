import { FormHandlerService } from '../form-handler.service';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mp3-form',
  templateUrl: './mp3-form.component.html',
  styleUrls: ['./mp3-form.component.css']
})
export class Mp3FormComponent implements OnInit {

  constructor(private formHandlerService: FormHandlerService, private router: Router) { }

  ngOnInit(): void {
    this.formHandlerService.feedValue.subscribe((feedValue) => {
      console.log("Result: " + feedValue)
      document.getElementById('mp3-upload').innerHTML = feedValue;
    });
  }

  onSubmit() {
    this.router.navigateByUrl('/result');
  }     

}
