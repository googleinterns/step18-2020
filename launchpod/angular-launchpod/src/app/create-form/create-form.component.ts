import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-create-form',
  templateUrl: './create-form.component.html',
  styleUrls: ['./create-form.component.css']
})

export class CreateFormComponent implements OnInit {

  feedValue: string;

  constructor(private formHandlerService: FormHandlerService) {}

  ngOnInit(): void {}

  // Sends input data to backend when user clicks create button.
  public postUploadData() {
    let formData = new HttpParams();
    formData = formData.set('title', (document.getElementById("title") as HTMLInputElement).value);
    formData = formData.set('description', (document.getElementById("description") as HTMLInputElement).value);
    formData = formData.set('language', (document.getElementById("language") as HTMLInputElement).value);
    formData = formData.set('email', (document.getElementById("email") as HTMLInputElement).value);

    this.formHandlerService.postUploadData(formData)
      .subscribe((response) => {
        this.feedValue = response;
        console.log("Create feedValue: " + this.feedValue);
        this.formHandlerService.sendFeedValue(response);
      });
  }
}
