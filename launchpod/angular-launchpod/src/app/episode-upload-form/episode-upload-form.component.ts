import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { HttpParams } from '@angular/common/http';

interface Language {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-episode-upload-form',
  templateUrl: './episode-upload-form.component.html',
  styleUrls: ['./episode-upload-form.component.css']
})
export class EpisodeUploadFormComponent implements OnInit {
  
  languages: Language[] = [
    {value: 'en', viewValue: 'English'},
    {value: 'es', viewValue: 'Spanish'},
  ];

  selectedLanguage = this.languages[0].value;

  constructor(private formHandlerService: FormHandlerService) { }

  ngOnInit(): void {
  }

  // Sends episode creation data to back end when user clicks the "Add Episode" button.
  public postEpisodeUploadData() {
    let formData = new HttpParams();
    formData = formData.set('episodeTitle', (document.getElementById("episodeTitle") as HTMLInputElement).value);
    formData = formData.set('episodeDescription', (document.getElementById("episodeDescription") as HTMLInputElement).value);
    formData = formData.set('episodeLanguage', this.selectedLanguage);

    this.formHandlerService.currentFeedKey.subscribe((id) => {
      formData = formData.set('id', id);
    });

    this.formHandlerService.postEpisodeUploadData(formData)
      .subscribe((response) => {
        this.formHandlerService.sendFeedValue(response);
      });
  }
}

