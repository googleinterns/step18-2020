import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';

interface Category {
  value: string;
}

interface Language {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-create-form',
  templateUrl: './create-form.component.html',
  styleUrls: ['./create-form.component.css']
})

export class CreateFormComponent implements OnInit {

  categories: Category[] = [
    {value: 'Arts'},
    {value: 'Business'},
    {value: 'Comedy'},
    {value: 'Education'},
    {value: 'Fiction'},
    {value: 'Government'},
    {value: 'History'},
    {value: 'Health & Fitness'},
    {value: 'Kids & Family'},
    {value: 'Leisure'},
    {value: 'Music'},
    {value: 'News'},
    {value: 'Religion & Spirituality'},
    {value: 'Science'},
    {value: 'Society & Culture'},
    {value: 'Sports'},
    {value: 'Technology'},
    {value: 'True Crime'},
    {value: 'TV & Film'},
  ];

  languages: Language[] = [
    {value: 'af', viewValue: 'Afrikaans'},
    {value: 'sq', viewValue: 'Albanian'},
    {value: 'am', viewValue: 'Amharic'},
    {value: 'ar', viewValue: 'Arabic'},
    {value: 'hy', viewValue: 'Armenian'},
    {value: 'az', viewValue: 'Azerbaijani'},
    {value: 'eu', viewValue: 'Basque'},
    {value: 'be', viewValue: 'Belarusian'},
    {value: 'bn', viewValue: 'Bengali'},
    {value: 'bs', viewValue: 'Bosnian'},
    {value: 'bg', viewValue: 'Bulgarian'},
    {value: 'ca', viewValue: 'Catalan'},
    {value: 'ceb', viewValue: 'Cebuano'},
    {value: 'zh', viewValue: 'Chinese(Simplified)'},
    {value: 'zh-TW', viewValue: 'Chinese(Traditional)'},
    {value: 'co', viewValue: 'Corsican'},
    {value: 'hr', viewValue: 'Croatian'},
    {value: 'cs', viewValue: 'Czech'},
    {value: 'da', viewValue: 'Danish'},
    {value: 'nl', viewValue: 'Dutch'},
    {value: 'en', viewValue: 'English'},
    {value: 'eo', viewValue: 'Esperanto'},
    {value: 'et', viewValue: 'Estonian'},
    {value: 'fi', viewValue: 'Finnish'},
    {value: 'fr', viewValue: 'French'},
    {value: 'fy', viewValue: 'Frisian'},
    {value: 'gl', viewValue: 'Galician'},
    {value: 'ka', viewValue: 'Georgian'},
    {value: 'de', viewValue: 'German'},
    {value: 'el', viewValue: 'Greek'},
    {value: 'ht', viewValue: 'Haitian Creole'},
    {value: 'ha', viewValue: 'Hausa'},
    {value: 'haw', viewValue: 'Hawaiian'},
    {value: 'he', viewValue: 'Hebrew'},
    {value: 'hi', viewValue: 'Hindi'},
    {value: 'hu', viewValue: 'Hungarian'},
    {value: 'is', viewValue: 'Icelandic'},
    {value: 'ig', viewValue: 'Igbo'},
    {value: 'id', viewValue: 'Indonesian'},
    {value: 'ga', viewValue: 'Irish'},
    {value: 'it', viewValue: 'Italian'},
    {value: 'ja', viewValue: 'Japanese'},
    {value: 'jv', viewValue: 'Javanese'},
    {value: 'ko', viewValue: 'Korean'},
    {value: 'ku', viewValue: 'Kurdish'},
    {value: 'la', viewValue: 'Latin'},
    {value: 'lt', viewValue: 'Lithuanian'},
    {value: 'mk', viewValue: 'Macedonian'},
    {value: 'ms', viewValue: 'Malay'},
    {value: 'mn', viewValue: 'Mongolian'},
    {value: 'ne', viewValue: 'Nepali'},
    {value: 'no', viewValue: 'Norwegian'},
    {value: 'fa', viewValue: 'Persian'},
    {value: 'pl', viewValue: 'Polish'},
    {value: 'pt', viewValue: 'Portuguese'},
    {value: 'ro', viewValue: 'Romanian'},
    {value: 'ru', viewValue: 'Russian'},
    {value: 'sm', viewValue: 'Samoan'},
    {value: 'sr', viewValue: 'Serbian'},
    {value: 'sl', viewValue: 'Slovenian'},
    {value: 'es', viewValue: 'Spanish'},
    {value: 'sw', viewValue: 'Swahili'},
    {value: 'sv', viewValue: 'Swedish'},
    {value: 'tl', viewValue: 'Tagalog'},
    {value: 'th', viewValue: 'Thai'},
    {value: 'tr', viewValue: 'Turkish'},
    {value: 'uk', viewValue: 'Ukrainian'},
    {value: 'vi', viewValue: 'Vietnamese'},
  ];

  selectedCategory = this.categories[0].value;
  selectedLanguage = this.languages[20].value;

  constructor(private formHandlerService: FormHandlerService, private router: Router) {}

  ngOnInit(): void {}

  // Sends input data to backend when user clicks create button.
  public postFormData() {
    let formData = new HttpParams();
    formData = formData.set('title', (document.getElementById("title") as HTMLInputElement).value);
    formData = formData.set('description', (document.getElementById("description") as HTMLInputElement).value);
    formData = formData.set('language', this.selectedLanguage);

    // TO-DO after merging: move this into episode-link-form component
    // this.formHandlerService.postUploadData(formData)
    //   .subscribe((response) => {
    //     this.feedValue = response;
    //     console.log("Create feedValue: " + this.feedValue);
    //     this.formHandlerService.sendFeedValue(response);
    // });
    // formData = formData.set('mp3Link', (document.getElementById("mp3Link") as HTMLInputElement).value);
    formData = formData.set('name', (document.getElementById("name") as HTMLInputElement).value);
    formData = formData.set('category', this.selectedCategory);

    this.formHandlerService.postFormData(formData)
    .subscribe((response) => {
      this.formHandlerService.sendMyFeeds(response);
      setInterval(() => {
        this.formHandlerService.updateHasNewFeed();
      }, 1000); 
    });

    this.router.navigate(['/my-feeds']);
  }
}
