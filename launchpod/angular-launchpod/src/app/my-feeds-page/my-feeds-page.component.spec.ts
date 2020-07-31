import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MyFeedsPageComponent } from './my-feeds-page.component';

describe('MyFeedsPageComponent', () => {
  let component: MyFeedsPageComponent;
  let fixture: ComponentFixture<MyFeedsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MyFeedsPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MyFeedsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
