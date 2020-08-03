import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';

interface Language {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-episode-tts-form',
  templateUrl: './episode-tts-form.component.html',
  styleUrls: ['./episode-tts-form.component.css']
})
export class EpisodeTtsFormComponent implements OnInit {

  key: String;

  languages: Language[] = [
    {value: 'en', viewValue: 'English'},
    {value: 'es', viewValue: 'Spanish'},
  ];

  selectedLanguage = this.languages[0].value;

  constructor(private formHandlerService: FormHandlerService, private router: Router) {}

  ngOnInit(): void {}

  public postEpisodeLinkData() {
    let formData = new HttpParams();
    formData = formData.set('episodeTitle', (document.getElementById("episodeTitle") as HTMLInputElement).value);
    formData = formData.set('episodeDescription', (document.getElementById("episodeDescription") as HTMLInputElement).value);
    formData = formData.set('episodeLanguage', this.selectedLanguage);
    formData = formData.set('text', (document.getElementById("text") as HTMLInputElement).value);

    this.formHandlerService.currentFeedKey.subscribe((key) => {
      formData = formData.set('keyId', key);
    });

    this.formHandlerService.postEpisodeTTSData(formData)
      .subscribe((response) => {
        this.formHandlerService.sendFeedValue(response);
      });
  }
}
