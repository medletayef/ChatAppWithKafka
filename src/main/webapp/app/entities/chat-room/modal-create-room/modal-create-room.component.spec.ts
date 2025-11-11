import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalCreateRoomComponent } from './modal-create-room.component';

describe('ModalCreateRoomComponent', () => {
  let component: ModalCreateRoomComponent;
  let fixture: ComponentFixture<ModalCreateRoomComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalCreateRoomComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalCreateRoomComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
