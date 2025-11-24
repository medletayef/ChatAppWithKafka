import { ChangeDetectionStrategy, Component, inject, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { InvitationService } from './service/invitation.service';
import { InvitationStatus } from '../../enum/InvitationStatus';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TrackerService } from '../../core/tracker/tracker.service';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';
import SharedModule from '../../shared/shared.module';
import { NewChatRoom } from '../chat-room/chat-room.model';

@Component({
  selector: 'jhi-invitation',
  standalone: true,
  imports: [FormsModule, MatIconModule, MatButtonModule, MatCheckboxModule, SharedModule],
  templateUrl: './invitation.component.html',
  styleUrl: './invitation.component.scss',
})
export class InvitationComponent implements OnInit {
  activeModal = inject(NgbActiveModal);
  trackerService = inject(TrackerService);
  usersStatus$ = this.trackerService.userStatus$;
  @Input() roomEvent: any;
  sendInvitationsToMembers = false;
  membersToInvite: any[] = [];
  private readonly invitationService = inject(InvitationService);

  ngOnInit(): void {
    console.log('sendInvitationsToMembers = ', this.sendInvitationsToMembers);
    this.usersStatus$.subscribe(res => {
      if (this.roomEvent.members) {
        this.membersToInvite = res.filter(user => !this.roomEvent.members.map((m: any) => m.login).includes(user.userId));
        this.membersToInvite = this.membersToInvite.map(obj => ({
          ...obj,
          selected: false,
        }));
      }
    });
  }

  acceptInvitation(): void {
    this.invitationService.getInvitationByRoomId(this.roomEvent.roomId).subscribe(res => {
      const invitation = res;
      invitation.status = InvitationStatus.ACCEPTED;
      this.invitationService.updateInvitation(invitation).subscribe(() => {
        this.activeModal.close('accepted');
      });
    });
  }

  rejectInvitation(): void {
    this.invitationService.getInvitationByRoomId(this.roomEvent.roomId).subscribe(res => {
      const invitation = res;
      invitation.status = InvitationStatus.REJECTED;
      this.invitationService.updateInvitation(invitation).subscribe(() => {
        this.activeModal.close('rejected');
      });
    });
  }

  selectMember(member: any): void {
    this.membersToInvite = this.membersToInvite.map((m: any) => {
      if (m.userId === member.userId) {
        return { ...m, selected: !m.selected };
      }
      return m;
    });
  }

  sendInvitationsToRoom(): void {
    const recipients = this.membersToInvite.filter(m => m.selected).map(m => m.userId);
    const chatRoom = {
      id: this.roomEvent.id,
      members: recipients,
      name: this.roomEvent.name,
      createdBy: null,
      createdDate: null,
    };
    console.log('chatRoom object for invitations = ', chatRoom);
    this.invitationService.sendInvitationsToRoom(chatRoom).subscribe(() => {
      this.activeModal.close('invitations sent');
    });
  }

  isValidOperation(): boolean {
    return this.membersToInvite.filter(m => m.selected === true).length > 0;
  }
}
