import { Result } from './result.model';

export class ResultWrapper {
    currPage: number;
    maxPage: number;
    totalHits: number;
    results: Result[];

    constructor() {
        this.results = [];
    }

}
