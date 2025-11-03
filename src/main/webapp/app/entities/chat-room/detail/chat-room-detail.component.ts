import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { IChatRoom } from '../chat-room.model';

@Component({
  standalone: true,
  selector: 'jhi-chat-room-detail',
  templateUrl: './chat-room-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class ChatRoomDetailComponent {
  chatRoom = input<IChatRoom | null>(null);

  previousState(): void {
    window.history.back();
  }
}
