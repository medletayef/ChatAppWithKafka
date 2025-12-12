import { AfterViewInit, Component, CUSTOM_ELEMENTS_SCHEMA, inject, Input, OnInit } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ChatRoomService } from '../../chat-room/service/chat-room.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'jhi-muting',
  standalone: true,
  imports: [MatSlideToggleModule, FormsModule],
  templateUrl: './muting.component.html',
  styleUrl: './muting.component.scss',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class MutingComponent implements AfterViewInit {
  chatRoomSummary: any;
  notificationParam: any;
  activeModal = inject(NgbActiveModal);
  protected readonly chatRoomService = inject(ChatRoomService);
  private _snackBar = inject(MatSnackBar);
  ngAfterViewInit(): void {
    this.chatRoomService.getRoomNotification(this.chatRoomSummary.id).subscribe(res => {
      //    console.log('notificationParam = ', res);
      this.notificationParam = res;
    });
  }

  muteUnmuteNotifications(): void {
    this.chatRoomService.muting(this.chatRoomSummary.id).subscribe();
  }
}
