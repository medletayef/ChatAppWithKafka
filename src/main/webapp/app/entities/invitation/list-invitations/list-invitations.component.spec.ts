import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListInvitationsComponent } from './list-invitations.component';

describe('ListInvitationsComponent', () => {
  let component: ListInvitationsComponent;
  let fixture: ComponentFixture<ListInvitationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListInvitationsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ListInvitationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
