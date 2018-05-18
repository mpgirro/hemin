import { TestBed, inject } from '@angular/core/testing';

import { EpisodeService } from './episode.service';

describe('EpisodeService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EpisodeService]
    });
  });

  it('should be created', inject([EpisodeService], (service: EpisodeService) => {
    expect(service).toBeTruthy();
  }));
});
