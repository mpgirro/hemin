import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeRichlistComponent } from './episode-richlist.component';

describe('EpisodeRichlistComponent', () => {
  let component: EpisodeRichlistComponent;
  let fixture: ComponentFixture<EpisodeRichlistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeRichlistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeRichlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
