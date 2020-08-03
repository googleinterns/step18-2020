import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeUploadFormComponent } from './episode-upload-form.component';

describe('EpisodeUploadFormComponent', () => {
  let component: EpisodeUploadFormComponent;
  let fixture: ComponentFixture<EpisodeUploadFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeUploadFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeUploadFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
