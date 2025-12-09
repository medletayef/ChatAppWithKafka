import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MutingComponent } from './muting.component';

describe('MutingComponent', () => {
  let component: MutingComponent;
  let fixture: ComponentFixture<MutingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MutingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MutingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
