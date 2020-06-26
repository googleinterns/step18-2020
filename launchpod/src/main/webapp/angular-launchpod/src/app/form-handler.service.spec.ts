import { TestBed } from '@angular/core/testing';

import { FormHandlerService } from './form-handler.service';

describe('FormHandlerService', () => {
  let service: FormHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FormHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
