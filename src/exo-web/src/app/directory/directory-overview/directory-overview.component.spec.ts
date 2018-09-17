import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DirectoryOverviewComponent } from './directory-overview.component';

describe('DirectoryOverviewComponent', () => {
  let component: DirectoryOverviewComponent;
  let fixture: ComponentFixture<DirectoryOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DirectoryOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DirectoryOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
