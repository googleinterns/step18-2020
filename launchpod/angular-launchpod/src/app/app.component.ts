import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {Component, OnInit} from '@angular/core';
import {LoginPopupComponent} from './login-popup/login-popup.component';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'angular-launchpod';

  isHandset$: Observable<boolean> =
      this.breakpointObserver.observe(Breakpoints.Handset)
          .pipe(map(result => result.matches), shareReplay());

  constructor(private breakpointObserver: BreakpointObserver, public dialog: MatDialog) {}

  ngOnInit(): void {
    this.getLoginStatus();
  }

  getLoginStatus() {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;

    this.dialog.open(LoginPopupComponent, dialogConfig);
  }
}
