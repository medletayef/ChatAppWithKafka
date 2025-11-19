import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { InvitationService } from './service/invitation.service';
import { InvitationStatus } from '../../enum/InvitationStatus';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'jhi-invitation',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './invitation.component.html',
  styleUrl: './invitation.component.scss',
})
export class InvitationComponent {
  activeModal = inject(NgbActiveModal);
  @Input() roomEvent: any;
  private readonly invitationService = inject(InvitationService);

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
}
