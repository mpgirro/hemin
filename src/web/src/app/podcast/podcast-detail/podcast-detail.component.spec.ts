import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PodcastDetailComponent } from './podcast-detail.component';

describe('PodcastDetailComponent', () => {
  let component: PodcastDetailComponent;
  let fixture: ComponentFixture<PodcastDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PodcastDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PodcastDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
