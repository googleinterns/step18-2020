import { Component, OnInit } from '@angular/core';
import { FormHandlerService } from '../form-handler.service';

@Component({
  selector: 'app-login-popup',
  templateUrl: './login-popup.component.html',
  styleUrls: ['./login-popup.component.css']
})
export class LoginPopupComponent implements OnInit {

  constructor(private formHandlerService: FormHandlerService) {}

  ngOnInit(): void {
    this.formHandlerService.loginLink.subscribe((link) => {
      document.getElementById("login-button").setAttribute("href", link);
    });
  }
}
