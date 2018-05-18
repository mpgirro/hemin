import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DirectoryListComponent } from './directory-list.component';

describe('DirectoryListComponent', () => {
  let component: DirectoryListComponent;
  let fixture: ComponentFixture<DirectoryListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DirectoryListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DirectoryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
