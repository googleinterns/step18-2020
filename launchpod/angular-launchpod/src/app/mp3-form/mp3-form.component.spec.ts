import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Mp3FormComponent } from './mp3-form.component';

describe('Mp3FormComponent', () => {
  let component: Mp3FormComponent;
  let fixture: ComponentFixture<Mp3FormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Mp3FormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Mp3FormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
