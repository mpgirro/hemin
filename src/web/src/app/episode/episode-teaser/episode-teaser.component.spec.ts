import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeTeaserComponent } from './episode-teaser.component';

describe('EpisodeTeaserComponent', () => {
  let component: EpisodeTeaserComponent;
  let fixture: ComponentFixture<EpisodeTeaserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeTeaserComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeTeaserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
