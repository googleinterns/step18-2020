import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ResultPageComponent } from './result-page/result-page.component';
import { AppComponent } from './app.component';
import { CreateFormComponent } from './create-form/create-form.component';
import { Mp3FormComponent } from './mp3-form/mp3-form.component';


const routes: Routes = [
  { path: '', component: CreateFormComponent},
  { path: 'result', component: ResultPageComponent},
  { path: 'mp3-form', component: Mp3FormComponent},
  { path: '**', redirectTo: '/create', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
