import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PodcastMatrixComponent } from './podcast-matrix.component';

describe('PodcastMatrixComponent', () => {
  let component: PodcastMatrixComponent;
  let fixture: ComponentFixture<PodcastMatrixComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PodcastMatrixComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PodcastMatrixComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
