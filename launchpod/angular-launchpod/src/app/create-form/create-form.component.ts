import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

interface Category {
  value: string;
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

  selected = this.categories[0].value;

  constructor(private formHandlerService: FormHandlerService) {}

  ngOnInit(): void {}

  // Sends input data to backend when user clicks create button.
  public postUploadData() {
    let formData = new HttpParams();
    formData = formData.set('title', (document.getElementById("title") as HTMLInputElement).value);
    formData = formData.set('description', (document.getElementById("description") as HTMLInputElement).value);
    formData = formData.set('language', (document.getElementById("language") as HTMLInputElement).value);
    formData = formData.set('email', (document.getElementById("email") as HTMLInputElement).value);
    // formData = formData.set('mp3Link', (document.getElementById("mp3Link") as HTMLInputElement).value);
    formData = formData.set('name', (document.getElementById("name") as HTMLInputElement).value);
    formData = formData.set('category', this.selected);

    this.formHandlerService.postUploadData(formData)
      .subscribe((response) => {
        this.formHandlerService.sendFeedValue(response);
      });
  }
}
