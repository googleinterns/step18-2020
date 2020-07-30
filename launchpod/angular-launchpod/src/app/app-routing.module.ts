import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ResultPageComponent } from './result-page/result-page.component';
import { AppComponent } from './app.component';
import { CreateFormComponent } from './create-form/create-form.component';
import { Mp3FormComponent } from './mp3-form/mp3-form.component';
import { TranscribeFormComponent } from './transcribe-form/transcribe-form.component';
import { TranslateFormComponent } from './translate-form/translate-form.component';
import { MyFeedsPageComponent } from './my-feeds-page/my-feeds-page.component';

const routes: Routes = [
  { path: '', component: CreateFormComponent},
  { path: 'create', component: CreateFormComponent },
  { path: 'transcribe', component: TranscribeFormComponent },
  { path: 'translate', component: TranslateFormComponent },
  { path: 'my-feeds', component: MyFeedsPageComponent },
  { path: 'result', component: ResultPageComponent},
  { path: 'mp3-form', component: Mp3FormComponent},
  { path: '**', redirectTo: '/create', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
