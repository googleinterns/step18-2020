import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';
import { Router } from '@angular/router'

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
    {value: 'af', viewValue: 'Afrikaans'},
    {value: 'sq', viewValue: 'Albanian'},
    {value: 'am', viewValue: 'Amharic'},
    {value: 'ar', viewValue: 'Arabic'},
    {value: 'zh', viewValue: 'Chinese(Simplified)'},
    {value: 'zh-TW', viewValue: 'Chinese(Traditional)'},
    {value: 'cs', viewValue: 'Czech'},
    {value: 'da', viewValue: 'Danish'},
    {value: 'nl', viewValue: 'Dutch'},
    {value: 'en', viewValue: 'English'},
    {value: 'fr', viewValue: 'French'},
    {value: 'de', viewValue: 'German'},
    {value: 'el', viewValue: 'Greek'},
    {value: 'haw', viewValue: 'Hawaiian'},
    {value: 'he', viewValue: 'Hebrew'},
    {value: 'id', viewValue: 'Indonesian'},
    {value: 'ga', viewValue: 'Irish'},
    {value: 'it', viewValue: 'Italian'},
    {value: 'ja', viewValue: 'Japanese'},
    {value: 'ko', viewValue: 'Korean'},
    {value: 'la', viewValue: 'Latin'},
    {value: 'mn', viewValue: 'Mongolian'},
    {value: 'no', viewValue: 'Norwegian'},
    {value: 'fa', viewValue: 'Persian'},
    {value: 'pl', viewValue: 'Polish'},
    {value: 'pt', viewValue: 'Portuguese'},
    {value: 'ro', viewValue: 'Romanian'},
    {value: 'ru', viewValue: 'Russian'},
    {value: 'es', viewValue: 'Spanish'},
    {value: 'sv', viewValue: 'Swedish'},
    {value: 'tr', viewValue: 'Turkish'},
    {value: 'vi', viewValue: 'Vietnamese'},
  ];

  selected = this.languages[9].value;

  constructor(private formHandlerService: FormHandlerService , private router: Router) {}

  ngOnInit(): void {}

  public postFormData() {
    let formData = new HttpParams();
    formData = formData.set('rssFeedLink', (document.getElementById("rssFeedLink") as HTMLInputElement).value);
    formData = formData.set('language', (document.getElementById("language") as HTMLInputElement).value);
    //formData = formData.set('language', this.selected);

    this.formHandlerService.postTranslationData(formData)
    .subscribe((response) => {
      this.formHandlerService.sendMyFeeds(response);
      this.formHandlerService.updateHasNewFeed();
    });

    this.router.navigate(['/my-feeds']);
  }

}
