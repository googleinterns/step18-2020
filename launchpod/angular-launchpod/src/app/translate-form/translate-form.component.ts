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

  constructor(private formHandlerService: FormHandlerService) {}

  ngOnInit(): void {}

  public postFormData() {
    let formData = new HttpParams();
    formData = formData.set('rssFeedLink', (document.getElementById("rssFeedLink") as HTMLInputElement).value);
    formData = formData.set('language', (document.getElementById("language") as HTMLInputElement).value);

    this.formHandlerService.postTranslationData(formData)
      .subscribe((response) => {
        this.feedValue = response;
        console.log("Create feedValue: " + this.feedValue);
        this.formHandlerService.sendFeedValue(response);
      });
  }

}
