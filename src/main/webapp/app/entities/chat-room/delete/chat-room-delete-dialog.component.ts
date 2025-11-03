import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IChatRoom } from '../chat-room.model';
import { ChatRoomService } from '../service/chat-room.service';

@Component({
  standalone: true,
  templateUrl: './chat-room-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ChatRoomDeleteDialogComponent {
  chatRoom?: IChatRoom;

  protected chatRoomService = inject(ChatRoomService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.chatRoomService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
