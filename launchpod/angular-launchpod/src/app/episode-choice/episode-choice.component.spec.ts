import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeChoiceComponent } from './episode-choice.component';

describe('EpisodeChoiceComponent', () => {
  let component: EpisodeChoiceComponent;
  let fixture: ComponentFixture<EpisodeChoiceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeChoiceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeChoiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
