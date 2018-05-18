import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';

import { throwIfAlreadyLoaded } from './module-import.guard';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    BrowserModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    RouterModule,
    BrowserModule
  ],
  declarations: [
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    throwIfAlreadyLoaded(parentModule, 'CoreModule');
  }
}
