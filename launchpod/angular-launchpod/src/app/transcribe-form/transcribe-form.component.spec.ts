import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TranscribeFormComponent } from './transcribe-form.component';

describe('TranscribeFormComponent', () => {
  let component: TranscribeFormComponent;
  let fixture: ComponentFixture<TranscribeFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TranscribeFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TranscribeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
