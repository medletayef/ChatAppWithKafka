import { AfterViewInit, Component, inject, OnInit, ViewChild } from '@angular/core';
import { InvitationService } from '../service/invitation.service';
import { MatCardModule } from '@angular/material/card';
import { ITEMS_PER_PAGE } from '../../../config/pagination.constants';
import { FormsModule } from '@angular/forms';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import SharedModule from '../../../shared/shared.module';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'jhi-list-invitations',
  standalone: true,
  imports: [FormsModule, MatButtonModule, MatCardModule, MatTableModule, MatPaginator, MatPaginatorModule, SharedModule],
  templateUrl: './list-invitations.component.html',
  styleUrl: './list-invitations.component.scss',
})
export class ListInvitationsComponent implements AfterViewInit {
  invitations: any[] = [];
  page = 0;
  size = ITEMS_PER_PAGE;
  totalElements = 0;
  displayedColumns: string[] = ['id', 'from', 'roomName', 'createdDate', 'status'];
  private readonly invitationService = inject(InvitationService);

  ngAfterViewInit(): void {
    this.getInvitations();
  }

  getInvitations(): void {
    this.invitationService.getInvitationsByUserId(this.page, this.size).subscribe(res => {
      const data: any[] = [];
      this.totalElements = res.body.totalElements;
      res.body.content?.forEach((value: any) => {
        data.push({
          id: value.id,
          from: value.user.fullName,
          roomName: value.chatRoom.name,
          createdDate: value.createdDate,
          status: value.status,
        });
      });
      this.invitations = data;
    });
  }

  paginate(event: any): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.getInvitations();
  }

  updateInvitationStatus(invitation: any, status: string): void {
    const updatedInvitation = { id: invitation.id, status: status };
    this.invitationService.partialUpdateInvitation(updatedInvitation).subscribe(() => {
      this.getInvitations();
    });
  }
}
