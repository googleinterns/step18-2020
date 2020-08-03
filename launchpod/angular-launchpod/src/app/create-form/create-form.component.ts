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
    {value: 'en', viewValue: 'English'},
    {value: 'es', viewValue: 'Spanish'},
  ];

  selectedCategory = this.categories[0].value;
  selectedLanguage = this.languages[0].value;

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
      this.formHandlerService.updateHasNewFeed();
    });

    this.router.navigate(['/my-feeds']);
  }
}
