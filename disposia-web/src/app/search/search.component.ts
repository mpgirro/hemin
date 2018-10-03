import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ParamMap} from '@angular/router';

import { Result } from '../result.model';
import { SearchService } from '../search.service';
import { DomainService } from '../domain.service';
import {ResultWrapper} from '../resultwrapper.model';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  DEFAULT_SIZE = 20;

  currSize: number;
  currPage: number;
  maxPage: number;
  pages: number[];

  totalHits: number;

  results: Result[];
  query: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private searchService: SearchService,
              private domainService: DomainService) { }

  ngOnInit() {
    this.search();
  }

  search(): void {
    this.route.paramMap
      .switchMap((params: ParamMap) => {
        const q = params.get('q');
        const p = params.get('p');
        const s = params.get('s');
        this.query = q;
        this.currPage = (p) ? Number(p) : 1;
        this.currSize = (s) ? Number(s) : this.DEFAULT_SIZE;
        return this.searchService.search(this.query, this.currPage, this.currSize);
      }).subscribe(response => this.onSearchResponse(response));
  }

  onSearchResponse(response: ResultWrapper) {
    this.currPage  = response.currPage;
    this.maxPage   = response.maxPage;
    this.totalHits = response.totalHits;
    this.results   = response.results;

    this.pages = new Array(this.maxPage).fill(0 ).map((x, i) => i + 1);
  }

  onEnter(query: string): void {
    if (query) {
      this.router.navigate(['/search', { 'q': query, 'p' : 1, 's': this.currSize }]);
      this.search();
    } else {
      this.router.navigate(['/search']);
    }
  }

  navigate(result: Result): void {
    let pre;
    if (result.docType === 'podcast') {
      pre = '/p';
    } else if (result.docType === 'episode') {
      pre = '/e';
    } else {
      console.log('Unknown docType : ' + result.docType);
    }
    this.router.navigate([pre,  result.id]);
  }

}
