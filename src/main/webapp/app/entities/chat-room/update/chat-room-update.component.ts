import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IChatRoom } from '../chat-room.model';
import { ChatRoomService } from '../service/chat-room.service';
import { ChatRoomFormGroup, ChatRoomFormService } from './chat-room-form.service';

@Component({
  standalone: true,
  selector: 'jhi-chat-room-update',
  templateUrl: './chat-room-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ChatRoomUpdateComponent {
  // isSaving = false;
  // chatRoom: IChatRoom | null = null;
  //
  // usersSharedCollection: IUser[] = [];
  //
  // protected chatRoomService = inject(ChatRoomService);
  // protected chatRoomFormService = inject(ChatRoomFormService);
  // protected userService = inject(UserService);
  // protected activatedRoute = inject(ActivatedRoute);
  //
  // // eslint-disable-next-line @typescript-eslint/member-ordering
  // editForm: ChatRoomFormGroup = this.chatRoomFormService.createChatRoomFormGroup();
  //
  // compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);
  // ngOnInit(): void {
  //   this.activatedRoute.data.subscribe(({ chatRoom }) => {
  //     this.chatRoom = chatRoom;
  //     if (chatRoom) {
  //       this.updateForm(chatRoom);
  //     }
  //
  //     this.loadRelationshipsOptions();
  //   });
  // }
  // previousState(): void {
  //   window.history.back();
  // }
  //
  // save(): void {
  //   this.isSaving = true;
  //   const chatRoom = this.chatRoomFormService.getChatRoom(this.editForm);
  //   if (chatRoom.id !== null) {
  //     this.subscribeToSaveResponse(this.chatRoomService.update(chatRoom));
  //   } else {
  //     this.subscribeToSaveResponse(this.chatRoomService.create(chatRoom));
  //   }
  // }
  //
  // protected subscribeToSaveResponse(result: Observable<HttpResponse<IChatRoom>>): void {
  //   result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
  //     next: () => this.onSaveSuccess(),
  //     error: () => this.onSaveError(),
  //   });
  // }
  //
  // protected onSaveSuccess(): void {
  //   this.previousState();
  // }
  //
  // protected onSaveError(): void {
  //   // Api for inheritance.
  // }
  //
  // protected onSaveFinalize(): void {
  //   this.isSaving = false;
  // }
  // protected updateForm(chatRoom: IChatRoom): void {
  //   this.chatRoom = chatRoom;
  //   this.chatRoomFormService.resetForm(this.editForm, chatRoom);
  //
  //   this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(
  //     this.usersSharedCollection,
  //     ...(chatRoom.members ?? []),
  //   );
  // }
  //
  // protected loadRelationshipsOptions(): void {
  //   this.userService
  //     .query()
  //     .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
  //     .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, ...(this.chatRoom?.members ?? []))))
  //     .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  // }
}
