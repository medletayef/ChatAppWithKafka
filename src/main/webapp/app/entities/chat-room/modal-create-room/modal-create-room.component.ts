import { AfterViewInit, Component, ElementRef, inject, Input, NO_ERRORS_SCHEMA, OnInit, viewChild } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NewChatRoom } from '../chat-room.model';
import { ChatRoomService } from '../service/chat-room.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
@Component({
  selector: 'jhi-modal-create-room',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, FormsModule],
  templateUrl: './modal-create-room.component.html',
  styleUrl: './modal-create-room.component.scss',
  schemas: [NO_ERRORS_SCHEMA],
})
export class ModalCreateRoomComponent implements AfterViewInit {
  activeModal = inject(NgbActiveModal);
  chatRoom: NewChatRoom = { id: null, name: null, createdAt: null, members: [] };
  roomNameRef = viewChild.required<ElementRef>('RoomNameRef');
  @Input() user: any;

  @Input() members: any;
  private _snackBar = inject(MatSnackBar);
  private readonly chatRoomService = inject(ChatRoomService);

  ngAfterViewInit(): void {
    this.roomNameRef().nativeElement.focus();
  }

  //
  //
  //
  confirm(): void {
    if (this.user) {
      this.chatRoom.members?.push(this.user.userId);
      this.chatRoomService.create(this.chatRoom).subscribe(() => {
        this.activeModal.close('room created');
      });
    }
  }
}
