import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { CreateFormComponent } from './create-form/create-form.component';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MaterialModule } from './material/material.module';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ReactiveFormsModule } from '@angular/forms';
import { ResultPageComponent } from './result-page/result-page.component';
import { Mp3FormComponent } from './mp3-form/mp3-form.component'
import { TranslateFormComponent } from './translate-form/translate-form.component';
import { TranscribeFormComponent } from './transcribe-form/transcribe-form.component';
import { MyFeedsPageComponent } from './my-feeds-page/my-feeds-page.component';
import { LoginPopupComponent } from './login-popup/login-popup.component'

@NgModule({
  declarations: [
    AppComponent,
    CreateFormComponent,
    ResultPageComponent,
    Mp3FormComponent,
    TranslateFormComponent,
    TranscribeFormComponent,
    MyFeedsPageComponent,
    LoginPopupComponent
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    ClipboardModule,
    FormsModule,
    HttpClientModule,
    LayoutModule,
    MatButtonModule,
    MatDialogModule,
    MaterialModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [LoginPopupComponent]
})
export class AppModule { }
