import { Directive, HostListener, ElementRef } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[jhiContentScroll]',
})
export class ContentScrollDirective {
  constructor(private element: ElementRef) {}

  @HostListener('scroll', ['$event'])
  onScroll(event: Event): void {
    const target = this.element.nativeElement as HTMLElement;
  }
}
