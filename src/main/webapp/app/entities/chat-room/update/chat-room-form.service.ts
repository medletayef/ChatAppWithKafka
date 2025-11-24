import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IChatRoom, NewChatRoom } from '../chat-room.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IChatRoom for edit and NewChatRoomFormGroupInput for create.
 */
type ChatRoomFormGroupInput = IChatRoom | PartialWithRequiredKeyOf<NewChatRoom>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IChatRoom | NewChatRoom> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

type ChatRoomFormRawValue = FormValueOf<IChatRoom>;

type NewChatRoomFormRawValue = FormValueOf<NewChatRoom>;

type ChatRoomFormDefaults = Pick<NewChatRoom, 'id' | 'createdDate' | 'members'>;

type ChatRoomFormGroupContent = {
  id: FormControl<ChatRoomFormRawValue['id'] | NewChatRoom['id']>;
  name: FormControl<ChatRoomFormRawValue['name']>;
  createdDate: FormControl<ChatRoomFormRawValue['createdDate']>;
  members: FormControl<ChatRoomFormRawValue['members']>;
};

export type ChatRoomFormGroup = FormGroup<ChatRoomFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ChatRoomFormService {
  // createChatRoomFormGroup(chatRoom: ChatRoomFormGroupInput = { id: null }): ChatRoomFormGroup {
  //   const chatRoomRawValue = this.convertChatRoomToChatRoomRawValue({
  //     ...this.getFormDefaults(),
  //     ...chatRoom,
  //   });
  //   return new FormGroup<ChatRoomFormGroupContent>({
  //     id: new FormControl(
  //       { value: chatRoomRawValue.id, disabled: true },
  //       {
  //         nonNullable: true,
  //         validators: [Validators.required],
  //       },
  //     ),
  //     name: new FormControl(chatRoomRawValue.name, {
  //       validators: [Validators.required],
  //     }),
  //     createdDate: new FormControl(chatRoomRawValue.createdDate, {
  //       validators: [Validators.required],
  //     }),
  //     members: new FormControl(chatRoomRawValue.members ?? []),
  //   });
  // }
  //
  // getChatRoom(form: ChatRoomFormGroup): IChatRoom | NewChatRoom {
  //   return this.convertChatRoomRawValueToChatRoom(form.getRawValue() as ChatRoomFormRawValue | NewChatRoomFormRawValue);
  // }
  //
  // resetForm(form: ChatRoomFormGroup, chatRoom: ChatRoomFormGroupInput): void {
  //   const chatRoomRawValue = this.convertChatRoomToChatRoomRawValue({ ...this.getFormDefaults(), ...chatRoom });
  //   form.reset(
  //     {
  //       ...chatRoomRawValue,
  //       id: { value: chatRoomRawValue.id, disabled: true },
  //     } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
  //   );
  // }
  //
  // private getFormDefaults(): ChatRoomFormDefaults {
  //   const currentTime = dayjs();
  //
  //   return {
  //     id: null,
  //     createdDate: currentTime,
  //     members: [],
  //   };
  // }
  //
  // private convertChatRoomRawValueToChatRoom(rawChatRoom: ChatRoomFormRawValue | NewChatRoomFormRawValue): IChatRoom | NewChatRoom {
  //   return {
  //     ...rawChatRoom,
  //     createdDate: dayjs(rawChatRoom.createdDate, DATE_TIME_FORMAT),
  //   };
  // }
  //
  // private convertChatRoomToChatRoomRawValue(
  //   chatRoom: IChatRoom | (Partial<NewChatRoom> & ChatRoomFormDefaults),
  // ): ChatRoomFormRawValue | PartialWithRequiredKeyOf<NewChatRoomFormRawValue> {
  //   return {
  //     ...chatRoom,
  //     createdDate: chatRoom.createdDate ? chatRoom.createdDate.format(DATE_TIME_FORMAT) : undefined,
  //     members: chatRoom.members ?? [],
  //   };
  // }
}
