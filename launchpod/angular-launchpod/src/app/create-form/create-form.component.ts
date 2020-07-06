import {Component, OnInit} from '@angular/core';
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

  constructor(private formHandlerService: FormHandlerService) {}

  ngOnInit(): void {}

  // Sends input data to backend when user clicks create button.
  public postFormData() {
    let formData = new HttpParams();
    formData = formData.set('title', (document.getElementById("title") as HTMLInputElement).value);
    formData = formData.set('mp3Link', (document.getElementById("mp3Link") as HTMLInputElement).value);

    this.formHandlerService.postFormData(formData)
      .subscribe(
        (response) => console.log(response),
      );
  }
}
