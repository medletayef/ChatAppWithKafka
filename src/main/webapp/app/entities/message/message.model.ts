import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { IChatRoom } from 'app/entities/chat-room/chat-room.model';

export interface IMessage {
  id: number;
  content?: string | null;
  sentAt?: dayjs.Dayjs | null;
  sender?: Pick<IUser, 'id' | 'login'> | null;
  room?: Pick<IChatRoom, 'id'> | null;
}

export type NewMessage = Omit<IMessage, 'id'> & { id: null };
