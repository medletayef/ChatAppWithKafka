import { Component, inject, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ChatRoomService } from '../chat-room/service/chat-room.service';

@Component({
  selector: 'jhi-invitation',
  standalone: true,
  imports: [],
  templateUrl: './invitation.component.html',
  styleUrl: './invitation.component.scss',
})
export class InvitationComponent {
  activeModal = inject(NgbActiveModal);
  @Input() roomEvent: any;
  private readonly chatRoomService = inject(ChatRoomService);
}
