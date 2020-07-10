import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

interface Language {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-translate-form',
  templateUrl: './translate-form.component.html',
  styleUrls: ['./translate-form.component.css']
})
export class TranslateFormComponent implements OnInit {

  feedValue: string;
  
  languages: Language[] = [
    {value: 'es', viewValue: 'Spanish'},
  ];

  constructor() {}

  ngOnInit(): void {}

  // Sends input data to backend when user clicks create button.
  public postFormData() {
    //TODO: implement this method https://github.com/googleinterns/step18-2020/issues/28
  }

}
