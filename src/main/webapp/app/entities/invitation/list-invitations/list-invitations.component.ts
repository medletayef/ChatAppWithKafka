import { AfterViewInit, Component, HostListener, inject, OnInit, signal, ViewChild } from '@angular/core';
import { InvitationService } from '../service/invitation.service';
import { MatCardModule } from '@angular/material/card';
import { ITEMS_PER_PAGE } from '../../../config/pagination.constants';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import SharedModule from '../../../shared/shared.module';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import NavbarComponent from '../../../layouts/navbar/navbar.component';
import dayjs from 'dayjs';

@Component({
  selector: 'jhi-list-invitations',
  standalone: true,
  imports: [FormsModule, MatButtonModule, MatCardModule, MatTableModule, MatPaginator, MatPaginatorModule, SharedModule, NavbarComponent],
  templateUrl: './list-invitations.component.html',
  styleUrl: './list-invitations.component.scss',
})
export class ListInvitationsComponent implements AfterViewInit {
  invitations: any[] = [];
  page = 0;
  size = ITEMS_PER_PAGE;
  totalElements = 0;
  displayedColumns: string[] = ['id', 'from', 'roomName', 'createdDate', 'status'];
  transposedInvitations: any[] = [];

  readonly screenWidth = signal<number>(window.innerWidth);
  cardInvitationsHeight = window.innerHeight * 0.8;

  displayedColumnsSmall: string[] = ['property', 'value'];
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

  scrollEnd(): void {
    this.page = 0;
    this.size += 5;
    this.getInvitations();
  }

  updateInvitationStatus(invitation: any, status: string): void {
    const updatedInvitation = { id: invitation.id, status: status };
    this.invitationService.partialUpdateInvitation(updatedInvitation).subscribe(() => {
      this.getInvitations();
    });
  }

  @HostListener('window:resize')
  onResize(): void {
    this.screenWidth.set(window.innerWidth);
    this.cardInvitationsHeight = window.innerHeight * 0.8;
    if (this.screenWidth() < 850) {
      if (this.transposedInvitations.length === 0) {
        this.transposedInvitations = this.transpose(this.invitations);
      }

      console.log(this.transposedInvitations);
    } else {
      this.page = 0;
      this.size = ITEMS_PER_PAGE;
      this.getInvitations();
    }
  }

  transpose(data: any[]): any {
    if (!data || !data.length) {
      return [];
    }

    const keys = Object.keys(data[0]);

    const result: any[] = [];
    data.forEach(item => {
      keys.map(key => {
        const row: any = { property: key };
        if (key === 'createdDate') {
          row['value'] = dayjs(item[key]).format('DD-MM-YYYY HH:mm');
        } else {
          row['value'] = item[key];
        }
        result.push(row);
      });
    });
    return result;
  }
}
