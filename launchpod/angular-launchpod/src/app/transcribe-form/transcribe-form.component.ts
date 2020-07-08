import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-transcribe-form',
  templateUrl: './transcribe-form.component.html',
  styleUrls: ['./transcribe-form.component.css']
})
export class TranscribeFormComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService) { }

  ngOnInit(): void {
  }

  // Sends input data to backend when user clicks create button.
  public postFormData() {
    //TODO: implement this method
  }

}
