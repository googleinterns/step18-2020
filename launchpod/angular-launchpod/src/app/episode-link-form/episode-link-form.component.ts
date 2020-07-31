import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { HttpParams } from '@angular/common/http';

interface Language {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-episode-link-form',
  templateUrl: './episode-link-form.component.html',
  styleUrls: ['./episode-link-form.component.css']
})
export class EpisodeLinkFormComponent implements OnInit {
  
  languages: Language[] = [
    {value: 'en', viewValue: 'English'},
    {value: 'es', viewValue: 'Spanish'},
  ];

  selectedLanguage = this.languages[0].value;

  constructor(private formHandlerService: FormHandlerService) { }

  ngOnInit(): void {
  }

  // Sends episode creation data to back end when user clicks the "Add Episode" button.
  public postFormData() {
    let formData = new HttpParams();
    formData = formData.set('title', (document.getElementById("title") as HTMLInputElement).value);
    formData = formData.set('description', (document.getElementById("description") as HTMLInputElement).value);
    formData = formData.set('language', this.selectedLanguage);
    formData = formData.set('mp3Link', (document.getElementById("mp3Link") as HTMLInputElement).value);

    this.formHandlerService.postFormData(formData)
      .subscribe((response) => {
        this.formHandlerService.sendFeedValue(response);
      });
  }
}
