import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PodcastRichlistComponent } from './podcast-richlist.component';

describe('PodcastRichlistComponent', () => {
  let component: PodcastRichlistComponent;
  let fixture: ComponentFixture<PodcastRichlistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PodcastRichlistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PodcastRichlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
