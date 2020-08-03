import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeLinkFormComponent } from './episode-link-form.component';

describe('EpisodeLinkFormComponent', () => {
  let component: EpisodeLinkFormComponent;
  let fixture: ComponentFixture<EpisodeLinkFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeLinkFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeLinkFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
