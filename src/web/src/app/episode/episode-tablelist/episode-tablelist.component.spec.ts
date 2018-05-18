import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpisodeTablelistComponent } from './episode-tablelist.component';

describe('EpisodeTablelistComponent', () => {
  let component: EpisodeTablelistComponent;
  let fixture: ComponentFixture<EpisodeTablelistComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpisodeTablelistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpisodeTablelistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
