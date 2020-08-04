import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeTtsFormComponent } from './episode-tts-form.component';

describe('EpisodeTtsFormComponent', () => {
  let component: EpisodeTtsFormComponent;
  let fixture: ComponentFixture<EpisodeTtsFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeTtsFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeTtsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
